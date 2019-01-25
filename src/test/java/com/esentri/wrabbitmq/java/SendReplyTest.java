package com.esentri.wrabbitmq.java;

import com.esentri.wrabbitmq.WrabbitTopic;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.esentri.wrabbitmq.java.TestFunctions.Await;
import static org.fest.assertions.Assertions.assertThat;

public class SendReplyTest {

   private final WrabbitTopic topic = new WrabbitTopic("TestTopic-Replier-Java");
   private <MESSAGE extends Serializable, REPLY extends Serializable> WrabbitEventWithReply<MESSAGE, REPLY> newEvent() {
      return new WrabbitEventWithReply<>(topic, UUID.randomUUID().toString());
   }

   @Test
   public void sendReplyStringToInt() {
      final WrabbitEventWithReply<String, Integer> event = newEvent();
      final CountDownLatch countDownLatch = new CountDownLatch(1);
      final int intValue = 12345;
      final String message = Integer.toString(intValue);

      event.replier(it -> {
         assertThat(it).isEqualTo(message);
         return Integer.parseInt(it);
      });
      event.sendAndReceive(message).thenAccept(it -> {
         assertThat(it).isEqualTo(intValue);
         countDownLatch.countDown();
      });

      Await(countDownLatch);
   }

   @Test
   public void sendReplyStringToInt_X_times() {
      final WrabbitEventWithReply<String, Integer> event = newEvent();
      final int intValue = 12345;
      final String message = Integer.toString(intValue);
      final int sentTimes = 100;
      final CountDownLatch countDownLatch = new CountDownLatch(sentTimes);

      TestDomain.ReplierTopic1.StringToInt.replier(it -> {
         assertThat(it).isEqualTo(message);
         return Integer.parseInt(it);
      });

      IntStream.range(0, sentTimes).forEach(it -> {
         TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenAccept(it2 -> {
            assertThat(it2).isEqualTo(intValue);
            countDownLatch.countDown();
         });
      });

      Await(countDownLatch);
   }

   @Test
   public void sendReplyStringToInt_2_replier_default() throws InterruptedException {
      final WrabbitEventWithReply<String, Integer> event = newEvent();
      // AtomicInteger is used to check after all expected events if any unexpected event occurs.
      // This is not possible with CountDownLatch.
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final int intValue = 12345;
      final String message = Integer.toString(intValue);

      event.replier(it -> {
         assertThat(it).isEqualTo(message);
         return Integer.parseInt(it);
      });
      event.replier(it -> {
         assertThat(it).isEqualTo(message);
         return Integer.parseInt(it);
      });

      event.sendAndReceive(message).thenAccept(it -> {
         assertThat(it).isEqualTo(intValue);
         waitCounter.incrementAndGet();
      });

      while(waitCounter.get() <= 0) {
         Thread.sleep(300);
      }

      Thread.sleep(1000);
      assertThat(waitCounter.get()).isEqualTo(1);
   }

   @Test
   public void sendAndReplyWithContext() {
      final WrabbitEventWithReply<TestObjectObject, String> event = newEvent();
      final CountDownLatch countDownLatch = new CountDownLatch(1);
      final TestObjectObject message = new TestObjectObject(new TestObjectNumberText(12345, "hello world"));
      final String propertyKey = "test";
      final String propertyValue = "property";

      event.replier((context, it) -> {
         assertThat(it.getObj().getNumber()).isEqualTo(message.getObj().getNumber());
         assertThat(it.getObj().getText()).isEqualTo(message.getObj().getText());
         assertThat(context.get(propertyKey).toString()).isEqualToIgnoringCase(propertyValue);
         countDownLatch.countDown();
         return it.getObj().getText();
      });
      event
         .messageBuilder()
         .property(propertyKey, propertyValue)
         .sendAndReceive(message)
         .thenAccept(it -> assertThat(it).isEqualTo(message.getObj().getText()));

      Await(countDownLatch);
   }
}
