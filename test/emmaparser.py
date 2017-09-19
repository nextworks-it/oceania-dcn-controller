from argparse import ArgumentParser
from datetime import timedelta
from enum import Enum
from functools import partial
from threading import Condition
from time import sleep
from typing import Dict, Callable, Optional, Union

from os.path import isfile, exists

from genericlogparser import (
    LifeCycle,
    LifeCycleObjFactory,
    LifeCycleHandler,
    LifeCycledObject,
    RemoteTask,
    EMMAParser,
    ODLParser,
    LogParser,
    History
)


class PathStates(Enum):
    PLACEHOLDER = 1
    PROCESSING = 2
    TRAFFIC_MATRIX = 3
    COMPUTING = 4
    TRANSLATING = 5
    PUSHING = 6
    DONE = 7


class PathEvents(Enum):
    GOT_REQUEST = 1
    SENT_TO_TM_ENGINE = 2
    SENT_TO_COMPUTING = 3
    COMPUTING_DONE = 4
    TRANSLATION_DONE = 5
    PUSHED = 6


path_life_cycle = LifeCycle('path_lc',
                            states=PathStates,
                            events=PathEvents,
                            transitions={
                                ('PLACEHOLDER', 'GOT_REQUEST'): 'PROCESSING',
                                ('PROCESSING', 'SENT_TO_TM_ENGINE'): 'TRAFFIC_MATRIX',
                                ('TRAFFIC_MATRIX', 'SENT_TO_COMPUTING'): 'COMPUTING',
                                ('COMPUTING', 'COMPUTING_DONE'): 'TRANSLATING',
                                ('TRANSLATING', 'TRANSLATION_DONE'): 'PUSHING',
                                ('PUSHING', 'PUSHED'): 'DONE',
                            })


class PathObject(LifeCycledObject):
    def get_processing_time(self):
        try:
            return self.get_delta(PathEvents.GOT_REQUEST, PathEvents.SENT_TO_TM_ENGINE)
        except ValueError as e:
            print("Illegal object {}: {}.".format(self.id, e))
            return timedelta(0)

    def get_tm_time(self):
        try:
            return self.get_delta(PathEvents.SENT_TO_TM_ENGINE, PathEvents.SENT_TO_COMPUTING)
        except ValueError as e:
            print("Illegal object {}: {}.".format(self.id, e))
            return timedelta(0)

    def get_computing_time(self):
        try:
            return self.get_delta(PathEvents.SENT_TO_COMPUTING, PathEvents.COMPUTING_DONE)
        except ValueError as e:
            print("Illegal object {}: {}.".format(self.id, e))
            return timedelta(0)

    def get_translating_time(self):
        try:
            return self.get_delta(PathEvents.COMPUTING_DONE, PathEvents.TRANSLATION_DONE)
        except ValueError as e:
            print("Illegal object {}: {}.".format(self.id, e))
            return timedelta(0)

    def get_pushing_time(self):
        try:
            return self.get_delta(PathEvents.TRANSLATION_DONE, PathEvents.PUSHED)
        except ValueError as e:
            print("Illegal object {}: {}.".format(self.id, e))
            return timedelta(0)


def make_odl_parser(path_factory: LifeCycleObjFactory):
    odl_parser = ODLParser()

    # Paths handlers
    odl_parser.add_handler(path_factory.make_handler(
        'Posting traffic matrix.',
        PathEvents.GOT_REQUEST
    ))

    odl_parser.add_handler(path_factory.make_handler(
        '.* Received path request (?P</id/>[a-zA-Z\d:_-]+)',
        PathEvents.GOT_REQUEST
    ))

    odl_parser.add_handler(path_factory.make_handler(
        '.* Received path request (?P</id/>[a-zA-Z\d:_-]+)',
        PathEvents.GOT_REQUEST
    ))

    odl_parser.add_handler(path_factory.make_handler(
        '.* Received path request (?P</id/>[a-zA-Z\d:_-]+)',
        PathEvents.GOT_REQUEST
    ))

    odl_parser.add_handler(path_factory.make_handler(
        '.* Received path request (?P</id/>[a-zA-Z\d:_-]+)',
        PathEvents.GOT_REQUEST
    ))

    odl_parser.add_handler(path_factory.make_handler(
        '.* Connection successfully established (?P</id/>[a-zA-Z\d:_-]+)',
        PathEvents.REQUEST_DONE
    ))
    # END Paths handlers

    return odl_parser


def main(output_file: str, emma_file: str, emma_ip: str, emma_user: str, odl_file: str, odl_ip: str, odl_user: str):
    ns_factory = LifeCycleObjFactory(ns_life_cycle, lc_obj_subclass=NsObj, handler_subclass=NSHandler)
    vm_factory = LifeCycleObjFactory(vm_life_cycle, lc_obj_subclass=VmObj)
    xpu_factory = LifeCycleObjFactory(p_state_life_cycle, lc_obj_subclass=PowerStateObj)
    xpfe_factory = LifeCycleObjFactory(p_state_life_cycle, lc_obj_subclass=PowerStateObj,
                                       history_factory=partial(History, time_format='%Y-%m-%d %H:%M:%S,%f'))
    path_factory = LifeCycleObjFactory(path_life_cycle, lc_obj_subclass=PathObj,
                                       history_factory=partial(History, time_format='%Y-%m-%d %H:%M:%S,%f'))

    if not exists(output_file):
        with open(output_file, 'w') as f:
            f.write('ns_total,'
                    'compute,'
                    'vlink,'
                    'all_create,'
                    'all_config,'
                    'vm_create_avg,'
                    'vm_config_avg,'
                    'xpu_state_avg,'
                    'xpfe_state_avg,'
                    'path_avg\n')

    if not isfile(output_file):
        print('ERROR: {} exists and is not a file.')
        raise SystemExit()

    emma_parser = EMMAParser()

    odl_parser = make_odl_parser(xpfe_factory, path_factory)

    emma_task = RemoteTask(emma_file,
                           emma_parser.parse_line,
                           emma_ip,
                           emma_user)

    stop_condition = Condition()

    def cb(_):
        stop_condition.acquire()
        stop_condition.notify_all()
        stop_condition.release()

    handlers_to_emma_parser(emma_parser, ns_factory, xpu_factory, vm_factory, cb)

    odl_task = RemoteTask(odl_file,
                          odl_parser.parse_line,
                          odl_ip,
                          odl_user)

    emma_task.start()
    odl_task.start()

    # Do what you have to do here (e.g. instantiation calls)

    # When done, we wait for the emma parser to activate the stop_condition

    stop_condition.acquire()
    stop_condition.wait()

    # Probably not needed, but meh...
    stop_condition.release()

    try:
        emma_task.stop()
        odl_task.stop()
    except:
        print('Already closed.')

    sleep(5)

    print('\n************************')
    print('NS factory:')
    for x, y in ns_factory.repo.items():
        print(x)
        print(y)

    print('\n************************')
    print('VM factory:')
    for x, y in vm_factory.repo.items():
        print(x)
        print(y)

    print('\n************************')
    print('XPU factory:')
    for x, y in xpu_factory.repo.items():
        print(x)
        print(y)

    print('\n************************')
    print('XPFE factory:')
    for x, y in xpfe_factory.repo.items():
        print(x)
        print(y)

    print('\n************************')
    print('PATH factory:')
    for x, y in path_factory.repo.items():
        print(x)
        print(y)

    ns_total = timedelta(0)
    compute = timedelta(0)
    vlink = timedelta(0)
    all_create = timedelta(0)
    all_config = timedelta(0)

    vm_create_total = timedelta(0)
    vm_config_total = timedelta(0)
    xpu_state_total = timedelta(0)
    xpfe_state_total = timedelta(0)
    path_total = timedelta(0)

    number_vm_created = 0
    number_vm_conf = 0
    number_xpus = 0
    number_xpfes = 0
    number_paths = 0

    for ns in ns_factory.repo.values():  # type: NsObj
        ns_total += ns.get_total()
        compute += ns.get_computation()
        vlink += ns.get_vlink()
        all_create += ns.get_create()
        all_config += ns.get_configure()

    for vm in vm_factory.repo.values():  # type: VmObj
        tp, time = vm.get_type_and_time()
        if tp == VmStates.CREATED:
            vm_create_total += time
            number_vm_created += 1
        elif tp == VmStates.CONFIGURED:
            vm_config_total += time
            number_vm_conf += 1
        else:
            print('Some weird thing happened. Report at m.capitani[AT]nextworks.it.')

    for xpu in xpu_factory.repo.values():  # type: PowerStateObj
        xpu_state_total += xpu.get_time()
        number_xpus += 1

    for xpfe in xpfe_factory.repo.values():  # type: PowerStateObj
        xpfe_state_total += xpfe.get_time()
        number_xpfes += 1

    for path in path_factory.repo.values():  # type: PathObj
        path_total += path.get_time()
        number_paths += 1

    # Write results line
    with open(output_file, 'a') as f:
        f.write(','.join([
            str(ns_total),
            str(compute),
            str(vlink),
            str(all_create),
            str(all_config),
            str(vm_create_total / number_vm_created),
            str(vm_config_total / number_vm_conf),
            str(xpu_state_total / number_xpus) if number_xpus > 0 else 'N/A',
            str(xpfe_state_total / number_xpfes) if number_xpfes > 0 else 'N/A',
            str(path_total / number_paths) if number_paths > 0 else 'N/A',
        ]) + '\n')

if __name__ == '__main__':
    p = ArgumentParser()
    p.add_argument('-C', '--csv', help='Path of the results csv file', type=str, default='results.csv')
    p.add_argument('-E', '--emma-file', help='Path of the emma file (in the remote machine)', type=str,
                   default='/var/log/emma/NFVO.log')
    p.add_argument('-O', '--emma-ip', help='IP of the orchestrator (emma)', type=str, default='10.5.1.60')
    p.add_argument('-N', '--emma-user', help='Username for emma', type=str, default='nextworks')
    p.add_argument('-S', '--odl-file', help='Path of the ODL log file (in the remote machine)', type=str,
                   default='/home/nextworks/distribution-karaf-0.4.2-Beryllium-SR2/data/log/karaf.log')
    p.add_argument('-A', '--odl-ip', help='IP of the ODL machine', type=str, default='10.5.1.51')
    p.add_argument('-U', '--odl-user', help='Username for ODL', type=str, default='nextworks')
    args = p.parse_args()
    main(
        args.csv,
        args.emma_file,
        args.emma_ip,
        args.emma_user,
        args.odl_file,
        args.odl_ip,
        args.odl_user
    )
