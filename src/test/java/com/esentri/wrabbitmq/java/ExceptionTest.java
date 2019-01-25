package com.esentri.wrabbitmq.java;

import com.esentri.wrabbitmq.WrabbitEventWithReply;
import com.esentri.wrabbitmq.WrabbitTopic;
import com.esentri.wrabbitmq.exceptions.WrabbitBasicReplyException;
import com.esentri.wrabbitmq.java.testhelper.TestException;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

import static com.esentri.wrabbitmq.java.TestFunctions.Await;
import static com.esentri.wrabbitmq.java.TestFunctions.NewEventWithReply;
import static org.fest.assertions.Assertions.assertThat;

public class ExceptionTest {

   private WrabbitTopic topic = new WrabbitTopic("TestTopic-Exception");

   private <MESSAGE extends Serializable, REPLY extends Serializable> WrabbitEventWithReply<MESSAGE, REPLY> newEventWithReply() {
      return NewEventWithReply(topic);
   }

   @Test
   public void noReplier_withDefaultTimeout() {
      CountDownLatch countDownLatch = new CountDownLatch(1);
      WrabbitEventWithReply<String, String> event = newEventWithReply();
      event.sendAndReceive("hello").thenAccept(it -> { })
           .exceptionally(it -> {
              assertThat(it.getCause()).isInstanceOf(WrabbitBasicReplyException.class);
              assertThat(it.getMessage())
                 .contains(event.getEventName())
                 .contains(topic.getTopicName());
              countDownLatch.countDown();
              return null;
           });
      Await(countDownLatch);
   }

   @Test
   public void noReplier_withCustomTimeout() {
      CountDownLatch countDownLatch = new CountDownLatch(1);
      WrabbitEventWithReply<String, String> event = newEventWithReply();
      event.sendAndReceive("hello", 10).thenAccept(it -> { })
           .exceptionally(it -> {
              assertThat(it.getCause()).isInstanceOf(WrabbitBasicReplyException.class);
              assertThat(it.getMessage())
                 .contains(event.getEventName())
                 .contains(topic.getTopicName());
              countDownLatch.countDown();
              return null;
           });
      Await(countDownLatch);
   }

   @Test
   public void replierThrowsException() {
      CountDownLatch countDownLatch = new CountDownLatch(1);
      WrabbitEventWithReply<String, String> event = newEventWithReply();
      String exceptionMessage = "hello world";

      event.replier(it -> {
         throw new TestException(exceptionMessage);
      });

      event.sendAndReceive("hello").thenAccept(it -> { })
           .exceptionally(it -> {
              assertThat(it.getCause()).isInstanceOf(WrabbitBasicReplyException.class);
              assertThat(it.getCause().getCause()).isInstanceOf(TestException.class);
              assertThat(it.getCause().getCause()).hasMessage(exceptionMessage);
              countDownLatch.countDown();
              return null;
           });

      Await(countDownLatch);
   }

}
