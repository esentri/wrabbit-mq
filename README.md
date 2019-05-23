[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.esentri.oss/wrabbit-mq/badge.svg)](https://search.maven.org/artifact/com.esentri.oss/wrabbit-mq)
[![Build Status](https://travis-ci.org/esentri/wrabbit-mq.svg?branch=master)](https://travis-ci.org/esentri/wrabbit-mq)

# WRABBIT-MQ

`Wrabbit-MQ` is a light-weight wrapper for RabbitMQ. It simplifies the usage of RabbitMQ and has
built-in support for the `broadcast-response`-pattern.

## Dependency

* Gradle

  ```
  compile 'com.esentri.oss:wrabbit-mq:3.2.4'
  ```
  
* Maven

  ```
  <dependency>
    <groupId>com.esentri.oss</groupId>
    <artifactId>wrabbit-mq</artifactId>
    <version>3.2.4</version>
    <type>pom</type>
  </dependency>
  ```


## Usage

First, you need to create your domain; i.e. declare all topics and corresponding events. Then you can
send and _send and receive_ on base of the events.

For receiving the events you have two methods:

* replier,
* listener.

`replier` is the only one who can reply on an event. It is also ensured that only one replier will receive an event
even if you registered multiple repliers on the same topic/event. This will ensure scalability without side effects.

`listener`s will always receive any event they are registered for. But they cannot reply.

In addition, `listener`s 
* can be registered on a topic as well (not only on events like `replier`) and
* can be grouped; i.e. only one of the group listeners will be informed (in contrary to all listeners).

**For Java users:** please make sure to use `WrabbitEvent` and `WrabbitEventReplier` from the `com.esentri.wrabbitmq.java` package.

### Create your domain

*(creating a domain like this is optional but recommended)*

<details>
   <summary>Kotlin</summary>
   
   ```kotlin
   object TestDomain {
      object ListenerTopic1: WrabbitTopic("TestTopic-1") {
         val StringEvent = WrabbitEvent<String>(this, "TT1-TE-1")
         val TestObjectObjectEvent = WrabbitEvent<TestObjectObject>(this, "TT1-TE-2")
      }

      object ReplierTopic1: WrabbitTopic("TestTopic-2") {
         val StringToInt = WrabbitEventWithReply<String, Int>(this, "TT2-TE1")
         val TestObjectObjectToString = WrabbitEventWithReply<TestObjectObject, String>(this, "TT2-TE2")
         val TestObjectObjectToTestObjectNumberText = WrabbitEventWithReply<TestObjectObject, TestObjectNumberText>(this, "TT2-TE3")
      }
   }
   ```
</details>
  
<details>
   <summary>Java</summary>
   
  ```java
  public final class TestDomain {
  
     private TestDomain(){}
  
     public static final SimpleListenerTopic ListenerTopic1 = new SimpleListenerTopic();
     public static final SimpleReplierTopic ReplierTopic1 = new SimpleReplierTopic();
  
     public static final class SimpleListenerTopic extends WrabbitTopic {
        private SimpleListenerTopic() {
           super("Test-Topic-1");
        }
  
        public WrabbitEvent<String> StringEvent = new WrabbitEvent<>(this, "TT1-TE-1");
        public WrabbitEvent<TestObjectObject> TestObjectObjectEvent = new WrabbitEvent<>(this, "TT1-TE-2");
     }
  
     public static final class SimpleReplierTopic extends WrabbitTopic {
        private SimpleReplierTopic() {
           super("Test-Topic-2");
        }
  
        public WrabbitEventWithReply<String, Integer> StringToInt = new WrabbitEventWithReply<>(this, "TT2-TE1");
        public WrabbitEventWithReply<TestObjectObject, String> TestObjectObjectToString = new WrabbitEventWithReply<>(this, "TT2-TE2");
     }
  }
  ```
</details>

### Use the domain

* sending an event
  * Kotlin
     ```kotlin
      TestDomain.ListenerTopic1.StringEvent.send("Hello world!")
     ```
  * Java
     ```java
      TestDomain.ListenerTopic1.StringEvent.send("Hello world!");
     ```
     
* sending an event with additional context (apart of the message itself)
  * you can add as many properties as you want
  * Kotlin
     ```kotlin
      TestDomain.ListenerTopic1.StringEvent
         .messageBuilder()
         .property("key", "value")
         .send(message)
     ```
  * Java
     ```java
      TestDomain.ListenerTopic1.StringEvent
         .messageBuilder()
         .property("key", "value")
         .send(message);
     ```
     
* send an event and receive a reply (if any)
  * Kotlin
    ```kotlin
      TestDomain.ReplierTopic1.StringToInt.sendAndReceive("12345").thenAccept {
         // do something with it
      }
    ```
  * Java
    ```java
      TestDomain.ReplierTopic1.StringToInt.sendAndReceive("12345").thenAccept(it -> {
         // do something with it
      });
    ```
     
* listen to an event
  * Kotlin
    ```kotlin
    TestDomain.ListenerTopic1.StringEvent.listener { it ->
       // do something with it
    }
    ```
    
  * Java
    ```java
    TestDomain.ListenerTopic1.StringEvent.listener(it -> {
       // do something with it
    });
    ```
    
* listen to an event and receive the context
  * Kotlin
    ```kotlin
      TestDomain.ListenerTopic1.StringEvent.listener { context, it ->
         // do something
      }
    ```
  * Java
    ```java
      TestDomain.ListenerTopic1.StringEvent.listener((context, it) -> {
         // do something
      });
    ```
    
* listen to an event as part of a group
  * Kotlin
    ```kotlin
      TestDomain.ListenerTopic1.StringEvent.listener("group1") { it ->
         // do something with it
      }
    ```
  * Java
    ```java
    TestDomain.ListenerTopic1.StringEvent.listener("group1", it -> {
       // do something with it
    });
    ```

* reply to an event
  * Kotlin
    ```kotlin
    TestDomain.ReplierTopic1.StringToInt.replier { it ->
       it.toInt()
    }
    ```
  * Java
    ```java
    TestDomain.ReplierTopic1.StringToInt.replier(it ->
       Integer.parseInt(it);
    );
    ```
    
* reply to an event with context
  * Kotlin
    ```kotlin
      TestDomain.ReplierTopic1.TestObjectObjectToString.replier { context, it ->
         // context[propertyKey]
         it.obj.text
      }
    ```
  * Java
    ```java
      TestDomain.ReplierTopic1.TestObjectObjectToString.replier((context, it) -> {
         // context.get(propertyKey)
         return it.getObj().getText();
      });
    ```

### Exceptions

Currently, the important exceptions are logged to `error` with `SLF4J`.

In addition you can do the following:

* receive exceptions from a replier on sender side (e.g. you send a message and wait for a reply but the replier failed)
  * those exceptions are wrapped inside `WrabbitBasicReplyException` which contains information about the topic and event
    the message was originally sent to
  * Kotlin
    ```kotlin
      event.sendAndReceive("hello").thenAccept {
         countDownLatch.countDown()
      }.exceptionally {
      // do something with it
      }
    ```
  * Java
    ```java
    event.sendAndReceive("hello").thenAccept(it -> { })
         .exceptionally(it -> { 
          // do something with it
        });
    ```

## Configuration

Here are the default values:

```
Host = "localhost"
Port = 5672
Username = "guest"
Password = "guest"
Timeout = 30000
HeartBeat = 30
ReplyTimeOutMS = 5000
```

To change any of the values please set the corresponding environment variable
(at the start of your application):

* in general
  ```
  "wrabbit.host"
  "wrabbit.port"
  "wrabbit.username"
  "wrabbit.password"
  "wrabbit.connection-timeout"
  "wrabbit.requested-heartbeat"
  "wrabbit.reply-timeout-ms"
  ```

* *or* in Spring
  ```
  "spring.rabbitmq.host"
  "spring.rabbitmq.port"
  "spring.rabbitmq.username"
  "spring.rabbitmq.password"
  "spring.rabbitmq.connection-timeout"
  "spring.rabbitmq.requested-heartbeat"
  "spring.rabbitmq.reply-timeout-ms"
  ```

## Logging

`WrabbitMQ` uses `SLF4J` so you can integrate it easily with your current logging system.

## Contributions

Contributions are welcomed. Please clone the repository, make your changes and open a pull request.

For open issues see the issue tracker.


## License

MIT License

Copyright (c) 2019 esentri AG

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.