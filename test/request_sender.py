import json
from argparse import ArgumentParser
from functools import partial

from tornado.httpclient import HTTPRequest, HTTPClient
from tornado.ioloop import IOLoop


class Config(object):
    verbose = False


class SendAction(object):
    headers = {'content-type': "application/yang.data+json"}

    @property
    def body(self):
        return json.dumps(self.payload)

    def __init__(self,
                 pod1,
                 tor1,
                 port1,
                 pod2,
                 tor2,
                 port2,
                 bw,
                 destination_ip=None,
                 controller=None):
        self.pod1 = pod1
        self.tor1 = tor1
        self.port1 = port1
        self.pod2 = pod2
        self.tor2 = tor2
        self.port2 = port2
        self.bw = bw
        self.connection_id = None
        self.payload = None
        self.destination_ip = destination_ip
        self.controller = controller if controller is not None else '127.0.0.1'
        self.uri = "http://{}/affinity/connection".format(controller)
        self.make_res()

    def make_res(self):
        self.payload = \
            {
                "connections": [
                    {
                        "Destination_IP": self.destination_ip,
                        "Source_end_point": {
                            "Pod_ID": self.pod1,
                            "ToR_ID": self.tor1,
                            "Zone_ID": self.port1
                        },
                        "Destination_end_point": {
                            "Pod_ID": self.pod2,
                            "ToR_ID": self.tor2,
                            "Zone_ID": self.port2
                        },
                        "Traffic_profile": {
                            "Reserved_bandwidth": self.bw
                        },
                        "Connection_type": "POINT_TO_POINT",
                        "Recovery": "UNPROTECTED"
                    }
                ]
            }

    async def send_conn(self, http_client):
        """Install a connection with specified parameters."""
        request = HTTPRequest(
            method='POST',
            url=self.uri,
            auth_username='admin',
            auth_password='admin',
            headers=self.headers,
            body=self.body
        )
        response = await http_client.fetch(request)
        print("Request sent for {}, status: {}.".format(str(self), response.code))
        self.connection_id = json.loads(response.body)['Connection_ID']
        if Config.verbose or response.code != 204:
            print(response.body)
        return response.code

    async def closing(self, http_client):
        del_uri = self.uri + '/' + self.connection_id
        request = HTTPRequest(
            method='DELETE',
            url=del_uri,
            auth_username='admin',
            auth_password='admin'
        )
        response = await http_client.fetch(request)
        print("Deletion request sent for {}, status: {}.".format(str(self), response.code))
        if Config.verbose or response.code != 204:
            print(response.body)
        return response.code

    def __str__(self):
        return "connection ({}, {}, {}) -> ({}, {}, {})".format(
            self.pod1, self.tor1, self.port1,
            self.pod2, self.tor2, self.port2
        )


def main():
    parser = ArgumentParser()
    parser.add_argument('--sp', help='Source pod', type=int, required=True)
    parser.add_argument('--st', help='Source tor', type=int, required=True)
    parser.add_argument('--sz', help='Source zone', type=int, required=True)
    parser.add_argument('--dp', help='Destination pod', type=int, required=True)
    parser.add_argument('--dt', help='Destination tor', type=int, required=True)
    parser.add_argument('--dz', help='Destination zone', type=int, required=True)
    parser.add_argument('--bw', help='Bandwidth', type=int, required=True)
    parser.add_argument('--d-ip', help='Destination IP', type=str, default=None)
    parser.add_argument('--con', help='Controller IP', type=str, default='127.0.0.1')
    args = parser.parse_args()
    if args.d_ip is None:
        d_ip = '10.{}.{}.{}'.format(args.dp, args.dt, args.dz)
    else:
        d_ip = args.d_ip
    http_client = HTTPClient()
    cb = partial(
        SendAction(
            args.sp, args.st, args.sz,
            args.dp, args.dt, args.dz,
            args.bw,
            d_ip,
            args.con
        ).send_conn,
        http_client
    )
    IOLoop.current().run_sync(cb)


if __name__ == "__main__":
    main()
