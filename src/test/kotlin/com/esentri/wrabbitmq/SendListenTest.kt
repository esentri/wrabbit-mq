package com.esentri.wrabbitmq

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.Serializable
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class SendListenTest {

   private val topic = WrabbitTopic("TestTopic-Listeners")

   private fun <MESSAGE: Serializable> newEvent(): WrabbitEvent<MESSAGE> =
     WrabbitEvent(topic, UUID.randomUUID().toString())

   @Test
   fun sendListenString() {
      val wrabbitEvent = newEvent<String>()
      val countDownLatch = CountDownLatch(1)
      val message = "1: Hello World!"

      wrabbitEvent.listener { it ->
         assertThat(it).isEqualTo(message)
         countDownLatch.countDown()
      }
      wrabbitEvent.send(message)

      Await(countDownLatch)
   }

   @Test
   fun sendListenString_X_times() {
      val wrabbitEvent = newEvent<String>()
      val sentTimes = 1000
      val countDownLatch = CountDownLatch(sentTimes)
      val message = "2: Hello World!"

      wrabbitEvent.listener { it ->
         assertThat(it).isEqualTo(message)
         countDownLatch.countDown()
      }
      for (i in 1..sentTimes) {
         wrabbitEvent.send(message)
      }

      Await(countDownLatch)
   }

   @Test
   fun sendListenString_2_listener() {
      val wrabbitEvent = newEvent<String>()
      val countDownLatch = CountDownLatch(2)
      val message = "3: Hello World!"

      wrabbitEvent.listener { it ->
         assertThat(it).isEqualTo(message)
         countDownLatch.countDown()
      }
      wrabbitEvent.listener { it ->
         assertThat(it).isEqualTo(message)
         countDownLatch.countDown()
      }
      wrabbitEvent.send(message)

      Await(countDownLatch)
   }

   @Test
   fun sendTestObjectObject() {
      val wrabbitEvent = newEvent<TestObjectObject>()
      val countDownLatch = CountDownLatch(1)
      val message = TestObjectObject(TestObjectNumberText(12345, "1: Hello World!"))

      wrabbitEvent.listener { it ->
         assertThat(it).isInstanceOf(TestObjectObject::class.java)
         assertThat(it.obj).isInstanceOf(TestObjectNumberText::class.java)
         assertThat(it.obj.number).isEqualTo(message.obj.number)
         assertThat(it.obj.text).isEqualTo(message.obj.text)
         countDownLatch.countDown()
      }
      wrabbitEvent.send(message)

      Await(countDownLatch)
   }

   @Test
   fun sendParallel() {
      val wrabbitEvent1 = newEvent<String>()
      val wrabbitEvent2 = newEvent<TestObjectObject>()
      val countDownLatch = CountDownLatch(2)
      val message1 = "4: Hello World!"
      val message2 = TestObjectObject(TestObjectNumberText(12345, "2: Hello World!"))

      wrabbitEvent1.listener { it ->
         assertThat(it).isEqualTo(message1)
         countDownLatch.countDown()
      }
      wrabbitEvent2.listener { it ->
         assertThat(it).isInstanceOf(TestObjectObject::class.java)
         assertThat(it.obj).isInstanceOf(TestObjectNumberText::class.java)
         assertThat(it.obj.number).isEqualTo(message2.obj.number)
         assertThat(it.obj.text).isEqualTo(message2.obj.text)
         countDownLatch.countDown()
      }

      wrabbitEvent1.send(message1)
      wrabbitEvent2.send(message2)

      Await(countDownLatch)
   }

   @Test
   fun sendGroups() {
      val wrabbitEvent = newEvent<String>()
      // AtomicInteger is used to check if after all expected events another one is arriving
      // This would not be possible with CountDownLatch
      val waitCounter = AtomicInteger(0)
      val sentTimes = 10
      val message = "5: Hello World!"

      wrabbitEvent.listener("group1") { it ->
         assertThat(it).isEqualTo(message)
         waitCounter.incrementAndGet()
      }
      wrabbitEvent.listener("group1") { it ->
         assertThat(it).isEqualTo(message)
         waitCounter.incrementAndGet()
      }
      wrabbitEvent.listener("group2") { it ->
         assertThat(it).isEqualTo(message)
         waitCounter.incrementAndGet()
      }

      for (i in 1..sentTimes) {
         wrabbitEvent.send(message)
      }
      while (waitCounter.get() < sentTimes * 2) {
         Thread.sleep(300)
      }
      for (i in 1..5) {
         Thread.sleep(500)
      }
      assertThat(waitCounter.get()).isEqualTo(sentTimes * 2)
   }

   @Test
   fun sendAndListenWithContext() {
      val wrabbitEvent = newEvent<String>()
      val countDownLatch = CountDownLatch(1)
      val message = "6: Hello World!"
      val propertyKey = "test"
      val propertyValue = "property"

      wrabbitEvent.listener { context, it ->
         assertThat(it).isEqualTo(message)
         assertThat(context[propertyKey].toString()).isEqualToIgnoringCase(propertyValue)
         countDownLatch.countDown()
      }
      wrabbitEvent
         .messageBuilder()
         .property(propertyKey, propertyValue)
         .send(message)

      Await(countDownLatch)
   }
}