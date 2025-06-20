from setuptools import setup, find_packages
import os

# import pathlib
#
# here = pathlib.Path(__file__).parent.resolve()
# long_description = (here / "readme.txt").read_text(encoding="utf-8")


def read_file(filename):
    path = os.path.join(os.path.abspath(os.path.dirname(__file__)), filename)
    with open(path) as f:
        return f.read()

long_description = read_file('readme.txt')

setup(
    name='test_grpc_lib',
    version='0.8.0',
    description='Test grpc client library',
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/pypa/sampleproject",
    author='Pasha i Masha',
    author_email='pasha@team.ru',
    classifiers=[
        "Development Status :: 3 - Alpha",
        "Intended Audience :: Developers",
        "Topic :: Software Development :: Build Tools",
        "License :: OSI Approved :: MIT License",
        "Programming Language :: Python :: 2",
    ],
    keywords="grpc, protobuf",
    # packages=find_packages(where="src"),
    packages=['test_grpc', ],
    package_dir={'test_grpc': 'python'},
    python_requires=">=2.7, <4",
    install_requires=["grpcio >= 1.16.0rc1"],
    zip_safe=False,
)
