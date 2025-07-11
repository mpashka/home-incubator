import logging
import time

import grpc
from test_grpc import route_guide_pb2_grpc, route_guide_pb2
from concurrent import futures


# noinspection PyPep8Naming
class GrpcServiceMock(route_guide_pb2_grpc.RouteGuideStub):
    def __init__(self, port, log_path):
        # super().__init__(channel)
        self.logger = self._create_logger(log_path)
        self.server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
        self.port = port
        self.feature_ = route_guide_pb2.Feature(
            name="",
            location=route_guide_pb2.Point(latitude=0, longitude=0))

    @staticmethod
    def _create_logger(log_path):
        logger = logging.getLogger(__name__)
        # self.logger.setLevel(logging.DEBUG)
        formatter = logging.Formatter('%(asctime)s %(levelname)s %(funcName)s: %(message)s')

        fh = logging.FileHandler(log_path)
        fh.setLevel(logging.DEBUG)
        fh.setFormatter(formatter)
        logger.addHandler(fh)
        return logger

    def set_feature(self, feature):
        self.feature_ = feature

    def start_server(self):
        route_guide_pb2_grpc.add_RouteGuideServicer_to_server(self, self.server)
        self.server.add_insecure_port("[::]:50051")
        self.server.start()
        self.logger.info("Server started")
        # server.wait_for_termination()

    def GetFeature(self, request, context):
        self.logger.info("Server received call GetFeature(point={point})".format(point=request))
        return self.feature_

    def ListFeatures(self, request, context):
        yield self.feature_

    def RecordRoute(self, request_iterator, context):
        return route_guide_pb2.RouteSummary(
            point_count=2,
            feature_count=3,
            distance=int(4),
            elapsed_time=int(5),
        )

    def RouteChat(self, request_iterator, context):
        self.logger.info("Route chat")
        yield route_guide_pb2.RouteNote(
            location = route_guide_pb2.Point(latitude = 1,longitude = 2),
            message = "Hello world"
        )