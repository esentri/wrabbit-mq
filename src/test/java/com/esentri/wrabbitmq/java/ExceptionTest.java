package com.esentri.wrabbitmq.java;

import com.esentri.wrabbitmq.WrabbitEventWithReply;
import com.esentri.wrabbitmq.WrabbitTopic;
import com.esentri.wrabbitmq.exceptions.WrabbitReplyBasicException;
import com.esentri.wrabbitmq.exceptions.WrabbitReplyTimeoutException;
import com.esentri.wrabbitmq.exceptions.WrabbitSerializationException;
import com.esentri.wrabbitmq.java.testhelper.TestException;
import com.esentri.wrabbitmq.testhelper.NonSerializableException;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static com.esentri.wrabbitmq.java.TestFunctions.Await;
import static com.esentri.wrabbitmq.java.TestFunctions.NewEventWithReply;
import static org.fest.assertions.Assertions.assertThat;

public class ExceptionTest {

   private WrabbitTopic topic = new WrabbitTopic("TestTopic-Exception");

   private <MESSAGE extends Serializable, REPLY extends Serializable> WrabbitEventWithReply<MESSAGE, REPLY> newEventWithReply() {
      return NewEventWithReply(topic);
   }

   @Test
   public void noReplier_withDefaultTimeout() throws ExecutionException, InterruptedException {
      WrabbitEventWithReply<String, String> event = newEventWithReply();
      event.sendAndReceive("hello").handle((v, e) -> {
              assertThat(e.getCause()).isInstanceOf(WrabbitReplyTimeoutException.class);
              assertThat(e.getMessage())
                 .contains(event.getEventName())
                 .contains(topic.getTopicName());
              return null;
           }).join();
   }

   @Test
   public void noReplier_withCustomTimeout() {
      WrabbitEventWithReply<String, String> event = newEventWithReply();
      event.sendAndReceive("hello", 10).handle((v, e) -> {
              assertThat(e.getCause()).isInstanceOf(WrabbitReplyTimeoutException.class);
              assertThat(e.getMessage())
                 .contains(event.getEventName())
                 .contains(topic.getTopicName());
              return null;
           }).join();
   }

   @Test
   public void replierThrowsException() {
      WrabbitEventWithReply<String, String> event = newEventWithReply();
      String exceptionMessage = "hello world";

      event.replier(it -> {
         throw new TestException(exceptionMessage);
      });

      event.sendAndReceive("hello").handle((v, e) -> {
              assertThat(e.getCause()).isInstanceOf(WrabbitReplyBasicException.class);
              assertThat(e.getCause().getCause()).isInstanceOf(TestException.class);
              assertThat(e.getCause().getCause()).hasMessage(exceptionMessage);
              return null;
           }).join();
   }

   @Test
   public void nonSerializableExceptionTest() {
      WrabbitEventWithReply<String, String> event = newEventWithReply();
      event.replier (it -> {
         throw new NonSerializableException();
      });
      event.sendAndReceive("hello", 10000).handle((v, e) -> {
         assertThat(e.getCause()).isInstanceOf(WrabbitReplyBasicException.class);
         assertThat(e.getCause().getCause()).isInstanceOf(WrabbitSerializationException.class);
         assertThat(e.getCause().getCause().getCause()).isNull();
         return null;
      }).join();
   }

}
