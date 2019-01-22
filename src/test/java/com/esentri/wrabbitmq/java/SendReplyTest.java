package com.esentri.wrabbitmq.java;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.fest.assertions.Assertions.assertThat;

public class SendReplyTest {

   @Test
   public void sendReplyStringToInt() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final int intValue = 12345;
      final String message = Integer.toString(intValue);

      TestDomain.ReplierTopic1.StringToInt.replier(it -> {
         assertThat(it).isEqualTo(message);
         return Integer.parseInt(it);
      });
      TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenAccept(it -> {
         assertThat(it).isEqualTo(intValue);
         waitCounter.incrementAndGet();
      });

      while(waitCounter.get() == 0) {
         Thread.sleep(300);
      }
   }

   @Test
   public void sendReplyStringToInt_X_times() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final int intValue = 12345;
      final String message = Integer.toString(intValue);
      final int sentTimes = 100;

      TestDomain.ReplierTopic1.StringToInt.replier(it -> {
         assertThat(it).isEqualTo(message);
         return Integer.parseInt(it);
      });

      IntStream.range(0, sentTimes).forEach(it -> {
         TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenAccept(it2 -> {
            assertThat(it2).isEqualTo(intValue);
            waitCounter.incrementAndGet();
         });
      });

      while(waitCounter.get() < sentTimes) {
         Thread.sleep(300);
      }
   }

   @Test
   public void sendReplyStringToInt_2_replier_default() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final int intValue = 12345;
      final String message = Integer.toString(intValue);

      TestDomain.ReplierTopic1.StringToInt.replier(it -> {
         assertThat(it).isEqualTo(message);
         return Integer.parseInt(it);
      });
      TestDomain.ReplierTopic1.StringToInt.replier(it -> {
         assertThat(it).isEqualTo(message);
         return Integer.parseInt(it);
      });

      TestDomain.ReplierTopic1.StringToInt.sendAndReceive(message).thenAccept(it -> {
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
   public void sendAndReplyWithContext() throws InterruptedException {
      final AtomicInteger waitCounter = new AtomicInteger(0);
      final TestObjectObject message = new TestObjectObject(new TestObjectNumberText(12345, "hello world"));
      final String propertyKey = "test";
      final String propertyValue = "property";

      TestDomain.ReplierTopic1.TestObjectObjectToString.replier2((context, it) -> {
         assertThat(it.getObj().getNumber()).isEqualTo(message.getObj().getNumber());
         assertThat(it.getObj().getText()).isEqualTo(message.getObj().getText());
         assertThat(context.get(propertyKey).toString()).isEqualToIgnoringCase(propertyValue);
         waitCounter.incrementAndGet();
         return it.getObj().getText();
      });
      TestDomain.ReplierTopic1.TestObjectObjectToString
         .messageBuilder()
         .property(propertyKey, propertyValue)
         .sendAndReceive(message)
         .thenAccept(it -> assertThat(it).isEqualTo(message.getObj().getText()));

      while (waitCounter.get() == 0) {
         Thread.sleep(300);
      }
   }
}
