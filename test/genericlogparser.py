from abc import ABCMeta, abstractmethod
from enum import Enum, EnumMeta
from functools import partial
from io import StringIO
from threading import Thread
from typing import Optional, Callable, Dict, List, Tuple, Union, TextIO, Type

from datetime import datetime

import re
import subprocess
from os.path import isfile


class LifeCycle(object):

    def __init__(self, name: str, states: EnumMeta, events: EnumMeta, transitions: Dict[Tuple[str, str], str]):
        if name is None:
            raise ValueError("Name must not be none.")
        if states is None or len(states) == 0:
            raise ValueError("List of states must be non empty.")
        if events is None or len(events) == 0:
            raise ValueError("List of events must be non empty.")
        if transitions is None or len(transitions) == 0:
            raise ValueError("List of transitions must be non empty.")
        self.name = name
        # noinspection PyArgumentList
        self.states = states
        # noinspection PyArgumentList
        self.events = events

        self.starting_status = self.states(1)

        self.transitions = {}
        for (current_state, event), new_state in transitions.items():
            c_s = self.get_state(current_state)
            n_s = self.get_state(new_state)
            ev = self.get_event(event)
            self.transitions[(c_s, ev)] = n_s

    def get_state(self, item):
        if isinstance(item, self.states):
            return item
        try:
            state = self.states[item]
            return state
        except KeyError as e:
            raise ValueError("State {} not found in lifecycle {}.".format(e.args[0], self.name))

    def get_event(self, item):
        if isinstance(item, self.events):
            return item
        try:
            ev = self.events[item]
            return ev
        except KeyError as e:
            raise ValueError("Event {} not found in lifecycle {}.".format(e.args[0], self.name))

    def get_next_state(self, event, state, strict: bool = False):

        if isinstance(state, str):
            state = self.get_state(state)
        if not isinstance(state, self.states):
            raise ValueError("State parameter should be an event or name of event.")

        if isinstance(event, str):
            event = self.get_event(event)
        if not isinstance(event, self.events):
            raise ValueError("Event parameter should be an event or name of event.")

        new_state = self.transitions.get((state, event), None)
        if new_state is None:
            if strict:
                raise ValueError("No transition found for state {} and event {}.".format(state, event))
            else:
                new_state = state
        return new_state


class History(metaclass=ABCMeta):

    def __init__(self, time_format: str):
        self.format = time_format
        self.event_list = []

    def append(self, item, time):
        time = self._check_time(time)
        self._append(item, time)

    def _check_time(self, time):
        if isinstance(time, str):
            time = datetime.strptime(time, self.format)
        if not isinstance(time, datetime):
            raise ValueError("Time argument must be a date, either in string or datetime format.")
        return time

    def __iter__(self):
        yield from self.event_list

    def _append(self, item, time):
        self.event_list.append((item, time))

    def __str__(self):
        return "\n\t".join(["History:"] + [str(x) for x in self.event_list])


class LifeCycledObject(object):

    def __init__(self, obj_id: str, life_cycle: LifeCycle, history: History):
        self.id = obj_id
        self.life_cycle = life_cycle
        self.status = self.life_cycle.starting_status
        self.history = history

    def accept_event(self, event, time):
        ev = self.life_cycle.get_event(event)
        new_status = self.life_cycle.get_next_state(ev, self.status)
        self.history.append(event, time)
        self.status = new_status

    def get_delta(self, start_event, end_event):
        start_time = None
        end_time = None
        s_ev = self.life_cycle.get_event(start_event)
        e_ev = self.life_cycle.get_event(end_event)
        for event, time in self.history:
            if event == s_ev:
                if start_time is not None:
                    raise ValueError('More than one event of type {}'.format(s_ev))
                start_time = time
            if event == e_ev:
                if end_time is not None:
                    raise ValueError('More than one event of type {}'.format(e_ev))
                end_time = time
        if start_time is None:
            raise ValueError('Start event {} not happened'.format(start_event))
        if end_time is None:
            raise ValueError('End event {} not happened'.format(end_event))
        if end_time < start_time:
            raise ValueError('End event {} happened before start event {}'.format(end_event, start_event))
        return end_time - start_time

    def __str__(self):
        return "LC_obj id = {}\n" \
               "{}".format(self.id, self.history)


class LogParser(metaclass=ABCMeta):

    def __init__(self, base_pattern: str, miss_handler: Optional['Handler'] = None):
        self.compiled = False
        if miss_handler is None:
            miss_handler = SimpleHandler("", lambda x: None)
        self.miss_handler = miss_handler
        self.reaction_dict = {}
        self._compiled_dict = {}
        self._good_line_pattern = None
        self._base = base_pattern

    @staticmethod
    def get_matches(match) -> Dict[str, str]:
        if not match:
            return {}
        return match.groupdict()

    def add_handler(self, handler: 'Handler'):
        self._add_reaction(handler.pattern, handler)

    def _add_reaction(self, pattern: Optional[str], action: Callable[[Dict[str, str]], None]) -> None:
        self.reaction_dict[pattern] = action

    def parse_line(self, line: str) -> None:
        if not self.compiled:
            self._compile()
        action, arguments = self._is_matching(line)
        action(arguments)

    def _is_matching(self, line: str) -> Tuple[Callable[[Dict[str, str]], None], Dict[str, str]]:
        if not self.compiled:
            raise ValueError("Parser not yet compiled.")
        match = self._good_line_pattern.match(line)
        return self._get_matching_pattern(match)

    def _get_matching_pattern(self, match) -> Tuple[Callable[[Dict[str, str]], None], Dict[str, str]]:
        if match is None:  # Invoke handler for table miss
            return self.miss_handler, {}
        for pattern, action in self._compiled_dict.items():
            if pattern.search(match.group(2)):
                return action, self.get_matches(match)
        else:
            raise ValueError('General pattern matched, but no single pattern did. State dump: {}.'
                             .format(self.__dict__)
                             )

    def _compile(self) -> None:
        self.compiled = True
        self._good_line_pattern = re.compile(self._general_pattern())
        print('General pattern: ' + self._general_pattern())
        for pattern, action in self.reaction_dict.items():
            print('Sub pattern: ' + pattern)
            self._compiled_dict[re.compile(pattern)] = action

    def _general_pattern(self) -> str:
        return self._base + "(" + "|".join(self.reaction_dict.keys()) + ")"


class Handler(metaclass=ABCMeta):

    def __init__(self, pattern: str):
        self.pattern = pattern
        self.handled_messages = 0

    def __call__(self, match_dict: Dict[str, str]):
        self.callback(match_dict)
        self.handled_messages += 1

    @abstractmethod
    def callback(self, _: Dict[str, str]):
        """Override in subclasses to set a default action"""
        pass


class ListAppendingHandler(Handler):

    def __init__(self, pattern: str, record_list: list):
        super(ListAppendingHandler, self).__init__(pattern)
        self.list = record_list

    def callback(self, d: Dict[str, str]):
        self.list.append(d)


class SimpleHandler(Handler):

    def __init__(self, pattern: str, callback: Callable):
        super(SimpleHandler, self).__init__(pattern)
        self.action = callback

    def callback(self, match_dict: Dict[str, str]):
        self.action(match_dict)


class LifeCycleHandler(Handler):

    def __init__(self,
                 pattern: str,
                 path_tag: str,
                 factory: 'LifeCycleObjFactory',
                 event: Union[str, Enum],
                 side_effects: Optional[Callable[[Dict[str, str]], None]] = None):
        def base_action(match_dict: Dict[str, str]):
            path_id = match_dict[path_tag]
            path_history = factory.fetch(path_id)
            path_history.accept_event(event, match_dict["time"])
            print("Got event: {}.".format(event))

        if side_effects is not None:
            def action(match_dict: Dict[str, str]):
                base_action(match_dict)
                side_effects(match_dict)
        else:
            action = base_action

        self.action = action

        super(LifeCycleHandler, self).__init__(pattern)

    def callback(self, d: Dict[str, str]):
        self.action(d)


class LifeCycleObjFactory(object):

    def __init__(self, life_cycle: LifeCycle,
                 history_factory: Optional[Callable[[], History]] = None,
                 lc_obj_subclass=None,
                 handler_subclass=None):
        """
        :type lc_obj_subclass: Optional[Type['LifeCycledObject']]
        :type handler_subclass: Optional[Type['LifeCycleHandler']]
        """
        self.life_cycle = life_cycle
        self.repo = {}
        self.history_factory = history_factory if history_factory is not None \
            else partial(History, time_format='%Y-%m-%d %H:%M:%S.%f')
        self.lc_factory = lc_obj_subclass if lc_obj_subclass is not None else LifeCycledObject
        self.handler_factory = handler_subclass if handler_subclass is not None else LifeCycleHandler

    def _make(self, obj_id):
        return self.lc_factory(obj_id, self.life_cycle, self.history_factory())

    def make_handler(self,
                     pattern: str,
                     event: Union[str, Enum],
                     side_effects: Optional[Callable[[Dict[str, str]], None]] = None):
        if pattern.find('/id/') == -1:
            raise ValueError('The pattern specified must contain the tag "/id/".')
        real_pattern = pattern.replace('/id/', self.life_cycle.name + '_' + event.name + '_' + 'id')
        return self.handler_factory(
            real_pattern,
            self.life_cycle.name + '_' + event.name + '_' + 'id',
            self,
            event,
            side_effects
        )

    def fetch(self, obj_id: str):
        return self.repo.setdefault(obj_id, self._make(obj_id))


class Reader(object):

    def __init__(self, file_like: Union[TextIO, StringIO]):
        self.file = file_like

    def start(self):
        while True:
            line = self.file.readline()
            if line == "":
                raise StopIteration()
            yield line


class Task(object):

    def __init__(self, file, line_callback):
        self.thread = Thread(target=self)
        self.results = {}
        self._file_check(file)
        self.file = file
        self.process = None  # type: Optional[subprocess.Popen]
        self.callback = line_callback

    def _file_check(self, file):
        if not isfile(file):
            raise ValueError("Not a file: {}.".format(file))

    def start(self):
        self.thread.start()

    def stop(self):
        self.process.kill()
        self.thread.join()

    def __call__(self):

        self.process = subprocess.Popen(
            ['tail', '-F', self.file],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            universal_newlines=True)

        for l in Reader(self.process.stdout).start():
            self.callback(l)


class RemoteTask(Task):

    def __init__(self, file, line_callback, ip_address, username=None):
        self.coordinates = username + '@' + ip_address if username else ip_address
        super(RemoteTask, self).__init__(file, line_callback)

    def _file_check(self, file):
        print('I hope file {} exists on {}...'.format(file, self.coordinates))

    def __call__(self):

        self.process = subprocess.Popen(
            ['ssh', self.coordinates, 'tail', '-F', self.file],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            universal_newlines=True)

        for l in Reader(self.process.stdout).start():
            self.callback(l)


class ODLParser(LogParser):

    def __init__(self):

        super(ODLParser, self).__init__(
            r'^(?P<time>[0-9]{4}-[0-9]{2}-[0-9]{2} \d{2}:\d{2}:\d{2},\d{3}) \| .* \| .* \| .* \| .* \| '
        )


class NIDOParser(LogParser):

    def __init__(self):
        super(NIDOParser, self).__init__(
            r'(?P<time>[0-9]{4}-[0-9]{2}-[0-9]{2} \d{2}:\d{2}:\d{2}.\d{3})\s+[A-Z]+ \d+ --- \[.{15}\] [a-zA-Z\.]+\s+: '
        )


class EMMAParser(LogParser):

    def __init__(self):
        super(EMMAParser, self).__init__(
            r'(?P<time>[0-9]{4}-[0-9]{2}-[0-9]{2} \d{2}:\d{2}:\d{2}.\d{3})\s+[A-Z]+ [a-zA-Z\.]+:\d+ - '
        )
