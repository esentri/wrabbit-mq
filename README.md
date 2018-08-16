# WRABBIT-MQ

`Wrabbit-MQ` is a light-weight wrapper for RabbitMQ. It simplifies the usage of RabbitMQ and has
built-in support for the `broadcast-response`-pattern.

## Usage

First, you need to create your domain; i.e. declare all topics and corresponding events. Then you can
send and _send and receive_ on base of the events.

For receiving the events you have three methods:

* replier,
* listener,
* auditor.

`replier` is the only one who can reply on an event. It is also ensured that only one replier will receive an event
even if you registered multiple repliers on the same topic/event.

`listener`s and `auditor`s will always receive any event they are registered for. But they cannot reply.

In addition, `auditor`s can be registered on a topic as well (not only on events like `listener`s).

### Create your domain

Example in Kotlin:
```
object TestDomain {

   object SimpleTopic : WrabbitTopic(name = "test.topic.simple") {
      val Event1 = WrabbitEvent<String, Int>(this, "test.topic.simple.Event1")
      val Event2 = WrabbitEvent<Int, String>(this, "test.topic.simple.Event2")

      object NestedTopic : WrabbitTopic(name = "test.topic.simple.nestedTopic") {
         val Event1 = WrabbitEvent<Int, Int>(this, "test.topic.simple.nestedTopic.Event1")
         val Event2 = WrabbitEvent<Int, Int>(this, "test.topic.simple.nestedTopic.Event2")
      }
   }
}
```

### Send events

Example in Kotlin:
```
TestDomain.SimpleTopic.Event1.send("hello world")
TestDomain.SimpleTopic.Event2.send(123)
```


### Send and receive

Example in Kotlin:
```
   TestDomain.SimpleTopic.Event1.sendAndReceive("1234").thenAccept {
      LOGGER.info("Event1 received reply: $it")
   }

   TestDomain.SimpleTopic.Event2.sendAndReceive(123).thenAccept {
      LOGGER.info("Event2 received reply: $it")
   }
```


### Listen on an event

Example in Kotlin:
```
   TestDomain.SimpleTopic.Event1.listener {
      LOGGER.info("received string: $it")
   }

   TestDomain.SimpleTopic.Event2.listener {
      LOGGER.info("received number: $it")
   }
```


### Audit an event

Example in Kotlin:
```
   TestDomain.SimpleTopic.Event1.auditor {
      LOGGER.info("Event1: received string: $it")
   }

   TestDomain.SimpleTopic.Event2.auditor {
      LOGGER.info("Event2: received number: $it")
   }
```

### Audit a topic

Example in Kotlin:
```
   TestDomain.SimpleTopic.auditor<Any> {
      LOGGER.info("SimpleTopic: received: $it")
   }

   TestDomain.SimpleTopic.NestedTopic.auditor<Any> {
      LOGGER.info("NestedTopic: received: $it")
   }
```

## Configuration

Here are the default values:

```
WrabbitDefaultHost = "localhost"
WrabbitDefaultPort: Int = 5672
WrabbitDefaultUsername = "guest"
WrabbitDefaultPassword = "guest"
WrabbitDefaultTimeout: Int= 30000
WrabbitDefaultHeartBeat = 30
```

To change any of the values please set the corresponding environment variable:

```
"spring.rabbitmq.host"
"spring.rabbitmq.port"
"spring.rabbitmq.username"
"spring.rabbitmq.password"
"spring.rabbitmq.connection-timeout"
"spring.rabbitmq.requested-heartbeat"
```


## Contributions

Contributions are welcomed. Please clone the repository, make your changes and open a pull request.

For open issues see the issue tracker:

* rebuild of the `RabbitMQ` connection, so the `Spring` dependency can be dropped - which will result in an even 
lighter library
* automatic tests (currently, you need to spin up a RabbitMQ server and check manually if everything works)


## License

MIT License

Copyright (c) 2018 esentri AG

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.