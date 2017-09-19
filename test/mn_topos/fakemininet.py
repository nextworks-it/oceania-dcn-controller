from NEPHELE_mininet_gw import build_net, gw_pods


class FakeNode(object):

    def __init__(self, name):
        self.name = name

    def addIntf(self, *a, **ka):
        print('Called addIntf({}, {})'.format(', '.join([str(x) for x in a]), ka))

    def cmd(self, *a, **ka):
        print('Called cmd({}, {})'.format(', '.join([str(x) for x in a]), ka))


class FakeMininet(object):

    def addSwitch(self, name):
        print('Adding switch {}.'. format(name))
        return FakeNode(name)

    def addLink(self, s1, s2, i1=None, i2=None):
        if s1 is None or s2 is None:
            print('aha!')
        print('Linking switches {} and {}{}{}.'.format(
            s1.name,
            s2.name,
            '. s1 interface is {}'.format(i1) if i1 is not None else '',
            '. s2 interface is {}'.format(i2) if i1 is not None else ''
        ))

    def addHost(self, name, ip=None, mac=None):
        print('Adding host {}{}{}.'.format(
            name,
            '. ip is {}'.format(ip) if ip is not None else '',
            '. mac is {}'.format(mac) if mac is not None else ''
        ))
        return FakeNode(name)


def test1intf():
    return build_net(FakeMininet(), ['ens4'])


def test3intf():
    build_net(FakeMininet(), ['ens4', 'ens5', 'ens6'])


if __name__ == '__main__':
    print(test1intf())
    # print(test3intf())
