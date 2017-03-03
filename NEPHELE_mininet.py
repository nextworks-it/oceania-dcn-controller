#!/usr/bin/env python


from mininet.net import Mininet
from mininet.link import TCLink, Intf
from mininet.cli import CLI
from mininet.log import setLogLevel
from mininet.node import RemoteController


# Configuration

P = 3 		# Pods
I = 2 		# Planes
W = 2 		# Wavelengths ( == ToR per pod)
T = 12 		# Timeslots
R = 2 		# Rings
Z = 3 		# Zones (per ToR)


def build_pods(m_net):
    pods = {}
    for i in range(1, I+1):
        for p in range(1, P+1):
            # pod_id = (i * P) + p
            pods[(i, p)] = m_net.addSwitch('POD2{0:02}0{1:02}'.format(i, p))
            # indexed by couple (plane, pod)
    return pods


def build_tors(m_net):
    tors = {}
    for p in range(1, P+1):
        for w in range(1, W+1):
            # tor_id = (p * W) + w
            tors[(p, w)] = m_net.addSwitch('ToR1{0:02}0{1:02}'.format(p, w))
            # indexed by couple (pod, wavelength)
    return tors


def build_zones(m_net):
    zones = {}
    for p in range(1, P+1):
        for w in range(1, W+1):
            for z in range(1, Z+1):
                zone_id = ((p-1) * (W * Z)) + ((w-1) * Z) + z
                zones[(p, w, z)] = m_net.addHost('zone{}'.format(zone_id),
                                                 ip='10.{0}.{1}.{2}'.format(p, w, z),
                                                 mac='00:04:00:{0:02x}:{1:02x}:{2:02x}'.format(p, w, z))
    return zones


def link_rings(m_net, pods):
    for i in range(1, I+1):
        for p in range(1, P+1):
            for r in range(1, R+1):
                m_net.addLink(pods[(i, p)], pods[(i, (p % P) + 1)], (2*r)-1, 2*r)
                # ring links are port 2*r going forward, port 2*r-1 going back
                # Hence ports from 1 to 2*R are as such:
                # r_1 ||  p-1     eth2    ->   eth1   | p |    eth2    ->   p+1
                # r_2 ||  p-1     eth4    ->   eth3   | p |    eth4    ->   p+1
                # ...
                # r_R ||  p-1     eth2R   ->  eth2R-1 | p |    eth2R   ->   p+1


def link_tors(m_net, pods, tors):
    for p in range(1, P+1):
        for w in range(1, W+1):
            for i in range(1, I+1):
                m_net.addLink(tors[(p, w)], pods[(i, p)], i, (2*R) + w)
                # ports 1 -> I of a tor go to the plane with that number.
                # ports 2R +1 -> 2*R + W of a POD go to the ToR with that wavelength


def link_zones(m_net, tors, zones):
    for p in range(1, 1+P):
        for w in range(1, 1+W):
            for z in range(1, 1+Z):
                print("Linking zone {0} {1} {2} with tor {0} {1}".format(p, w, z))
                m_net.addLink(zones[(p, w, z)], tors[(p, w)], 0, I+z)
                # ports I + 1 -> I + Z of a ToR go to the innovation zone with that number


def build_net(m_net):
    zones = build_zones(m_net)
    tors = build_tors(m_net)
    pods = build_pods(m_net)
    link_rings(m_net, pods)
    link_tors(m_net, pods, tors)
    link_zones(m_net, tors, zones)

if __name__ == "__main__":
    setLogLevel('info')
    # use Linux Traffic Control emulated links
    net = Mininet(link=TCLink)

    # ODL controller: address=127.0.0.1, port=6633
    c = RemoteController('odl-controller', ip="127.0.0.1", port=6633)
    net.addController(c)

    # start
    build_net(net)
    net.build()
    net.staticArp()
    net.start()

    # cli
    CLI(net)

    # clean-up
    net.stop()
