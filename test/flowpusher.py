#! usr/bin/env python3
from argparse import ArgumentParser
from functools import partial
from random import expovariate, randrange

from datetime import datetime, timedelta

import time

from tornado.gen import sleep
from tornado.httpclient import AsyncHTTPClient
from tornado.ioloop import IOLoop

from request_sender import SendAction

actions = []

P = 20
W = 80
Z = 4

FIRST_POD = 10


async def send_flow(p1, t1, z1, p2, t2, z2, bw, http_client, d_ip=None, con=None):
    action = SendAction(
        p1, t1, z1,
        p2, t2, z2,
        bw,
        d_ip,
        con
    )
    actions.append(action)
    await action.send_conn(http_client)


def main(controller, rate, duration):
    exception = None
    start_time = datetime.now()
    end_time = start_time + timedelta(seconds=duration)
    try:

        async def periodic_check():
            while True:
                await sleep(5)
                print('*** {:04} Duration |                | Until the end {:04} ***'
                      .format(datetime.now() - start_time, end_time - datetime.now()))
                if datetime.now() >= end_time:
                    break

        async def schedule():
            client = None
            try:
                client = AsyncHTTPClient()
                while datetime.now() >= end_time:
                    back_off = expovariate(1 / rate)
                    await sleep(back_off)

                    p1 = randrange(0, P) + FIRST_POD
                    t1 = randrange(0, W)
                    z1 = randrange(0, Z)
                    p2 = randrange(0, P) + FIRST_POD
                    t2 = randrange(0, W)
                    z2 = randrange(0, Z)

                    while (p1, t1, z1) == (p2, t2, z2):
                        p1 = randrange(0, P) + FIRST_POD
                        t1 = randrange(0, W)
                        z1 = randrange(0, Z)
                        p2 = randrange(0, P) + FIRST_POD
                        t2 = randrange(0, W)
                        z2 = randrange(0, Z)

                    bw = randrange(1, 4)

                    send_cb = partial(
                        send_flow,
                        p1, t1, z1,
                        p2, t2, z2,
                        bw,
                        http_client=client,
                        controller=controller
                    )

                    IOLoop.current().spawn_callback(send_cb)
            finally:
                if client is not None:
                    client.close()

        IOLoop.current().spawn_callback(schedule)
        IOLoop.current().spawn_callback(periodic_check)
        print('*** Starting IOLoop.')
        IOLoop.current().start()
    except KeyboardInterrupt:
        print('Interrupted. Terminating.')
        # We don't want its info.
    except BaseException as e:
        exception = e
    finally:  # make sure the loop is stopped in case of exceptions (i.e. keyboard interrupt)
        print('*** Waiting 20 seconds to let the connections establish correctly.')
        time.sleep(20)
        IOLoop.current().stop()
        print('*** Cleaning up.')
        c = AsyncHTTPClient()

        async def delete_cb():
            counter = 0
            for action in actions:  # type: SendAction
                await action.closing(c)
                counter += 1
                await sleep(0.2)
                if counter == 10:
                    await sleep(1)
                    counter = 0
            print('*** All connections deleted, backing off.')
            await sleep(20)
            print('*** Pusher shutting down.')
            c.close()
            IOLoop.current().stop()

        IOLoop.current().spawn_callback(delete_cb)
        print('*** Starting IOLoop for cleanup.')
        IOLoop.current().start()
        if exception is not None:
            print('Exited due to {}: {}.'.format(exception.__class__.__name__, str(exception)))


if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument('controller', type=str, metavar='CONTROLLER',
                        help='Controller IP address.')
    parser.add_argument('rate', type=float, metavar='RATE',
                        help='The rate at which requests should be sent to the controller.')
    parser.add_argument('duration', type=int, metavar='DURATION',
                        help='The duration of the test, in seconds.')
    args = parser.parse_args()

    main(args.controller, args.rate, args.duration)
