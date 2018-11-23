package com.esentri.wrabbitmq.internal.consumer

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer

abstract class WrabbitConsumerBase(exculsiveChannel: Channel): DefaultConsumer(exculsiveChannel) {
   internal fun getContext(properties: AMQP.BasicProperties?): MutableMap<String, Any?> {
      val context: MutableMap<String, Any?> = HashMap()
      properties?.headers?.forEach { k, v ->
//         if (WrabbitHeader.isWrabbitHeader(k)) return@forEach
         context[k] = v?.toString()
      }
      return context
   }
}