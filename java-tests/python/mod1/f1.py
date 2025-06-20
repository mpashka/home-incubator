import logging
import sys

from aa.bb.f2 import fn

logging.basicConfig(level=logging.DEBUG, stream=sys.stdout, format="%(asctime)s [%(name)s:%(module)s:%(filename)s:%(funcName)s] %(levelname)s %(message)s")
logging.info("Hello f1")


def main():
    logging.info("Hello")
    fn()

if __name__ == '__main__':
    main()
