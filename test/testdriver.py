from argparse import ArgumentParser
from threading import Thread
from time import sleep

from flowpusher import main as push, kill as kill_pusher

from affinityparser import main as parse, kill as kill_parser


def main(controller, duration, rate, log_file, out_file, iterations):

    for i in range(iterations):
        print('************************************')
        print('*** Running iteration {:03} of {:03} ***'.format(i + 1, args.iterations))
        print('************************************')
        parser_t = Thread(name='Parser Thread', target=parse,
                          kwargs={'output_file': out_file, 'log_file': log_file})
        pusher_t = Thread(name='Pusher Thread', target=push,
                          kwargs={'controller': controller, 'rate': rate, 'duration': duration})
        try:
            parser_t.start()
            print('*** Sleeping 5 seconds to let the parser boot up.')
            sleep(5)
            pusher_t.start()
            print('*** Pusher thread started')
            pusher_t.join()
        except KeyboardInterrupt:
            break  # Will still pass through 'finally' block.
        finally:
            if pusher_t.is_alive():
                print('*** Stopping & joining pusher.')
                kill_pusher()
                pusher_t.join()
            print('*** Stopping parsers, pushing results.')
            kill_parser()
            print('*** Joining parser thread')
            parser_t.join()


if __name__ == '__main__':
    p = ArgumentParser()
    p.add_argument('-O', '--out-file', help='The output file to use',
                   metavar='OUT', type=str, default='out.csv')
    p.add_argument('-L', '--log-file', help='The log file to parse',
                   metavar='LOG', type=str,
                   default='/home/nextworks/beryllium-SR2/distribution-karaf-0.4.2-Beryllium-SR2/data/log/karaf.log')
    p.add_argument('-C', '--controller', help='The IP address of the controller',
                   metavar='CONTROLLER', type=str, default='127.0.0.1')
    p.add_argument('-F', '--flow-file', help='The file containing flow details',
                   metavar='FLOW', type=str, default='antennas.csv')
    p.add_argument('-I', '--iterations', help='The number of iterations of the test',
                   metavar='ITERATIONS', type=int, default=1)
    p.add_argument('rate', help='The inter-request time, in seconds', metavar='RATE', type=float)
    args = p.parse_args()
    main(args.controller, args.rate, args.out_file, args.log_file, args.flow_file, args.iterations)