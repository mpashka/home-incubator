import logging
import sys
import time

from start_server import GrpcServiceMock
from start_client import test_grpc

logging.basicConfig(
    level=logging.NOTSET, stream=sys.stdout,
    format="%(asctime)s [%(name)s:%(module)s:%(filename)s:%(funcName)s] %(levelname)s %(message)s")
logging.root.setLevel(logging.NOTSET)
logging_stdout_handler = logging.StreamHandler(sys.stdout)
logging_stdout_handler.setFormatter(logging.Formatter('%(asctime)s %(levelname)s %(funcName)s: %(message)s'))
logging.root.handlers = [logging_stdout_handler]

def start_all():
    mock = GrpcServiceMock.create_server()
    test_grpc(logger=logging)
    # time.sleep(100000)
    mock.stop()


if __name__ == '__main__':
    start_all()