package com.esentri.wrabbitmq

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test

class SendListenTest {

   @Test
   fun sendListenString() {
      var waitCounter = 0
      val message = "Hello World!"
      TestDomain.ListenerTopic1.StringEvent.listener {
         assertThat(it).isEqualTo(message)
         waitCounter++
      }
      TestDomain.ListenerTopic1.StringEvent.send(message)
      while(waitCounter == 0) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendListenString_2_times() {
      var waitCounter = 0
      val message = "Hello World!"
      TestDomain.ListenerTopic1.StringEvent.listener {
         assertThat(it).isEqualTo(message)
         waitCounter++
      }
      TestDomain.ListenerTopic1.StringEvent.send(message)
      TestDomain.ListenerTopic1.StringEvent.send(message)
      while(waitCounter != 2) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendListenString_2_listener() {
      var waitCounter = 0
      val message = "Hello World!"
      TestDomain.ListenerTopic1.StringEvent.listener {
         assertThat(it).isEqualTo(message)
         waitCounter++
      }
      TestDomain.ListenerTopic1.StringEvent.listener {
         assertThat(it).isEqualTo(message)
         waitCounter++
      }
      TestDomain.ListenerTopic1.StringEvent.send(message)
      while(waitCounter != 2) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendTestObjectObject() {
      var waitCounter = 0
      val message = TestObjectObject(TestObjectNumberText(12345, "Hello World!"))
      TestDomain.ListenerTopic1.TestObjectObjectEvent.listener {
         assertThat(it).isInstanceOf(TestObjectObject::class.java)
         assertThat(it.obj).isInstanceOf(TestObjectNumberText::class.java)
         assertThat(it.obj.number).isEqualTo(message.obj.number)
         assertThat(it.obj.text).isEqualTo(message.obj.text)
         waitCounter++
      }
      TestDomain.ListenerTopic1.TestObjectObjectEvent.send(message)
      while(waitCounter == 0) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendParallel() {
      var waitCounter = 0
      val message1 = "Hello World!"
      TestDomain.ListenerTopic1.StringEvent.listener {
         assertThat(it).isEqualTo(message1)
         waitCounter++
      }
      val message2 = TestObjectObject(TestObjectNumberText(12345, "Hello World!"))
      TestDomain.ListenerTopic1.TestObjectObjectEvent.listener {
         assertThat(it).isInstanceOf(TestObjectObject::class.java)
         assertThat(it.obj).isInstanceOf(TestObjectNumberText::class.java)
         assertThat(it.obj.number).isEqualTo(message2.obj.number)
         assertThat(it.obj.text).isEqualTo(message2.obj.text)
         waitCounter++
      }
      TestDomain.ListenerTopic1.TestObjectObjectEvent.send(message2)
      TestDomain.ListenerTopic1.StringEvent.send(message1)
      while(waitCounter != 2) {
         Thread.sleep(300)
      }
   }
}