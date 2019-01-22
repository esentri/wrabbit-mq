package com.esentri.wrabbitmq

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class SendListenTest {

   @Test
   fun sendListenString() {
      val waitCounter = AtomicInteger(0)
      val message = "1: Hello World!"
      TestDomain.ListenerTopic1.StringEvent.listener {
         assertThat(it).isEqualTo(message)
         waitCounter.incrementAndGet()
      }
      TestDomain.ListenerTopic1.StringEvent.send(message)
      while(waitCounter.get() == 0) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendListenString_X_times() {
      val waitCounter = AtomicInteger(0)
      val sentTimes = 1000
      val message = "2: Hello World!"
      TestDomain.ListenerTopic1.StringEvent.listener {
         assertThat(it).isEqualTo(message)
         waitCounter.incrementAndGet()
      }
      for(i in 1..sentTimes) {
         TestDomain.ListenerTopic1.StringEvent.send(message)
      }
      while(waitCounter.get() < sentTimes) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendListenString_2_listener() {
      val waitCounter = AtomicInteger(0)
      val message = "3: Hello World!"
      TestDomain.ListenerTopic1.StringEvent.listener {
         assertThat(it).isEqualTo(message)
         waitCounter.incrementAndGet()
      }
      TestDomain.ListenerTopic1.StringEvent.listener {
         assertThat(it).isEqualTo(message)
         waitCounter.incrementAndGet()
      }
      TestDomain.ListenerTopic1.StringEvent.send(message)
      while(waitCounter.get() != 2) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendTestObjectObject() {
      val waitCounter = AtomicInteger(0)
      val message = TestObjectObject(TestObjectNumberText(12345, "1: Hello World!"))
      TestDomain.ListenerTopic1.TestObjectObjectEvent.listener {
         assertThat(it).isInstanceOf(TestObjectObject::class.java)
         assertThat(it.obj).isInstanceOf(TestObjectNumberText::class.java)
         assertThat(it.obj.number).isEqualTo(message.obj.number)
         assertThat(it.obj.text).isEqualTo(message.obj.text)
         waitCounter.incrementAndGet()
      }
      TestDomain.ListenerTopic1.TestObjectObjectEvent.send(message)
      while(waitCounter.get() == 0) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendParallel() {
      val waitCounter = AtomicInteger(0)
      val message1 = "4: Hello World!"
      val message2 = TestObjectObject(TestObjectNumberText(12345, "2: Hello World!"))

      TestDomain.ListenerTopic1.StringEvent.listener {
         assertThat(it).isEqualTo(message1)
         waitCounter.incrementAndGet()
      }
      TestDomain.ListenerTopic1.TestObjectObjectEvent.listener {
         assertThat(it).isInstanceOf(TestObjectObject::class.java)
         assertThat(it.obj).isInstanceOf(TestObjectNumberText::class.java)
         assertThat(it.obj.number).isEqualTo(message2.obj.number)
         assertThat(it.obj.text).isEqualTo(message2.obj.text)
         waitCounter.incrementAndGet()
      }
      TestDomain.ListenerTopic1.TestObjectObjectEvent.send(message2)
      TestDomain.ListenerTopic1.StringEvent.send(message1)
      while(waitCounter.get() != 2) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendGroups() {
      val waitCounter = AtomicInteger(0)
      val sentTimes = 10
      val message = "5: Hello World!"
      TestDomain.ListenerTopic1.StringEvent.listener("group1") {
         assertThat(it).isEqualTo(message)
         waitCounter.incrementAndGet()
      }
      TestDomain.ListenerTopic1.StringEvent.listener("group1") {
         assertThat(it).isEqualTo(message)
         waitCounter.incrementAndGet()
      }
      TestDomain.ListenerTopic1.StringEvent.listener("group2") {
         assertThat(it).isEqualTo(message)
         waitCounter.incrementAndGet()
      }
      for(i in 1..sentTimes) {
         TestDomain.ListenerTopic1.StringEvent.send(message)
      }
      while(waitCounter.get() < sentTimes * 2) {
         Thread.sleep(300)
      }
      for(i in 1..5) {
         Thread.sleep(500)
      }
      assertThat(waitCounter.get()).isEqualTo(sentTimes * 2)
   }
}