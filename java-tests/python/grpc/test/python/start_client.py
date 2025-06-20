import grpc
from test_grpc import route_guide_pb2_grpc, route_guide_pb2


def test_grpc(logger):
    # feature = route_guide_pb2.Feature(name="defined_feature", location=route_guide_pb2.Point(latitude=10, longitude=20))
    # grpc_mock.set_feature(feature)

    channel = grpc.insecure_channel('localhost:50051')
    stub = route_guide_pb2_grpc.RouteGuideStub(channel)
    point = route_guide_pb2.Point(latitude = 1, longitude = 2)
    logger.info("Call server with {point}".format(point=point))
    feature = stub.GetFeature(point)
    logger.info("Server response with {feature}".format(feature=feature))
    channel.close()
