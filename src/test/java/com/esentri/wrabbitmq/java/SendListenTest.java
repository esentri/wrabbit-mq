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

public class SendListenTest {

   private final WrabbitTopic topic = new WrabbitTopic("TestTopic-Listener-Java");

   private <MESSAGE extends Serializable> WrabbitEvent<MESSAGE> newEvent() {
      return new WrabbitEvent<>(topic, UUID.randomUUID().toString());
   }

   @Test
   public void sendListenString() {
      final WrabbitEvent<String> event = newEvent();
      final CountDownLatch countDownLatch = new CountDownLatch(1);
      final String message = "1: Hello World!";

      event.listener(it -> {
         assertThat(it).isEqualTo(message);
         countDownLatch.countDown();
      });
      event.send(message);

      Await(countDownLatch);
   }

   @Test
   public void sendListenString_X_times() {
      final WrabbitEvent<String> event = newEvent();
      final String message = "2: Hello World!";
      final int sentTimes = 1000;
      final CountDownLatch countDownLatch = new CountDownLatch(sentTimes);

      event.listener(it -> {
         assertThat(it).isEqualTo(message);
         countDownLatch.countDown();
      });
      IntStream.range(0, sentTimes).forEach(it ->
         event.send(message)
      );

      Await(countDownLatch);
   }

   @Test
   public void sendListenString_2_listener() {
      final WrabbitEvent<String> event = newEvent();
      final CountDownLatch countDownLatch = new CountDownLatch(2);
      final String message = "3: Hello World!";

      event.listener(it -> {
         assertThat(it).isEqualTo(message);
         countDownLatch.countDown();
      });
      event.listener(it -> {
         assertThat(it).isEqualTo(message);
         countDownLatch.countDown();
      });
      event.send(message);

      Await(countDownLatch);
   }

   @Test
   public void sendTestObjectObject() {
      final WrabbitEvent<TestObjectObject> event = newEvent();
      final CountDownLatch countDownLatch = new CountDownLatch(1);
      final TestObjectObject message = new TestObjectObject(new TestObjectNumberText(12345, "1: Hello World!"));

      event.listener(it -> {
         assertThat(it).isInstanceOf(TestObjectObject.class);
         assertThat(it.getObj()).isInstanceOf(TestObjectNumberText.class);
         assertThat(it.getObj().getNumber()).isEqualTo(message.getObj().getNumber());
         assertThat(it.getObj().getText()).isEqualTo(message.getObj().getText());
         countDownLatch.countDown();
      });
      event.send(message);

      Await(countDownLatch);
   }

   @Test
   public void sendParallel() {
      final WrabbitEvent<String> event1 = newEvent();
      final WrabbitEvent<TestObjectObject> event2 = newEvent();
      final CountDownLatch countDownLatch = new CountDownLatch(2);
      final String message1 = "3: Hello World!";
      final TestObjectObject message2 = new TestObjectObject(new TestObjectNumberText(12345, "2: Hello World!"));

      event1.listener(it -> {
         assertThat(it).isEqualTo(message1);
         countDownLatch.countDown();
      });
      event2.listener(it -> {
         assertThat(it).isInstanceOf(TestObjectObject.class);
         assertThat(it.getObj()).isInstanceOf(TestObjectNumberText.class);
         assertThat(it.getObj().getNumber()).isEqualTo(message2.getObj().getNumber());
         assertThat(it.getObj().getText()).isEqualTo(message2.getObj().getText());
         countDownLatch.countDown();
      });
      event1.send(message1);
      event2.send(message2);

      Await(countDownLatch);
   }

   @Test
   public void sendGroups() throws InterruptedException {
      final WrabbitEvent<String> event = newEvent();
      // AtomicInteger is used to check if after the expected events others will occure.
      // This would not be possible with CountDownLatch.
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final String message = "5: Hello World!";
      final int sentTimes = 10;

      event.listener("group1", it -> {
         assertThat(it).isEqualTo(message);
         waitCounter.incrementAndGet();
      });
      event.listener("group1", it -> {
         assertThat(it).isEqualTo(message);
         waitCounter.incrementAndGet();
      });
      event.listener("group2", it -> {
         assertThat(it).isEqualTo(message);
         waitCounter.incrementAndGet();
      });

      IntStream.range(0, sentTimes).forEach(it -> event.send(message));

      while(waitCounter.get() < sentTimes * 2) {
         Thread.sleep(300);
      }

      for(int i = 0; i < 5; i++) {
         Thread.sleep(500);
      }

      assertThat(waitCounter.get()).isEqualTo(sentTimes * 2);
   }

   @Test
   public void sendAndListenWithContext() {
      final WrabbitEvent<String> event = newEvent();
      final CountDownLatch countDownLatch = new CountDownLatch(1);
      final String message = "6: Hello World!";
      String propertyKey = "test";
      String propertyValue = "property";

      event.listener((context, it) -> {
         assertThat(it).isEqualTo(message);
         assertThat(context.get(propertyKey).toString()).isEqualToIgnoringCase(propertyValue);
         countDownLatch.countDown();
      });
      event
         .messageBuilder()
         .property(propertyKey, propertyValue)
         .send(message);

      Await(countDownLatch);
   }
}
