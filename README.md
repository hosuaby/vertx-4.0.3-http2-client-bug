# Bug Vertx 4.0.3

Scenario leading to the bug:

1. User makes request to available server (request succeeds)
2. Server becomes unavailable (shutdown, network problem, etc)
3. User makes request to unavailable server (request fails)
4. Server becomes available again
5. User makes request to returned server
6. Request fails by timeout

If the same scenario run with HTTP 1.1 client, the third request succeed (what is expected).

The bug was introduced in Vertx `4.0.3`. Version `4.0.2` have no such problem.

To try this scenario:

```shell script
$ ./gradlew clean build
```

Test "After server is available again" -> "Request with HTTP2 client should succeed" is failed.

Try the same scenario with Vertx `4.0.2`:

```shell script
$ ./gradlew clean build -DVERTX_VERSION=4.0.2
```