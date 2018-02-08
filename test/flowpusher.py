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
stop = False

P = 6
W = 80
Z = 2

FIRST_POD = 10


def kill():
    global stop
    stop = True
    print('Stopping pusher.')


def main(controller, rate, duration):
    global stop
    exception = None
    start_time = datetime.now()
    end_time = start_time + timedelta(seconds=duration)
    try:

        async def periodic_check():
            while True:
                await sleep(5)
                print('*** {} Duration |                | Until the end {} ***'
                      .format(datetime.now() - start_time, end_time - datetime.now()))
                if datetime.now() >= end_time:
                    break

        async def schedule():
            client = None
            nonlocal exception
            try:
                client = AsyncHTTPClient()
                while datetime.now() <= end_time and not stop:
                    back_off = expovariate(1 / rate)
                    conn_duration = expovariate(1 / (5 * rate)) + 2
                    await sleep(back_off)

                    p1 = randrange(0, P) + FIRST_POD
                    t1 = randrange(0, W) + 1
                    z1 = randrange(0, Z) + 1
                    p2 = randrange(0, P) + FIRST_POD
                    t2 = randrange(0, W) + 1
                    z2 = randrange(0, Z) + 1

                    while (p1, t1, z1) == (p2, t2, z2):
                        p1 = randrange(0, P) + FIRST_POD
                        t1 = randrange(0, W) + 1
                        z1 = randrange(0, Z) + 1
                        p2 = randrange(0, P) + FIRST_POD
                        t2 = randrange(0, W) + 1
                        z2 = randrange(0, Z) + 1

                    bw = randrange(1, 4)

                    action = SendAction(
                        p1, t1, z1,
                        p2, t2, z2,
                        bw,
                        controller=controller
                    )
                    actions.append(action)

                    send_cb = partial(
                        action.send_conn,
                        http_client=client
                    )
                    del_cb = partial(
                        action.closing,
                        http_client=client
                    )

                    IOLoop.current().spawn_callback(send_cb)
                    IOLoop.current().call_later(conn_duration, del_cb)
            except BaseException as e:
                exception = e
            finally:
                if client is not None:
                    IOLoop.current().stop()
                    for action in actions:
                        if action.active:
                            IOLoop.current().run_sync(action.closing)
                    client.close()

        IOLoop.current().spawn_callback(schedule)
        IOLoop.current().spawn_callback(periodic_check)
        print('*** Starting IOLoop.')
        IOLoop.current().start()
    except KeyboardInterrupt:
        print('Interrupted. Terminating.')
        # We don't want its info.
    except BaseException as e:
        print('Error: {}.', str(e))
        exception = e
    finally:  # make sure the loop is stopped in case of exceptions (i.e. keyboard interrupt)
        print('*** Waiting 20 seconds to let the connections establish correctly.')
        time.sleep(20)
        IOLoop.current().stop()
        # print('*** Cleaning up.')
        # c = AsyncHTTPClient()
        #
        # async def delete_cb():
        #     counter = 0
        #     for action in actions:  # type: SendAction
        #         await action.closing(c)
        #         counter += 1
        #         await sleep(0.2)
        #         if counter == 10:
        #             await sleep(1)
        #             counter = 0
        #     print('*** All connections deleted, backing off.')
        #     await sleep(20)
        #     print('*** Pusher shutting down.')
        #     c.close()
        #     IOLoop.current().stop()
        #
        # IOLoop.current().spawn_callback(delete_cb)
        # print('*** Starting IOLoop for cleanup.')
        # IOLoop.current().start()
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
