package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.internal.consumer.WrabbitConsumerSimple
import com.rabbitmq.client.BuiltinExchangeType
import java.util.*

open class WrabbitTopic {

   val topicName: String
   private val standardListenerHeadersForTopic = listenerHeadersForEvent()

   @JvmOverloads
   constructor(topicName: String,
               type: BuiltinExchangeType = BuiltinExchangeType.HEADERS,
               durable: Boolean = true) {
      this.topicName = topicName
      ConfigChannel.exchangeDeclare(topicName, type, durable)
   }

   fun listener(listener: WrabbitListener<Any>) {
      this.listener { _, message-> listener(message) }
   }

   fun listener(group: String = UUID.randomUUID().toString(), listener: WrabbitListenerWithContext<Any>) {
      val newChannel = ThreadChannel()
      val queueName = "$topicName.LISTENER.$group"
      newChannel.queueDeclare(queueName, true, true, false, emptyMap())
      newChannel.queueBind(queueName, topicName, "", standardListenerHeadersForTopic)
      newChannel.basicConsume(queueName, true, WrabbitConsumerSimple(newChannel, listener))
   }

   private fun listenerHeadersForEvent(): Map<String, Any?> {
      val headers: MutableMap<String, Any?> = HashMap()
      headers["x-match"] = "all"
      headers[topicName] = null
      return headers
   }
}