import logging
import os
import sys

logging.basicConfig(level=logging.DEBUG, stream=sys.stdout, format="%(asctime)s [%(name)s:%(module)s:%(filename)s:%(funcName)s] %(levelname)s %(message)s")

mod_dir = os.path.dirname(os.path.abspath(__file__)) + "/../mod1"
logging.info("mod_dir=" + mod_dir)

sys.path.append(mod_dir)

from aa.bb.f2 import fn


def main():
    logging.info("Hello")
    fn()

if __name__ == '__main__':
    main()
