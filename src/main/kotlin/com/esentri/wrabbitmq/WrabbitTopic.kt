package com.esentri.wrabbitmq

import com.rabbitmq.client.BuiltinExchangeType

open class WrabbitTopic(val topicName: String,
                   type: BuiltinExchangeType = BuiltinExchangeType.HEADERS,
                   durable: Boolean = true) {

   init {
      ConfigChannel.exchangeDeclare(topicName, type, durable)
   }
}