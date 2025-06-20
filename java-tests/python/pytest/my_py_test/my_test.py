import logging
import os
import sys
import time

import grpc
from test_grpc import route_guide_pb2_grpc, route_guide_pb2

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


def test_grpc(logger, grpc_mock):
    feature = route_guide_pb2.Feature(name="defined_feature", location=route_guide_pb2.Point(latitude=10, longitude=20))
    grpc_mock.set_feature(feature)

    channel = grpc.insecure_channel('localhost:50051')
    stub = route_guide_pb2_grpc.RouteGuideStub(channel)
    point = route_guide_pb2.Point(latitude = 1, longitude = 2)
    logger.info("Call server with {point}".format(point=point))
    feature = stub.GetFeature(point)
    logger.info("Server response with {feature}".format(feature=feature))
    channel.close()

# class Tests(object):
#
#     def test_one(self, logger):
#         logger.info("Test one")
#
#     def test_two(self, logger):
#         logger.info("Test two")
