package com.esentri.wrabbitmq

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.Serializable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class SendReplyTest {

   private val topic = WrabbitTopic("TestTopic-Replier")
   private fun <MESSAGE : Serializable, REPLY : Serializable> newEvent() =
      NewEventWithReply<MESSAGE, REPLY>(topic)

   @Test
   fun sendReplyStringToInt() {
      val event = newEvent<String, Int>()
      val countDownLatch = CountDownLatch(1)
      val message = "12345"

      event.replier { it ->
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      event.sendAndReceive(message).thenAccept {
         assertThat(it).isEqualTo(message.toInt())
         countDownLatch.countDown()
      }

      Await(countDownLatch)
   }

   @Test
   fun sendReplyStringToInt_X_times() {
      val event = newEvent<String, Int>()
      val sentTimes = 100
      val countDownLatch = CountDownLatch(sentTimes)

      val message = "12345"
      event.replier { it ->
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      for (i in 1..sentTimes) {
         event.sendAndReceive(message).thenAccept {
            assertThat(it).isEqualTo(message.toInt())
            countDownLatch.countDown()
         }
      }

      Await(countDownLatch)
   }

   @Test
   fun sendReplyStringToInt_2_replier_default() {
      val event = newEvent<String, Int>()
      // AtomicInteger is used to check if any events occur after the expected.
      // This would not be possible with CountDownLatch.
      val waitCounter = AtomicInteger(0)
      val message = "12345"

      event.replier { it ->
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      event.replier { it ->
         assertThat(it).isEqualTo(message)
         it.toInt()
      }

      event.sendAndReceive(message).thenAccept {
         assertThat(it).isEqualTo(message.toInt())
         waitCounter.incrementAndGet()
      }

      while (waitCounter.get() <= 0) {
         Thread.sleep(300)
      }
      Thread.sleep(1000)
      assertThat(waitCounter.get()).isEqualTo(1)
   }

   @Test
   fun sendAndReplyWithContext() {
      val event = newEvent<TestObjectObject, String>()
      val countDownLatch = CountDownLatch(1)
      val message = TestObjectObject(TestObjectNumberText(12345, "hello world"))
      val propertyKey = "test"
      val propertyValue = "property"

      event.replier { context, it ->
         assertThat(it.obj.number).isEqualTo(message.obj.number)
         assertThat(it.obj.text).isEqualTo(message.obj.text)
         assertThat(context[propertyKey].toString()).isEqualToIgnoringCase(propertyValue)
         countDownLatch.countDown()
         it.obj.text
      }
      event
         .messageBuilder()
         .property(propertyKey, propertyValue)
         .sendAndReceive(message)
         .thenAccept {
            assertThat(it).isEqualTo(message.obj.text)
         }

      Await(countDownLatch)
   }

   @Test
   fun sendReply_parallel_two() {
      val event1 = newEvent<String, Int>()
      val event2 = newEvent<Int, String>()
      val asString = "12345"
      val asInt = 12345
      val sentTimesEach = 100
      val countDownLatch = CountDownLatch(sentTimesEach * 2)

      event1.replier { it -> it.toInt() }
      event2.replier { it -> it.toString() }

      for(i in 1..sentTimesEach) {
         CompletableFuture.runAsync {
            event2.sendAndReceive(asInt).thenAccept {
               assertThat(it).isEqualTo(asString)
               countDownLatch.countDown()
            }
            event1.sendAndReceive(asString).thenAccept {
               assertThat(it).isEqualTo(asInt)
               countDownLatch.countDown()
            }
         }
      }

      Await(countDownLatch)
   }

   @Test
   fun sendReply_parallel_single() {
      val event = newEvent<Int, String>()
      val startValue = 1
      val sentTimes = 1000
      val countDownLatch = CountDownLatch(sentTimes)

      event.replier { it -> it.toString() }

      for(i in 1..sentTimes) {
         CompletableFuture.runAsync {
            event.sendAndReceive(startValue + i).thenAccept {
               assertThat(it).isEqualTo((startValue + i).toString())
               countDownLatch.countDown()
            }
         }
      }

      Await(countDownLatch)
   }

}