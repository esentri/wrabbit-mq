package com.esentri.wrabbitmq.java;

import com.esentri.wrabbitmq.WrabbitTopic;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.Assertions.assertThat;

public class TestFunctions {
   public static void Await(CountDownLatch latch) {
      try {
         assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
      } catch (InterruptedException e) {
         throw new RuntimeException("CountDownLatch was interrupted.");
      }
   }

   public static <MESSAGE extends Serializable, REPLY extends Serializable> WrabbitEventWithReply<MESSAGE, REPLY> NewEventWithReply(WrabbitTopic topic) {
      return new WrabbitEventWithReply<MESSAGE, REPLY>(topic, UUID.randomUUID().toString());
   }
}
