import getpass
import os
import pwd
import stat


def check_kill():
    os.kill(112657, 0)
    # PermissionError: [Errno 1] Operation not permitted
    # ProcessLookupError: [Errno 3] No such process


def check_url():
    # url = "http://localhost:{port}/actuator/health/readiness".format(port=2000)
    url = "https://www.google.com"
    result = requests.get(url, timeout=2)
    if result.status_code != 201:
        raise Exception("Not 200: {}".format(result.status_code))
    # requests.exceptions.ConnectionError: HTTPConnectionPool(host='localhost', port=2000): Max retries exceeded with
    #   url: /actuator/health/readiness (Caused by NewConnectionError('<urllib3.connection.HTTPConnection object at
    #   0x7fc7a01a1f90>: Failed to establish a new connection: [Errno 111] Connection refused'))
    # Exception: Not 200: 200


def check_ps():
    pass


def check_chown():
    yatool_tmp = "/tmp/test1"
    os.makedirs(yatool_tmp, exist_ok=True)
    # user = getpass.getuser()
    user = 'ya-pashka'
    uid = pwd.getpwnam(user).pw_uid
    os.chown(yatool_tmp, uid, 0)
    os.chmod(yatool_tmp, stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)


if __name__ == '__main__':
    # check_kill()
    # check_url()
    # check_chown()
    print('aaa')
    os.system('portoctl list')
