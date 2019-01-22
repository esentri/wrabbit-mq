package com.esentri.wrabbitmq.java;

import com.esentri.wrabbitmq.WrabbitTopic;

public final class TestDomain {

   private TestDomain(){}

   public static final SimpleListenerTopic ListenerTopic1 = new SimpleListenerTopic();
   public static final SimpleReplierTopic ReplierTopic1 = new SimpleReplierTopic();

   public static final class SimpleListenerTopic extends WrabbitTopic {
      private SimpleListenerTopic() {
         super("Test-Topic-1");
      }

      public WrabbitEvent<String> StringEvent = new WrabbitEvent<>(this, "TT1-TE-1");
      public WrabbitEvent<TestObjectObject> TestObjectObjectEvent = new WrabbitEvent<>(this, "TT1-TE-2");
   }

   public static final class SimpleReplierTopic extends WrabbitTopic {
      private SimpleReplierTopic() {
         super("Test-Topic-2");
      }

      public WrabbitEventWithReply<String, Integer> StringToInt = new WrabbitEventWithReply<>(this, "TT2-TE1");
      public WrabbitEventWithReply<TestObjectObject, String> TestObjectObjectToString = new WrabbitEventWithReply<>(this, "TT2-TE2");
   }
}
