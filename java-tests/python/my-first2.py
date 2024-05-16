import subprocess
import os
import logging
import sys

if __name__ == '__main__':
    # check_kill()
    # check_url()
    # check_chown()
    print('aaa')
    # subprocess.call(['echo', "bbb"])
    # subprocess.call(['portoctl', 'vlist'])
    debugLogger = logging.getLogger("debug")
    debugLogger.setLevel(logging.DEBUG)
    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(name)s %(message)s"))
    handler2 = logging.FileHandler("debug.log", mode='w')
    formatter2 = logging.Formatter("%(created)f %(name)s %(asctime)s %(levelname)s %(message)s")
    handler2.setFormatter(formatter2)
    debugLogger.addHandler(handler)
    debugLogger.addHandler(handler2)

    os.system("echo 'os.system' >&2")

    result = os.popen("echo 'aaaa' >&2").read()
    debugLogger.debug("result: {result}".format(result=result))
    debugLogger.debug("done")
