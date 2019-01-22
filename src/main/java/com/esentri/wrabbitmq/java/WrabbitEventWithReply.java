package com.esentri.wrabbitmq.java;

import com.esentri.wrabbitmq.WrabbitTopic;
import kotlin.Unit;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class WrabbitEventWithReply<MESSAGE extends Serializable, REPLY extends Serializable> extends com.esentri.wrabbitmq.WrabbitEventWithReply<MESSAGE, REPLY> {
   public WrabbitEventWithReply(WrabbitTopic wrabbitTopic, String eventName) {
      super(wrabbitTopic, eventName);
   }

   public void listener(Consumer<MESSAGE> listener) {
      this.listener(UUID.randomUUID().toString(), it -> {
         listener.accept(it);
         return Unit.INSTANCE;
      });
   }

   public void listener(String group, Consumer<MESSAGE> listener) {
      this.listener(group, it -> {
         listener.accept(it);
         return Unit.INSTANCE;
      });
   }

}
