package com.esentri.wrabbitmq.domain;

import com.esentri.wrabbitmq.WrabbitChannel;
import com.esentri.wrabbitmq.WrabbitTopic;

public final class TestDomainJava {

   public static final SimpleTopicInternal SimpleTopic = new SimpleTopicInternal();

   public static final class SimpleTopicInternal extends WrabbitTopic {

      public final NestedTopicInternal NestedTopic = new NestedTopicInternal();

      SimpleTopicInternal() {
         super("test.topic.simple");
      }

      public final WrabbitChannel<String, Integer> Event1_StringToNumber =
         new WrabbitChannel<>(this, "test.topic.simple.Event1");

      public final WrabbitChannel<Integer, String> Event2_NumberToString =
         new WrabbitChannel<>(this, "test.topic.simple.Event2");


      public static final class NestedTopicInternal extends WrabbitTopic {

         NestedTopicInternal() {
            super("test.topic.simple.nestedTopic");
         }

         public final WrabbitChannel<Integer, Integer> Event1_IncrementNumber =
            new WrabbitChannel<>(this, "test.topic.simple.nestedTopic.Event1");

         public final WrabbitChannel<Integer, Integer> Event2_DecrementNumber =
            new WrabbitChannel<>(this, "test.topic.simple.nestedTopic.Event2");
      }

   }

}
