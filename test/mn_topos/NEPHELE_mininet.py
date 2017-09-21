#!/usr/bin/env python
from argparse import ArgumentParser
from itertools import groupby

from mininet.net import Mininet
from mininet.link import TCLink
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.node import RemoteController


# Configuration
from time import sleep

P = 10 		    # Pods
I = 2 		    # Planes
W = 10 		    # Wavelengths ( == ToR per pod)
T = 12 		    # Timeslots
R = 2 		    # Rings
Z = 2   	    # Zones (per ToR)
START_POD = 10      # Smallest pod number in the net


class SlowStartingNet(Mininet):

    def start(self):
        """Start controller and switches."""
        if not self.built:
            self.build()
        info('*** Starting controller\n')
        for controller in self.controllers:
            info(controller.name + ' ')
            controller.start()
        info('\n')
        info('*** Starting %s switches\n' % len(self.switches))
        for i, switch in enumerate(self.switches):
            info(switch.name + ' ')
        started = {}
        for swclass, switches in groupby(
                sorted(self.switches, key=type), type):
            all_switches = tuple(switches)
            while all_switches:
                switches, all_switches = all_switches[:10], all_switches[10:]
                info('*** Switch batch: '
                     + ' '.join((s.name for s in switches))
                     + '\n')
                for switch in switches:
                    switch.start(self.controllers)
                if hasattr(swclass, 'batchStartup'):
                    success = swclass.batchStartup(switches)
                    started.update({s: s for s in success})
                info('\n')
                info('*** Waiting for the switches to connect\n')
                remaining = list(switches)
                while True:
                    info('*** Check switches status')
                    for switch in tuple(remaining):
                        if switch.connected():
                            info('%s connected.\n' % switch)
                            remaining.remove(switch)
                    if remaining:
                        info('*** Remaining: '
                             + ' '.join((s.name for s in remaining))
                             + '\n')
                    else:
                        info('*** Done ***\n')
                        break
                    sleep(.5)
                info('*** Back off for 5 seconds ***\n')
                sleep(5)


def pod_nos():
    for i in range(START_POD, START_POD + P):
        yield i


def build_pods(m_net):
    pods = {}
    for i in range(1, I+1):
        for p in pod_nos():
            # pod_id = (i * P) + p
            pods[(i, p)] = m_net.addSwitch('POD2{0:02}0{1:02}'.format(i, p))
            # indexed by couple (plane, pod)
    return pods


def build_tors(m_net):
    tors = {}
    for p in pod_nos():
        for w in range(1, W+1):
            # tor_id = (p * W) + w
            tors[(p, w)] = m_net.addSwitch('ToR1{0:02}0{1:02}'.format(p, w))
            # indexed by couple (pod, wavelength)
    return tors


def build_zones(m_net):
    zones = {}
    for p in pod_nos():
        for w in range(1, W+1):
            for z in range(1, Z+1):
                zone_id = ((p-1) * (W * Z)) + ((w-1) * Z) + z
                zones[(p, w, z)] = m_net.addHost(
                    'zone{}'.format(zone_id),
                    ip='10.{0}.{1}.{2}'.format(p, w, z),
                    mac='00:04:00:{0:02x}:{1:02x}:{2:02x}'.format(p, w, z)
                )
    return zones


def link_rings(m_net, pods):
    for i in range(1, I+1):
        for p in pod_nos():
            next_pod = p+1
            if next_pod == START_POD + P:
                next_pod = START_POD
            for r in range(1, R+1):
                m_net.addLink(
                    pods[(i, p)],
                    pods[(i, next_pod)],
                    (2*r),
                    (2*r) - 1
                )
                # ring links are port 2*r going forward, port 2*r-1 going back
                # Hence ports from 1 to 2*R are as such:
                # r_1 ||  p-1     eth2    ->   eth1   | p |    eth2    ->   p+1
                # r_2 ||  p-1     eth4    ->   eth3   | p |    eth4    ->   p+1
                # ...
                # r_R ||  p-1     eth2R   ->  eth2R-1 | p |    eth2R   ->   p+1


def link_tors(m_net, pods, tors):
    for p in pod_nos():
        for w in range(1, W+1):
            for i in range(1, I+1):
                m_net.addLink(
                    tors[(p, w)],
                    pods[(i, p)],
                    i,
                    (2*R) + w
                )
                # ports 1 -> I of a tor go to the plane with that number.
                # ports 2R +1 -> 2*R + W of a POD go to the
                # ToR with that wavelength


def link_zones(m_net, tors, zones):
    for p in pod_nos():
        for w in range(1, 1+W):
            for z in range(1, 1+Z):
                m_net.addLink(
                    zones[(p, w, z)],
                    tors[(p, w)],
                    0,
                    I+z
                )
                # ports I + 1 -> I + Z of a ToR go to the
                # innovation zone with that number


def build_net(m_net):
    info("*** Building nodes ***\n")
    zones = build_zones(m_net)
    tors = build_tors(m_net)
    pods = build_pods(m_net)
    info("*** Building links ***\n")
    link_rings(m_net, pods)
    link_tors(m_net, pods, tors)
    link_zones(m_net, tors, zones)

if __name__ == "__main__":
    parser = ArgumentParser(description='Mininet for NIDO.')
    parser.add_argument('-C', '--controller', metavar='ADDRESS', type=str,
                        help='IP of the Oceania Controller for this net')
    args = parser.parse_args()
    controller_ip = args.controller if args.controller is not None \
        else '127.0.0.1'
    setLogLevel('info')
    info('Controller IP is {}'.format(controller_ip))
    # use Linux Traffic Control emulated links
    net = SlowStartingNet(link=TCLink)

    # ODL controller: address=127.0.0.1, port=6633
    c = RemoteController('odl-controller', ip=controller_ip, port=6633)
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
