package com.esentri.wrabbitmq

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class SendReplyTest {

   @Test
   fun sendReplyStringToInt() {
      val waitCounter = AtomicInteger(0)
      val message = "12345"
      TestDomain.ReplierTopic1.StringToInt.replier { it ->
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenAccept {
         assertThat(it).isEqualTo(message.toInt())
         waitCounter.incrementAndGet()
      }
      while(waitCounter.get() == 0) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendReplyStringToInt_X_times() {
      val waitCounter = AtomicInteger(0)
      val sentTimes = 100

      val message = "12345"
      TestDomain.ReplierTopic1.StringToInt.replier { it ->
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      for(i in 1..sentTimes) {
         TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenAccept {
            assertThat(it).isEqualTo(message.toInt())
            waitCounter.incrementAndGet()
         }
      }
      while(waitCounter.get() < sentTimes) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendReplyStringToInt_2_replier_default() {
      val waitCounter = AtomicInteger(0)
      val message = "12345"

      TestDomain.ReplierTopic1.StringToInt.replier { it ->
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      TestDomain.ReplierTopic1.StringToInt.replier { it ->
         assertThat(it).isEqualTo(message)
         it.toInt()
      }

      TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenAccept {
         assertThat(it).isEqualTo(message.toInt())
         waitCounter.incrementAndGet()
      }

      while(waitCounter.get() <= 0) {
         Thread.sleep(300)
      }
      Thread.sleep(1000)
      assertThat(waitCounter.get()).isEqualTo(1)
   }

   @Test
   fun sendAndReplyWithContext() {
      val waitCounter = AtomicInteger(0)
      val message = TestObjectObject(TestObjectNumberText(12345, "hello world"))
      val propertyKey = "test"
      val propertyValue = "property"

      TestDomain.ReplierTopic1.TestObjectObjectToString.replier { context, it ->
         assertThat(it.obj.number).isEqualTo(message.obj.number)
         assertThat(it.obj.text).isEqualTo(message.obj.text)
         assertThat(context[propertyKey].toString()).isEqualToIgnoringCase(propertyValue)
         waitCounter.incrementAndGet()
         it.obj.text
      }
      TestDomain.ReplierTopic1.TestObjectObjectToString
         .messageBuilder()
         .property(propertyKey, propertyValue)
         .sendAndReceive(message)
         .thenAccept {
            assertThat(it).isEqualTo(message.obj.text)
         }

      while (waitCounter.get() == 0) {
         Thread.sleep(300)
      }
   }

}