#!/usr/bin/env python3
import shlex
import subprocess
import time

import requests
from requests.auth import HTTPBasicAuth


class Deploy:
    """
    Deploy a local war in a known location remote or locally
    """

    ARTIFACT_PATH = "./build/libs/provider-api-1.0-SNAPSHOT.war"
    PATH = "/api/1"
    PORT = 8080
    CREDENTIALS = HTTPBasicAuth("tomcat", "changeme")
    KEY = "/Users/ixtli/.ssh/monday-root.pem"

    def __init__(self):
        self._tunnel = None

    def __del__(self):
        if self._tunnel:
            self._tunnel.kill()
            self._tunnel = None

    def _get_update_url(self, host: str) -> str:
        options: str = "update=true&path=" + self.PATH
        return "http://{}:{}/manager/text/deploy?{}".format(host, self.PORT,
                                                            options)

    def _deploy(self, host: str, proxies=None) -> None:
        with open(self.ARTIFACT_PATH, 'rb') as artifact:
            response = requests.put(self._get_update_url(host),
                                    auth=self.CREDENTIALS,
                                    data=artifact,
                                    proxies=proxies)
            print(response.status_code, response.text.strip())

    def _start_tunnel(self) -> None:
        remote = "ec2-user@bastion.monday.health"
        local = "localhost:{}".format(self.PORT)
        cmd = "ssh -i {} -N -D {} {}".format(self.KEY, local, remote)
        parsed = shlex.split(cmd)
        self._tunnel = subprocess.Popen(parsed, shell=False)
        print("Tunnel started:", self._tunnel.pid)
        time.sleep(1)

    def local(self) -> None:
        """
        Deploy a war to tomcat using the /manager/text/ api
        """
        self._deploy("localhost")

    def remote(self) -> None:
        """
        Deploy a war to the remote tomcat using the /manager/text/ api
        """
        self._start_tunnel()
        self._deploy("ec2-18-218-145-131.us-east-2.compute.amazonaws.com",
                     dict(http="socks5h://localhost:8080"))


if __name__ == "__main__":
    Deploy().remote()
