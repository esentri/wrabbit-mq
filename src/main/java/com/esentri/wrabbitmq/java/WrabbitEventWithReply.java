package com.esentri.wrabbitmq.java;

import com.esentri.wrabbitmq.WrabbitTopic;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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

   public void listener(BiConsumer<Map<String, ?>, MESSAGE> listener) {
      this.listener(UUID.randomUUID().toString(), listener);
   }

   public void listener(String group, BiConsumer<Map<String, ?>, MESSAGE> listener) {
      this.listener(group, (context, it) -> {
         listener.accept(context, it);
         return Unit.INSTANCE;
      });
   }

   public void replier2(BiFunction<Map<String, ?>, MESSAGE, REPLY> replier) {
      Function2<Map<String, ?>, MESSAGE, REPLY> function2 = replier::apply;
      this.replier(function2);
   }
}
