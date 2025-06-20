
def pytest_addoption(parser):
    group = parser.getgroup('my_tests', 'Settings specific to my test functional testing')
    group.addoption('--log_dir', dest='log_dir', default='./out',
                    help='Directory to which test harness writes its logs')


# def pytest_configure(config):
#     print "PyTest Configuration"
#     print config.__dict__
#     print "-----------"

