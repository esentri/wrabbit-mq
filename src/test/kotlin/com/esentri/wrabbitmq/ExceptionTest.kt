package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.exceptions.WrabbitBasicReplyException
import com.esentri.wrabbitmq.testhelper.TestException
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.Serializable
import java.util.concurrent.CountDownLatch

class ExceptionTest {

   private val topic = WrabbitTopic("TestTopic-Exception")
   private fun <MESSAGE : Serializable, REPLY : Serializable> newEventWithReply() =
      NewEventWithReply<MESSAGE, REPLY>(topic)

   @Test
   fun noReplier_withDefaultTimeout() {
      val countDownLatch = CountDownLatch(1)
      val event = newEventWithReply<String, String>()
      event.sendAndReceive("hello").thenAccept {
         countDownLatch.countDown()
      }.exceptionally {
         assertThat(it.cause).isInstanceOf(WrabbitBasicReplyException::class.java)
         assertThat(it.message)
            .contains(event.eventName)
            .contains(topic.topicName)
         countDownLatch.countDown()
         null
      }
      Await(countDownLatch)
   }

   @Test
   fun noReplier_withCustomTimeout() {
      val countDownLatch = CountDownLatch(1)
      val event = newEventWithReply<String, String>()
      event.sendAndReceive("hello", 10).thenAccept {
         countDownLatch.countDown()
      }.exceptionally {
         assertThat(it.cause).isInstanceOf(WrabbitBasicReplyException::class.java)
         assertThat(it.message)
            .contains(event.eventName)
            .contains(topic.topicName)
         countDownLatch.countDown()
         null
      }
      Await(countDownLatch)
   }

   @Test
   fun replierThrowsException() {
      val countDownLatch = CountDownLatch(1)
      val exceptionMessage = "hello world"
      val event = newEventWithReply<String, String>()
      event.replier { _ ->
         throw TestException(exceptionMessage)
      }
      event.sendAndReceive("hello", 10000).thenAccept {
         // never happens
      }.exceptionally {
         assertThat(it.cause).isInstanceOf(WrabbitBasicReplyException::class.java)
         assertThat(it.cause!!.cause).isInstanceOf(TestException::class.java)
         assertThat(it.cause!!.cause).hasMessage(exceptionMessage)
         countDownLatch.countDown()
         null
      }

      Await(countDownLatch)
   }
}