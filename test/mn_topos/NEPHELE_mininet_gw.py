#!/usr/bin/env python
from argparse import ArgumentParser
from itertools import groupby

from mininet.net import Mininet
from mininet.link import TCLink, Intf
from mininet.cli import CLI
from mininet.log import setLogLevel, info, error
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


def pod_nos():
    for i in range(START_POD, START_POD + P):
        yield i


def gw_pods():
    return {pod: index for index, pod in enumerate([pod_no for pod_no in pod_nos()][-3:])}
    # This is a dict {N-2: 0, N-1: 1, N: 2} where N is the last pod number


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
                    info('*** Check switches status\n')
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
    fws = {}
    for p in pod_nos():
        for w in range(1, W+1):
            if False:  # p in gw_pods() and w == W:
                # This is a gateway, not a common TOR, so we build a 'firewall'
                fws[gw_pods()[p]] = m_net.addSwitch('FW9{}'.format(gw_pods()[p]))
            else:
                for z in range(1, Z+1):
                    zone_id = ((p-1) * (W * Z)) + ((w-1) * Z) + z
                    temp = m_net.addHost(
                        'zone{}'.format(zone_id),
                        ip='10.{0}.{1}.{2}'.format(p, w, z),
                        mac='00:04:00:{0:02x}:{1:02x}:{2:02x}'.format(p, w, z)
                    )
                    zones[(p, w, z)] = temp

    return zones, fws


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
                    (2*r)-1,
                    2*r
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


def link_zones(m_net, tors, zones, fws):
    for p in pod_nos():
        for w in range(1, 1+W):
            if False:  # p in gw_pods() and w == W:
                m_net.addLink(
                    tors[(p, w)],
                    fws[gw_pods()[p]],
                    I+1,
                    1
                )
            else:
                for z in range(1, 1+Z):
                    m_net.addLink(
                        zones[(p, w, z)],
                        tors[(p, w)],
                        0,
                        I+z
                    )
                    # ports I + 1 -> I + Z of a ToR go to the
                    # innovation zone with that number


def bind_fws(fws, intfs):
    intfs_no = len(intfs)
    for i in range(intfs_no):
        Intf(intfs[i], node=fws[i])


def build_net(m_net, intfs):
    info("*** Building nodes ***\n")
    zones, fws = build_zones(m_net)
    tors = build_tors(m_net)
    pods = build_pods(m_net)
    info("*** Building links ***\n")
    link_rings(m_net, pods)
    link_tors(m_net, pods, tors)
    link_zones(m_net, tors, zones, fws)
    info("*** Binding access nodes ***\n")
    # bind_fws(fws, intfs)
    return fws


def configure_zones(zones):
    for (p, w, z) in zones:
        zone = zones[(p, w, z)]
        zone.setARP('10.{}.255.1'.format(p), '0a:0b:0c:0d:0e:0f')
        zone.cmd('route add -net 10.0.0.0/8 gateway 10.{}.255.1'.format(p))


if __name__ == "__main__":
    parser = ArgumentParser(description='Mininet for NIDO.')
    parser.add_argument('-C', '--controller', metavar='ADDRESS', type=str,
                        help='IP of the Oceania Controller for this net')
    # parser.add_argument('-I', '--interfaces', metavar='INTERFACES', type=str,
    #                     help='Comma separated list of up to 3 interfaces to connect'
    #                          'with the emulated DCN')
    parser.add_argument('-P', '--pod', metavar='POD', type=int, default=10,
                        help='Number of first pod of the DCN')
    args = parser.parse_args()

    setLogLevel('info')

    START_POD = args.pod
    controller_ip = args.controller if args.controller is not None \
        else '127.0.0.1'
    if args.controller is None:
        info('***Warning: Falling back to localhost controller.\n')
    # interfaces = args.interfaces.split()
    # if len(interfaces) > 3:
    #     error('Too many interfaces provided. Expected up to 3.\n')
    #     exit(1)
    info('Controller IP is {}\n'.format(controller_ip))
    # use Linux Traffic Control emulated links
    net = SlowStartingNet(link=TCLink)

    # ODL controller: address=127.0.0.1, port=6633
    c = RemoteController('odl-controller', ip=controller_ip, port=6633)
    net.addController(c)

    # start
    accesses = build_net(net, None)
    net.build()
    net.start()
    # info("*** Take care to configure the access nodes {}. ***\n"
    #      .format(" ".join((a_n.name for a_n in accesses.values()))))

    # configure routes and fake ARP entries
    # configure_zones(zs)

    # cli
    CLI(net)

    # clean-up
    net.stop()
