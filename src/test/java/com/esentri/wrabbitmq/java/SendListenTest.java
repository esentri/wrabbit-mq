package com.esentri.wrabbitmq.java;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.fest.assertions.Assertions.assertThat;

public class SendListenTest {

   @Test
   public void sendListenString() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final String message = "1: Hello World!";

      TestDomain.ListenerTopic1.StringEvent.listener(it -> {
         assertThat(it).isEqualTo(message);
         waitCounter.incrementAndGet();
      });
      TestDomain.ListenerTopic1.StringEvent.send(message);

      while(waitCounter.get() == 0) {
         Thread.sleep(300);
      }
   }

   @Test
   public void sendListenString_X_times() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final String message = "2: Hello World!";
      final int sentTimes = 1000;

      TestDomain.ListenerTopic1.StringEvent.listener(it -> {
         assertThat(it).isEqualTo(message);
         waitCounter.incrementAndGet();
      });
      IntStream.range(0, sentTimes).forEach(it ->
         TestDomain.ListenerTopic1.StringEvent.send(message)
      );

      while(waitCounter.get() < sentTimes) {
         Thread.sleep(300);
      }
   }

   @Test
   public void sendListenString_2_listener() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final String message = "3: Hello World!";

      TestDomain.ListenerTopic1.StringEvent.listener(it -> {
         assertThat(it).isEqualTo(message);
         waitCounter.incrementAndGet();
      });
      TestDomain.ListenerTopic1.StringEvent.listener(it -> {
         assertThat(it).isEqualTo(message);
         waitCounter.incrementAndGet();
      });
      TestDomain.ListenerTopic1.StringEvent.send(message);

      while(waitCounter.get() != 2) {
         Thread.sleep(300);
      }
   }

   @Test
   public void sendTestObjectObject() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final TestObjectObject message = new TestObjectObject(new TestObjectNumberText(12345, "1: Hello World!"));

      TestDomain.ListenerTopic1.TestObjectObjectEvent.listener(it -> {
         assertThat(it).isInstanceOf(TestObjectObject.class);
         final TestObjectObject castedIt = (TestObjectObject) it;
         assertThat(castedIt.getObj()).isInstanceOf(TestObjectNumberText.class);
         assertThat(castedIt.getObj().getNumber()).isEqualTo(message.getObj().getNumber());
         assertThat(castedIt.getObj().getText()).isEqualTo(message.getObj().getText());
         waitCounter.incrementAndGet();
      });
      TestDomain.ListenerTopic1.TestObjectObjectEvent.send(message);

      while(waitCounter.get() == 0) {
         Thread.sleep(300);
      }
   }

   @Test
   public void sendParallel() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final String message1 = "3: Hello World!";
      final TestObjectObject message2 = new TestObjectObject(new TestObjectNumberText(12345, "2: Hello World!"));

      TestDomain.ListenerTopic1.StringEvent.listener(it -> {
         assertThat(it).isEqualTo(message1);
         waitCounter.incrementAndGet();
      });

      TestDomain.ListenerTopic1.TestObjectObjectEvent.listener(it -> {
         assertThat(it).isInstanceOf(TestObjectObject.class);
         assertThat(it.getObj()).isInstanceOf(TestObjectNumberText.class);
         assertThat(it.getObj().getNumber()).isEqualTo(message2.getObj().getNumber());
         assertThat(it.getObj().getText()).isEqualTo(message2.getObj().getText());
         waitCounter.incrementAndGet();
      });
      TestDomain.ListenerTopic1.TestObjectObjectEvent.send(message2);
      TestDomain.ListenerTopic1.StringEvent.send(message1);

      while(waitCounter.get() != 2) {
         Thread.sleep(300);
      }
   }

   @Test
   public void sendGroups() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final String message = "5: Hello World!";
      final int sentTimes = 10;

      TestDomain.ListenerTopic1.StringEvent.listener("group1", it -> {
         assertThat(it).isEqualTo(message);
         waitCounter.incrementAndGet();
      });
      TestDomain.ListenerTopic1.StringEvent.listener("group1", it -> {
         assertThat(it).isEqualTo(message);
         waitCounter.incrementAndGet();
      });
      TestDomain.ListenerTopic1.StringEvent.listener("group2", it -> {
         assertThat(it).isEqualTo(message);
         waitCounter.incrementAndGet();
      });

      IntStream.range(0, sentTimes).forEach(it -> TestDomain.ListenerTopic1.StringEvent.send(message));

      while(waitCounter.get() < sentTimes * 2) {
         Thread.sleep(300);
      }

      for(int i = 0; i < 5; i++) {
         Thread.sleep(500);
      }

      assertThat(waitCounter.get()).isEqualTo(sentTimes * 2);
   }

   @Test
   public void sendAndListenWithContext() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final String message = "6: Hello World!";
      String propertyKey = "test";
      String propertyValue = "property";

      TestDomain.ListenerTopic1.StringEvent.listener((context, it) -> {
         assertThat(it).isEqualTo(message);
         assertThat(context.get(propertyKey).toString()).isEqualToIgnoringCase(propertyValue);
         waitCounter.incrementAndGet();
      });
      TestDomain.ListenerTopic1.StringEvent
         .messageBuilder()
         .property(propertyKey, propertyValue)
         .send(message);

      while (waitCounter.get() == 0) {
         Thread.sleep(300);
      }
   }
}
