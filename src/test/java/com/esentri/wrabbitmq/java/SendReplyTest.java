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
}
