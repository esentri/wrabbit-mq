package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.exceptions.WrabbitReplyBasicException
import com.esentri.wrabbitmq.exceptions.WrabbitReplyTimeoutException
import com.esentri.wrabbitmq.testhelper.CustomException
import com.esentri.wrabbitmq.testhelper.TestException
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.Serializable

class ExceptionTest {

   private val topic = WrabbitTopic("TestTopic-Exception")
   private fun <MESSAGE : Serializable, REPLY : Serializable> newEventWithReply() =
      NewEventWithReply<MESSAGE, REPLY>(topic)

   @Test
   fun noReplier_withDefaultTimeout() {
      val event = newEventWithReply<String, String>()
      event.sendAndReceive("hello").handle {_, e ->
         assertThat(e.cause).isInstanceOf(WrabbitReplyTimeoutException::class.java)
         assertThat(e.message)
            .contains(event.eventName)
            .contains(topic.topicName)
      }.get()
   }

   @Test
   fun noReplier_withCustomTimeout() {
      val event = newEventWithReply<String, String>()
      event.sendAndReceive("hello", 10).handle {_, e ->
         assertThat(e.cause).isInstanceOf(WrabbitReplyTimeoutException::class.java)
         assertThat(e.message)
            .contains(event.eventName)
            .contains(topic.topicName)
      }.get()
   }

   @Test
   fun replierThrowsException() {
      val exceptionMessage = "hello world"
      val event = newEventWithReply<String, String>()
      event.replier { _ ->
         throw TestException(exceptionMessage)
      }
      event.sendAndReceive("hello", 10000).handle {_, e ->
         assertThat(e.cause).isInstanceOf(WrabbitReplyBasicException::class.java)
         assertThat(e.cause!!.cause).isInstanceOf(TestException::class.java)
         assertThat(e.cause!!.cause).hasMessage(exceptionMessage)
      }.get()
   }

   @Test
   fun customException() {
      val exceptionMessage = "custom exception"
      val event = newEventWithReply<String, String>()
      event.replier { _ ->
         throw CustomException(exceptionMessage)
      }
      event.sendAndReceive("hello", 10000).handle {_, e ->
         assertThat(e.cause).isInstanceOf(WrabbitReplyBasicException::class.java)
         assertThat(e.cause!!.cause).isInstanceOf(CustomException::class.java)
         assertThat(e.cause!!.cause).hasMessage(exceptionMessage)
      }.get()
   }

}