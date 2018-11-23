package com.esentri.wrabbitmq

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test

object TestDomain {
   object Topic1: WrabbitTopic("TestTopic-1") {
      val Event1 = WrabbitEvent<String>(this, "TestEvent-1")
      val Event2 = WrabbitEvent<TestObjectObject>(this, "TestEvent-2")
   }
}

class SendListenTest {

   @Test
   fun sendListenString() {
      var waitCounter = 0
      val message = "Hello World!"
      TestDomain.Topic1.Event1.listener {
         assertThat(it).isEqualTo(message)
         waitCounter++
      }
      TestDomain.Topic1.Event1.send(message)
      while(waitCounter == 0) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendTestObjectObject() {
      var waitCounter = 0
      val message = TestObjectObject(TestObjectNumberText(12345, "Hello World!"))
      TestDomain.Topic1.Event2.listener {
         assertThat(it).isInstanceOf(TestObjectObject::class.java)
         assertThat(it.obj).isInstanceOf(TestObjectNumberText::class.java)
         assertThat(it.obj.number).isEqualTo(message.obj.number)
         assertThat(it.obj.text).isEqualTo(message.obj.text)
         waitCounter++
      }
      TestDomain.Topic1.Event2.send(message)
      while(waitCounter == 0) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendParallel() {
      var waitCounter = 0
      val message1 = "Hello World!"
      TestDomain.Topic1.Event1.listener {
         assertThat(it).isEqualTo(message1)
         waitCounter++
      }
      val message2 = TestObjectObject(TestObjectNumberText(12345, "Hello World!"))
      TestDomain.Topic1.Event2.listener {
         assertThat(it).isInstanceOf(TestObjectObject::class.java)
         assertThat(it.obj).isInstanceOf(TestObjectNumberText::class.java)
         assertThat(it.obj.number).isEqualTo(message2.obj.number)
         assertThat(it.obj.text).isEqualTo(message2.obj.text)
         waitCounter++
      }
      TestDomain.Topic1.Event2.send(message2)
      TestDomain.Topic1.Event1.send(message1)
      while(waitCounter != 2) {
         Thread.sleep(300)
      }
   }
}