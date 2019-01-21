package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.esentri.wrabbitmq.internal.consumer.WrabbitConsumerSimple
import com.rabbitmq.client.BuiltinExchangeType
import java.util.*

open class WrabbitTopic(val topicName: String,
                   type: BuiltinExchangeType = BuiltinExchangeType.HEADERS,
                        durable: Boolean = true) {

   private val standardListenerHeadersForTopic = listenerHeadersForEvent()

   init {
      ConfigChannel.exchangeDeclare(topicName, type, durable)
   }

   fun listener(listener: WrabbitListener<Any>) {
      val newChannel = NewChannel()
      val queueName = "$topicName.LISTENER.${UUID.randomUUID()}"
      newChannel.queueDeclare(queueName, true, true, false, emptyMap())
      newChannel.queueBind(queueName, topicName, "", standardListenerHeadersForTopic)
      newChannel.basicConsume(queueName, true, WrabbitConsumerSimple<Any>(newChannel, listener, queueName))
   }

   private fun listenerHeadersForEvent(): Map<String, Any?> {
      val headers: MutableMap<String, Any?> = HashMap()
      headers["x-match"] = "all"
      headers[topicName] = null
      headers[WrabbitHeader.LISTENER.key] = null
      return headers
   }
}