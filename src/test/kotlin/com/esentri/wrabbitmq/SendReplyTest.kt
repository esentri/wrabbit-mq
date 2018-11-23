package com.esentri.wrabbitmq

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test

class SendReplyTest {

   @Test
   fun sendReplyStringToInt() {
      var waitCounter = 0
      val message = "12345"
      TestDomain.ReplierTopic1.StringToInt.replier {
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenApply {
         assertThat(it).isEqualTo(message.toInt())
         waitCounter++
      }
      while(waitCounter == 0) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendReplyStringToInt_2_times() {
      var waitCounter = 0
      val message = "12345"
      TestDomain.ReplierTopic1.StringToInt.replier {
         assertThat(it).isEqualTo(message)
         it.toInt()
      }
      TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenApply {
         assertThat(it).isEqualTo(message.toInt())
         waitCounter++
      }
      TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenApply {
         assertThat(it).isEqualTo(message.toInt())
         waitCounter++
      }
      while(waitCounter != 2) {
         Thread.sleep(300)
      }
   }

   @Test
   fun sendReplyStringToInt_2_replier() {

      var waitCounter = 0
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
         waitCounter++
      }
      while(waitCounter <= 0) {
         Thread.sleep(300)
      }
      Thread.sleep(2000)
      assertThat(waitCounter).isEqualTo(1)
   }

}