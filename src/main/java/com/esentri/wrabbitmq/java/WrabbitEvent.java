package com.esentri.wrabbitmq.java;

import com.esentri.wrabbitmq.WrabbitTopic;
import kotlin.Unit;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Consumer;

public class WrabbitEvent<MESSAGE extends Serializable> extends com.esentri.wrabbitmq.WrabbitEvent<MESSAGE> {
   public WrabbitEvent(WrabbitTopic wrabbitTopic, String eventName) {
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
