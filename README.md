# steps

1. run the app `mvn spring-boot:run`
2. run `./test.sh`
3. run `watch 'curl -v http://localhost:8888/actuator/metrics/reactor.netty.connection.provider.active.streams | python -m json.tool'`
4. run `./test.sh` (until the stream leak appears)
