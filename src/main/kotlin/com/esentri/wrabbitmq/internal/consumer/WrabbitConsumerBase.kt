package com.esentri.wrabbitmq.internal.consumer

import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer

abstract class WrabbitConsumerBase(val exculsiveChannel: Channel, val exclusiveQueueName: String): DefaultConsumer(exculsiveChannel) {
   internal fun getContext(properties: AMQP.BasicProperties?): MutableMap<String, Any?> {
      val context: MutableMap<String, Any?> = HashMap()
      properties?.headers?.forEach { key, value ->
         if (WrabbitHeader.isWrabbitHeader(key)) return@forEach
         context[key] = value?.toString()
      }
      return context
   }

   fun finalize() {
      super.getChannel().queueDelete(exclusiveQueueName)
      super.getChannel().close()
   }
}