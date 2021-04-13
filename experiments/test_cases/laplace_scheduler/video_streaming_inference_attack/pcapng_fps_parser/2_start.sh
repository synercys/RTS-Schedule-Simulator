#!/usr/bin/env bash

docker run -it -v `pwd`:/root/parser -w /root/parser python:pcapng /bin/bash