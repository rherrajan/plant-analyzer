#!/bin/sh
set -x #echo on

cd "$(dirname "$0")"

npm start
http-server ./dist &

sleep 1s
sensible-browser http://localhost:8080/

grunt watch

