import argparse
import collections
import json
import logging
import os
import shutil

import porto
import sys
import time
import multiprocessing

from porto.api import Connection
# from porto.exceptions import ContainerDoesNotExist, EError

# AGENT_DIR = '/db/iss3'
AGENT_DIR = '/home/ya-pashka/Projects/github/m_pashka/home-incubator/java-tests/python/iss3'
AGENT_ROOT1_CONTAINER = '56789-iss-agent-mine'
AGENT_ROOT2_CONTAINER = '56789-iss-agent-mine/aaa'
AGENT_CONTAINER = '56789-iss-agent-mine/aaa/start_hook'
AGENT_CONF = os.path.join(AGENT_DIR, "application.conf")
AGENT_JAR = os.path.join(AGENT_DIR, "iss-agent.jar")
VM_OPTIONS = os.path.join(AGENT_DIR, "iss-agent.vmoptions")
PORTO_OPTIONS = os.path.join(AGENT_DIR, "porto.options")

PORTO_CONNECTION_TIMEOUT = 120
PORTO_CONTAINER_STOP_TIMEOUT = 120
PORTO_SOCKET_ERROR_RETRY_COUNT = 30
PORTO_SOCKET_ERROR_RETRY_DELAY = 1  # in seconds

JAVA_BIN = os.path.join("/opt/java/jdk-8", "bin", "java")

COMMAND_TEMPLATE_WITH_CONF_D = "{java_bin} {jvm_options} -Dagent.jar.dir={agent_dir} -jar {agent_jar} --config {agent_conf} --config-directory {agent_conf_d} --old-config {agent_old_conf} --config-safety-marker {config_safety_marker}"
# COMMAND_TEMPLATE = "{java_bin} {jvm_options} -Dagent.jar.dir={agent_dir} -jar {agent_jar} --config {agent_conf}"
COMMAND_TEMPLATE = "{java_bin} {jvm_options} -jar {agent_jar} --config {agent_conf}"

logging.basicConfig(level=logging.NOTSET, stream=sys.stdout, format='%(asctime)s [%(name)s] %(levelname)s %(message)s', datefmt='%Y-%d-%m %H:%M:%S')

logFormatter = logging.Formatter("%(name)s %(asctime)s %(levelname)s %(message)s")
log = logging.getLogger(os.path.basename(__file__))

def parse_options(options_file):
    with open(options_file) as opts:
        for line in opts:
            line = line.strip()
            if line and not line.startswith("#"):
                yield line


def touch(fname, times=None):
    with open(fname, 'a'):
        os.utime(fname, times)


class PortoConnection(object):

    def __init__(self, *args, **kwargs):
        self.args = args
        self.kwargs = kwargs

    def __enter__(self):
        log.debug("opening porto socket connection args=%s kwargs=%s", self.args, self.kwargs)
        # type: Connection
        self.connection = Connection(*self.args, **self.kwargs)
        return self.connection

    def __exit__(self, type, value, traceback):
        log.debug("closing porto socket connection args=%s kwargs=%s", self.args, self.kwargs)
        self.connection.disconnect()


class PortoClient(object):
    STATUS_DEAD = 'dead'

    def __init__(self, connection):
        # type: (Connection) -> None
        self.connection = connection

    def state(self, container):
        state = self.connection.Get([container], ['state'])[container]['state']
        if isinstance(state, Exception):
            raise state
        return state

    def exists(self, container):
        try:
            return self.state(container)
        except porto.exceptions.ContainerDoesNotExist:
            return False

    def destroy(self, container):
        return self.connection.Destroy(container)

    def create(self, container):
        return self.connection.Create(container)

    def get_property(self, container, property_name):
        return self.connection.GetProperty(container, property_name)

    def get_data(self, container, property_name):
        return self.connection.GetData(container, property_name)

    def set_properties(self, container, **props):
        return self.connection.Set(container, **props)

    def start(self, container):
        return self.connection.Start(container)

    def wait(self, container):
        return self.connection.Wait([container])

    def stop(self, container, timeout):
        return self.connection.Stop(container, timeout)

    def kill(self, container, signal):
        return self.connection.Kill(container, signal)


class AgentRunner:
    def __init__(self, client, container):
        # type: (PortoClient, str) -> None
        self.client = client
        self.container = container

    def _container_exists(self):
        return self.client.exists(self.container)

    def destroy(self, timeout):
        if self.stop(timeout):
            log.info("destroying porto container %s", self.container)
            self.client.destroy(self.container)

    def stop(self, timeout_sec):
        status = self._container_exists()
        if status == PortoClient.STATUS_DEAD:
            return True
        if status:
            log.info("stopping porto container %s", self.container)
            self.client.stop(self.container, timeout=timeout_sec)
        return status

    def start(self):
        log.info("creating porto container %s", self.container)
        self.client.create(self.container)

        vm_options = " ".join(parse_options(VM_OPTIONS))

        # do not use mapping here to allow
        # options to override preceding
        porto_options = {}
        for pair in parse_options(PORTO_OPTIONS):
            prop, value = pair.split("=", 1)
            porto_options[prop.strip()] = value.strip()

        # assign last to not allow user to override command
        command = COMMAND_TEMPLATE.format(
            java_bin=JAVA_BIN,
            jvm_options=vm_options,
            agent_dir=AGENT_DIR,
            agent_jar=AGENT_JAR,
            agent_conf=AGENT_CONF
        )

        porto_options['command'] = command

        log.debug("parsed jvm_options='%s', porto_options='%s'",
                  vm_options, porto_options)

        self.client.set_properties(self.container, **porto_options)

        log.info("starting porto container %s", self.container)
        self.client.start(self.container)

    def wait_running(self, state, timeout_sec):
        log.debug("Wait agent container state {}...".format(state))
        end_time = time.time() + timeout_sec
        while True:
            current_state = self.client.state(self.container)
            if current_state == state:
                log.info("Agent container state: {}".format(state))
                return
            if time.time() > end_time:
                log.warn("Agent container not {expected}: {current}".format(expected=state, current=current_state))
                return
            time.sleep(0.5)

    @staticmethod
    def wait_alive(timeout_sec):
        log.debug("Wait agent alive")
        end_time = time.time() + timeout_sec
        alive_file = os.path.join(AGENT_DIR, "run", ".alive")
        while time.time() < end_time:
            if os.path.isfile(alive_file):
                log.info("Agent started successfully")
                return
            time.sleep(0.5)
        log.warn("Agent start error")

    def kill(self, signal):
        self.client.kill(self.container, signal)

    def wait_exit(self):
        sock_err = 0
        while True:
            try:
                if not sock_err:
                    log.info("waiting for %s porto container exit", self.container)
                    self.client.wait(self.container)
                    break
                else:
                    state = self.client.get_property(self.container, 'state')
                    log.info("container state: %s", state)
                    sock_err = 0
            except porto.exceptions.ContainerDoesNotExist:
                log.error('container %s not found. Exit.', self.container)
                break
            except porto.exceptions.SocketTimeout:
                sock_err += 1
                log.info("porto socket timeout, error count: %d", sock_err)
                if sock_err >= PORTO_SOCKET_ERROR_RETRY_COUNT:
                    log.error('porto did not respond for %d seconds. Exit.',
                              PORTO_SOCKET_ERROR_RETRY_COUNT * PORTO_SOCKET_ERROR_RETRY_DELAY)
                    break
                time.sleep(PORTO_SOCKET_ERROR_RETRY_DELAY)


if __name__ == 'aa__main__':
    with PortoConnection(timeout=PORTO_CONNECTION_TIMEOUT) as connection:
        run_dir = os.path.join(AGENT_DIR, "run")
        shutil.rmtree(run_dir, ignore_errors=True)
        if not os.path.exists(run_dir):
            os.makedirs(run_dir)
        _porto_connection = connection
        _porto_connection.Create(AGENT_ROOT1_CONTAINER)
        _porto_connection.Create(AGENT_ROOT2_CONTAINER)

        porto_client = PortoClient(connection)
        runner = AgentRunner(porto_client, AGENT_CONTAINER)
        runner.start()
        runner.wait_running('running', 3)

        names = _porto_connection.List(mask='56789*')
        log.debug("m-start: %s" % names)
        # names = _porto_connection.List(mask='iss-agent-.*-mine')
        # log.debug("m2: %s" % names)
        # names = _porto_connection.List(mask='iss-.*-mine')
        # log.debug("m3: %s" % names)
        # names = _porto_connection.List(mask='iss-*-mine')
        # log.debug("m4: %s" % names)
        # names = _porto_connection.List(mask='*-mine')
        # log.debug("m5: %s" % names)
        names = _porto_connection.List(mask='*')
        log.debug("m-all*: %s" % names)
        names = _porto_connection.List()
        log.debug("m-all: %s" % names)
        names = _porto_connection.List(mask='56789*start_hook')
        log.debug("mmask0: %s" % names)
        names = _porto_connection.List(mask='56789*/start_hook')
        log.debug("mmask1: %s" % names)
        names = _porto_connection.List(mask='56789' + '*/' + 'start_hook')
        log.debug("mmask2: %s" % names)
        names = _porto_connection.List(mask='56789' + '*/*/' + 'start_hook')
        log.debug("mmask3: %s" % names)


        # runner.wait_alive(10)
        # log.debug("Wait 10 seconds")
        # time.sleep(10)

        log.info("Kill container")
        runner.kill(2)
        runner.wait_running('dead', 5)
        log.info("Stop container")
        runner.stop(4)
        runner.destroy(4)
        _porto_connection.Destroy(AGENT_ROOT2_CONTAINER)
        _porto_connection.Destroy(AGENT_ROOT1_CONTAINER)

# root_pid

if __name__ == '__main__':
    with PortoConnection(timeout=PORTO_CONNECTION_TIMEOUT) as connection:
        _porto_connection = connection
        container = 'aaa'
        key = 'root_pid'
        val0 = _porto_connection.Get([container], [key])
        val = val0[container][key]
        log.debug("val0: %s, val: %s" % (val0, val))
        log.debug("Doesnt: %s" % isinstance(val, porto.exceptions.EError))
