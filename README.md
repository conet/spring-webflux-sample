# steps

1. run `./test.sh`
2. run `watch 'curl -v http://localhost:8888/actuator/metrics/reactor.netty.connection.provider.active.streams | python -m json.tool'`
3. run `./test.sh` (until the stream leak appears)
