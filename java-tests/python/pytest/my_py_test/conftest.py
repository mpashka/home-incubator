import io
import logging
import os
import os.path
from logging import DEBUG
from sys import stdout

import grpc
import pytest
import sys


from my_grpc.mocks import GrpcServiceMock

log_format_template = "%(asctime)s [{}:%(name)s] %(levelname)s %(message)s"
logging.basicConfig(level=logging.DEBUG, stream=sys.stderr, format=log_format_template)

stdout_logger_ = logging.Logger(logging.DEBUG)
console_handler_ = logging.StreamHandler(sys.stdout)
console_handler_.setFormatter(logging.Formatter("%(asctime)s [%(name)s] %(levelname)s %(message)s"))
stdout_logger_.addHandler(console_handler_)
stdout_logger_.info("Conftest Start stdout")

stderr_logger_ = logging.Logger(logging.DEBUG)
console_handler_ = logging.StreamHandler(sys.stderr)
console_handler_.setFormatter(logging.Formatter(log_format_template))
stderr_logger_.addHandler(console_handler_)
stderr_logger_.info("Conftest Start stderr")

@pytest.fixture(scope='session')
def log_dir(request):
    stdout_logger_.info("log_dir test stdout")
    stderr_logger_.info("log_dir test stderr")
    log_dir = request.config.option.log_dir
    if not os.path.isdir(log_dir):
        os.makedirs(log_dir)
    return log_dir


@pytest.fixture(scope='function')
def test_name(request):
    # replace fs-like symbols
    test_name = request.node.name.replace('/', '.').replace('\\', '.')

    return test_name


class LogData:
    log_format_template = "%(asctime)s [{}:%(name)s] %(levelname)s %(message)s"

    def __init__(self, log_dir, out, name):
        self.basic_file = os.path.join(log_dir, 'all-tests.log')
        self.file_handler = logging.FileHandler(self.basic_file)
        self.basic_format = self.log_format_template.format(name)
        self.formatter = logging.Formatter(self.basic_format)
        self.file_handler.formatter = self.formatter
        self.console_handler = logging.StreamHandler(out)
        self.console_handler.formatter = self.formatter
        self.handlers = [self.console_handler, self.file_handler]

    def set_test(self, test_log_dir, name):
        logging.info("Starting test {}".format(name))
        self.formatter._fmt = self.log_format_template.format(name)
        self.file_handler.baseFilename = os.path.join(test_log_dir, 'test.log')
        self.file_handler.stream = None
        logging.info("Starting test {}".format(name))

    def unset_test(self):
        logging.info("Finish test")
        self.file_handler.baseFilename = self.basic_file
        self.file_handler.stream = None
        self.formatter._fmt = self.basic_format
        logging.info("Finish test")


@pytest.fixture(scope='session', autouse=True)
def log_data(log_dir, worker_id):
    xdist_follower = worker_id != 'master'
    is_follower = xdist_follower is not None
    log_data = logging.log_data = LogData(log_dir, sys.stderr if is_follower else sys.stdout, worker_id)
    logging.root.setLevel(logging.NOTSET)
    logging.root.handlers = log_data.handlers
    if is_follower:
        _redirect_std()
    return log_data


def _redirect_std():
    log_stdout = logging.getLogger("stdout")
    sys.stdout = io.StringIO()
    sys.stdout.write = log_stdout.info
    log_stderr = logging.getLogger("stdout")
    sys.stderr = io.StringIO()
    sys.stderr.write = log_stderr.info


def _get_xdist_replica(config):
    # TODO: have no idea how to check isinstance "__channelexec__.SlaveInteractor"
    slave_list = filter(lambda p: hasattr(p, 'slaveid'), config.plugin_manager._plugins)
    return slave_list[0] if len(slave_list) == 1 else None


@pytest.fixture(scope='session', autouse=True)
def init_out(worker_id, request, log_dir):
    # Save the current stdout, so we can restore it later
    original_stdout = sys.stdout
    original_stderr = sys.stderr

    # _init_logger(log_dir, worker_id, 'test_name')

    # # Create a file object and assign it to sys.stdout
    # with open(log_file, "w") as file:
    #     sys.stdout = file
    #     print("This text will be written to the file.")
    #     # request.config.option.test_server
    #     # return _create_logger()
    def fin():
        sys.stdout = original_stdout
        sys.stderr = original_stderr

    request.addfinalizer(fin)


@pytest.fixture(scope='function')
def test_log_dir(log_dir, test_name):
    path = os.path.join(log_dir, test_name)
    if not os.path.exists(path):
        os.makedirs(path)
    return path


@pytest.fixture(scope='function')
def logger(request, log_data, test_log_dir, test_name, worker_id):
    log_data.set_test(test_log_dir, test_name)
    logger = logging.getLogger("test-{test_name}-{worker_id}".format(
        test_name=test_name, worker_id=worker_id))

    def fin():
        # print("--------- request")
        # print(request)
        # print(request.__dict__)
        # print("--------- request.node")
        # print(request.node)
        # print(request.node.__dict__)
        # print("---------")
        log_data.unset_test()

    request.addfinalizer(fin)
    return logger

@pytest.fixture(scope='session')
def grpc_mock(request, log_dir):
    logging.info("Init grpc")
    mock = GrpcServiceMock(10000, log_dir + '/grpc_mock.log')
    mock.start_server()
    logging.info("Grpc server started")
    return mock
