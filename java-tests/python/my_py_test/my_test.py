import logging
import os
import sys
import time

iteration = 1
delay = 1

def test_something(logger):
    print("Start test")
    logger.info("Info!")
    print("End test")
    # print >> sys.stderr, 'test_err stream'


# def test_fail(logger):
#     global iteration
#     logger.info("Iteration {iteration}".format(iteration=iteration))
#     iteration += 1
#     assert iteration > 5


def test_1(logger):
    logger.info("Test 1. Sleep {delay} seconds in {pid}".format(delay=delay, pid=os.getpid()))
    time.sleep(delay)
    logger.info("End")


def test_2(logger):
    logger.info("Test 2. Sleep {delay} seconds in {pid}".format(delay=delay, pid=os.getpid()))
    time.sleep(delay)
    logger.info("End")


def test_3(logger):
    logger.info("Test 3. Sleep {delay} seconds in {pid}".format(delay=delay, pid=os.getpid()))
    time.sleep(delay)
    logger.info("End")


def test_4(logger):
    logger.info("Test 4. Sleep {delay} seconds in {pid}".format(delay=delay, pid=os.getpid()))
    time.sleep(delay)
    logger.info("End")



# class Tests(object):
#
#     def test_one(self, logger):
#         logger.info("Test one")
#
#     def test_two(self, logger):
#         logger.info("Test two")
