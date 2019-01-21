package com.esentri.wrabbitmq

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class SendReplyTest {

   @Test
   fun sendReplyStringToInt() {
      val waitCounter = AtomicInteger(0)
      val message = "12345"
      TestDomain.ReplierTopic1.StringToInt.replier {
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenApply {
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
      TestDomain.ReplierTopic1.StringToInt.replier {
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      for(i in 1..sentTimes) {
         TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenApply {
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

      TestDomain.ReplierTopic1.StringToInt.replier {
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      TestDomain.ReplierTopic1.StringToInt.replier {
         assertThat(it).isEqualTo(message)
         it.toInt()
      }

      TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenApply {
         assertThat(it).isEqualTo(message.toInt())
         waitCounter.incrementAndGet()
      }

      while(waitCounter.get() <= 0) {
         Thread.sleep(300)
      }
      Thread.sleep(1000)
      assertThat(waitCounter.get()).isEqualTo(1)
   }

}