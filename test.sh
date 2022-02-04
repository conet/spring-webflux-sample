#!/bin/bash

for i in {1..100}
do
  curl -s http://localhost:8888/http2 -o /dev/null &
done
wait

