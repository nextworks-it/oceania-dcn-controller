from argparse import ArgumentParser
from datetime import timedelta
from enum import Enum
from statistics import mean, stdev
from threading import Condition
from time import sleep

from os.path import isfile, exists

from datetime import datetime

from genericlogparser import (
    LifeCycle,
    LifeCycleObjFactory,
    LifeCycledObject,
    Task,
    SpringBaseParser
)


class PathStates(Enum):
    PLACEHOLDER = 1
    PROCESSING = 2
    DONE = 7


class PathEvents(Enum):
    GOT_REQUEST = 1
    DONE = 6


class TrafficStates(Enum):
    PLACEHOLDER = 1
    PROCESSING = 2
    DONE = 7


class TrafficEvents(Enum):
    GOT_REQUEST = 1
    DONE = 6


class ComputingStates(Enum):
    PLACEHOLDER = 1
    PROCESSING = 2
    DONE = 7


class ComputingEvents(Enum):
    GOT_REQUEST = 1
    DONE = 6


class TranslatingStates(Enum):
    PLACEHOLDER = 1
    PROCESSING = 2
    DONE = 7


class TranslatingEvents(Enum):
    GOT_REQUEST = 1
    DONE = 6


class PushStates(Enum):
    PLACEHOLDER = 1
    PROCESSING = 2
    DONE = 7


class PushEvents(Enum):
    GOT_REQUEST = 1
    DONE = 6


path_life_cycle = LifeCycle('path_lc',
                            states=PathStates,
                            events=PathEvents,
                            transitions={
                                ('PLACEHOLDER', 'GOT_REQUEST'): 'PROCESSING',
                                ('PROCESSING', 'DONE'): 'DONE'
                            })

traffic_life_cycle = LifeCycle('traffic_lc',
                               states=TrafficStates,
                               events=TrafficEvents,
                               transitions={
                                   ('PLACEHOLDER', 'GOT_REQUEST'): 'PROCESSING',
                                   ('PROCESSING', 'DONE'): 'DONE'
                               })

compute_life_cycle = LifeCycle('compute_lc',
                               states=ComputingStates,
                               events=ComputingEvents,
                               transitions={
                                   ('PLACEHOLDER', 'GOT_REQUEST'): 'PROCESSING',
                                   ('PROCESSING', 'DONE'): 'DONE'
                               })

translation_life_cycle = LifeCycle('translation_lc',
                                   states=TranslatingStates,
                                   events=TranslatingEvents,
                                   transitions={
                                       ('PLACEHOLDER', 'GOT_REQUEST'): 'PROCESSING',
                                       ('PROCESSING', 'DONE'): 'DONE'
                                   })

push_life_cycle = LifeCycle('push_lc',
                            states=PushStates,
                            events=PushEvents,
                            transitions={
                                ('PLACEHOLDER', 'GOT_REQUEST'): 'PROCESSING',
                                ('PROCESSING', 'DONE'): 'DONE'
                            })


class PathObject(LifeCycledObject):
    def get_time(self):
        try:
            return self.get_delta(PathEvents.GOT_REQUEST, PathEvents.DONE)
        except ValueError as e:
            print("Illegal object {}: {}.".format(self.id, e))
            return None


class TrafficObject(LifeCycledObject):
    def get_time(self):
        try:
            return self.get_delta(TrafficEvents.GOT_REQUEST, TrafficEvents.DONE)
        except ValueError as e:
            print("Illegal object {}: {}.".format(self.id, e))
            return None


class ComputeObject(LifeCycledObject):
    def get_time(self):
        try:
            return self.get_delta(ComputingEvents.GOT_REQUEST, ComputingEvents.DONE)
        except ValueError as e:
            print("Illegal object {}: {}.".format(self.id, e))
            return None


class TranslateObject(LifeCycledObject):
    def get_time(self):
        try:
            return self.get_delta(TranslatingEvents.GOT_REQUEST, TranslatingEvents.DONE)
        except ValueError as e:
            print("Illegal object {}: {}.".format(self.id, e))
            return None


class PushObject(LifeCycledObject):
    def get_time(self):
        try:
            return self.get_delta(PushEvents.GOT_REQUEST, PushEvents.DONE)
        except ValueError as e:
            print("Illegal object {}: {}.".format(self.id, e))
            return None


def make_parser(path_factory: LifeCycleObjFactory,
                traffic_factory: LifeCycleObjFactory,
                compute_factory: LifeCycleObjFactory,
                translate_factory: LifeCycleObjFactory,
                push_factory: LifeCycleObjFactory):
    odl_parser = SpringBaseParser()

    # Paths handlers
    odl_parser.add_handler(path_factory.make_handler(
        "Service request received. ID: (?P</id/>[a-zA-Z\d:_-]+)\.",
        PathEvents.GOT_REQUEST
    ))

    odl_parser.add_handler(path_factory.make_handler(
        "Established service: (?P</id/>[a-zA-Z\d:_-]+)\.",
        PathEvents.DONE
    ))

    # Traffic handlers
    odl_parser.add_handler(traffic_factory.make_handler(
        'Starting Traffic matrix computation: OpId (?P</id/>[a-zA-Z\d:_-]+)\.',
        TrafficEvents.GOT_REQUEST
    ))

    odl_parser.add_handler(traffic_factory.make_handler(
        "Got traffic matrix. OpId: (?P</id/>[a-zA-Z\d:_-]+)\.",
        TrafficEvents.DONE
    ))

    # Compute handlers
    odl_parser.add_handler(compute_factory.make_handler(
        "Posting traffic matrix. OpId: (?P</id/>[a-zA-Z\d:_-]+)\.",
        ComputingEvents.GOT_REQUEST
    ))

    odl_parser.add_handler(compute_factory.make_handler(
        "Got network allocation. OpId: (?P</id/>[a-zA-Z\d:_-]+)\.",
        ComputingEvents.DONE
    ))

    # Translate handlers
    odl_parser.add_handler(translate_factory.make_handler(
        "Translating inventory. OpId: (?P</id/>[a-zA-Z\d:_-]+)\.",
        TranslatingEvents.GOT_REQUEST
    ))

    odl_parser.add_handler(translate_factory.make_handler(
        "Got inventory. OpId: (?P</id/>[a-zA-Z\d:_-]+)\.",
        TranslatingEvents.DONE
    ))

    # Push handlers
    odl_parser.add_handler(push_factory.make_handler(
        "Sending inventory. OpId: (?P</id/>[a-zA-Z\d:_-]+)\.",
        PushEvents.GOT_REQUEST
    ))

    odl_parser.add_handler(push_factory.make_handler(
        "Inventory pushed. OpId: (?P</id/>[a-zA-Z\d:_-]+)\.",
        PushEvents.DONE
    ))

    return odl_parser


stop_condition = Condition()  # type: Condition


def kill():
    global stop_condition  # type: Condition
    stop_condition.acquire()
    stop_condition.notify_all()
    print('Stopping parser')
    stop_condition.release()


def main(output_file: str, log_file: str):
    path_factory = LifeCycleObjFactory(path_life_cycle, lc_obj_subclass=PathObject)
    traffic_factory = LifeCycleObjFactory(traffic_life_cycle, lc_obj_subclass=TrafficObject)
    compute_factory = LifeCycleObjFactory(compute_life_cycle, lc_obj_subclass=ComputeObject)
    translate_factory = LifeCycleObjFactory(translation_life_cycle, lc_obj_subclass=TranslateObject)
    push_factory = LifeCycleObjFactory(push_life_cycle, lc_obj_subclass=PushObject)
    global stop_condition

    if not exists(output_file):
        with open(output_file, 'w') as f:
            f.write('errors,'
                    'paths,'
                    'path_avg,'
                    'path_max,'
                    'path_min,'
                    'path_std,'
                    'traffic_matrices,'
                    'traffic_avg,'
                    'traffic_max,'
                    'traffic_min,'
                    'traffic_std,'
                    'computations,'
                    'computation_avg,'
                    'computation_max,'
                    'computation_min,'
                    'computation_std,'
                    'translations,'
                    'translation_avg,'
                    'translation_max,'
                    'translation_min,'
                    'translation_std,'
                    'pushed_schedules,'
                    'push_avg,'
                    'push_max,'
                    'push_min,'
                    'push_std\n')

    if not isfile(output_file):
        print('ERROR: {} exists and is not a file.')
        raise SystemExit()

    affinity_parser = make_parser(path_factory,
                                  traffic_factory,
                                  compute_factory,
                                  translate_factory,
                                  push_factory)

    task = Task(log_file,
                affinity_parser.parse_line)

    task.start()

    stop_condition.acquire()
    stop_condition.wait()

    # Probably not needed, but meh...
    stop_condition.release()

    try:
        task.stop()
    except:
        print('Already closed.')

    sleep(5)

    print('\n************************')
    print('Path factory:')
    for x, y in path_factory.repo.items():
        print(x)
        print(y)

    print('\n************************')
    print('Traffic factory:')
    for x, y in traffic_factory.repo.items():
        print(x)
        print(y)

    print('\n************************')
    print('Compute factory:')
    for x, y in compute_factory.repo.items():
        print(x)
        print(y)

    print('\n************************')
    print('Translate factory:')
    for x, y in translate_factory.repo.items():
        print(x)
        print(y)

    print('\n************************')
    print('Push factory:')
    for x, y in push_factory.repo.items():
        print(x)
        print(y)

    paths = []
    traffic = []
    compute = []
    translate = []
    push = []
    errors = []

    for x in path_factory.repo.values():
        time = x.get_time()
        if time is None:
            errors.append(x.id)
            continue
        paths.append(time.total_seconds())

    for x in traffic_factory.repo.values():
        time = x.get_time()
        if time is None:
            errors.append(x.id)
            continue
        traffic.append(time.total_seconds())

    for x in compute_factory.repo.values():
        time = x.get_time()
        if time is None:
            errors.append(x.id)
            continue
        compute.append(time.total_seconds())

    for x in translate_factory.repo.values():
        time = x.get_time()
        if time is None:
            errors.append(x.id)
            continue
        translate.append(time.total_seconds())

    for x in push_factory.repo.values():
        time = x.get_time()
        if time is None:
            errors.append(x.id)
            continue
        push.append(time.total_seconds())

    if len(paths) == 0:
        path_no = 0
        path_avg = 'N/A'
        path_max = 'N/A'
        path_min = 'N/A'
        path_std = 'N/A'
    else:
        path_no = len(paths)
        path_avg = mean(paths)
        path_max = max(paths)
        path_min = min(paths)
        path_std = stdev(paths)

    if len(traffic) == 0:
        traffic_no = 0
        traffic_avg = 'N/A'
        traffic_max = 'N/A'
        traffic_min = 'N/A'
        traffic_std = 'N/A'
    else:
        traffic_no = len(traffic)
        traffic_avg = mean(traffic)
        traffic_max = max(traffic)
        traffic_min = min(traffic)
        traffic_std = stdev(traffic)

    if len(compute) == 0:
        compute_no = 0
        compute_avg = 'N/A'
        compute_max = 'N/A'
        compute_min = 'N/A'
        compute_std = 'N/A'
    else:
        compute_no = len(compute)
        compute_avg = mean(compute)
        compute_max = max(compute)
        compute_min = min(compute)
        compute_std = stdev(compute)

    if len(translate) == 0:
        translate_no = 0
        translate_avg = 'N/A'
        translate_max = 'N/A'
        translate_min = 'N/A'
        translate_std = 'N/A'
    else:
        translate_no = len(translate)
        translate_avg = mean(translate)
        translate_max = max(translate)
        translate_min = min(translate)
        translate_std = stdev(translate)

    if len(push) == 0:
        push_no = 0
        push_avg = 'N/A'
        push_max = 'N/A'
        push_min = 'N/A'
        push_std = 'N/A'
    else:
        push_no = len(push)
        push_avg = mean(push)
        push_max = max(push)
        push_min = min(push)
        push_std = stdev(push)

    # Write results line
    with open(output_file, 'a') as f:
        f.write(','.join([

            str(len(errors)),

            str(path_no),
            str(path_avg),
            str(path_max),
            str(path_min),
            str(path_std),

            str(traffic_no),
            str(traffic_avg),
            str(traffic_max),
            str(traffic_min),
            str(traffic_std),

            str(compute_no),
            str(compute_avg),
            str(compute_max),
            str(compute_min),
            str(compute_std),

            str(translate_no),
            str(translate_avg),
            str(translate_max),
            str(translate_min),
            str(translate_std),

            str(push_no),
            str(push_avg),
            str(push_max),
            str(push_min),
            str(push_std),

        ]) + '\n')


if __name__ == '__main__':
    p = ArgumentParser()
    p.add_argument('-C', '--csv', help='Path of the results csv file', type=str, default='results.csv')
    p.add_argument('-S', '--odl-file', help='Path of the ODL log file (in the remote machine)', type=str,
                   default='/home/nextworks/distribution-karaf-0.4.2-Beryllium-SR2/data/log/karaf.log')
    args = p.parse_args()
    main(
        args.csv,
        args.odl_file
    )
