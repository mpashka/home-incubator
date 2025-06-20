https://github.com/grpc/grpc/blob/v1.73.0/examples/protos/route_guide.proto
pip install grpcio==1.16.0rc1 -i https://pypi.yandex-team.ru/simple
pip install grpcio-tools==1.16.0rc1 -i https://pypi.yandex-team.ru/simple


python -m grpc_tools.protoc --proto_path=proto \
  --python_out=python --grpc_python_out=python \
  proto/route_guide.proto

pip install .
