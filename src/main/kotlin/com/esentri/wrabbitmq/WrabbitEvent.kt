package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.esentri.wrabbitmq.internal.consumer.WrabbitConsumerSimple
import com.esentri.wrabbitmq.internal.converter.WrabbitObjectConverter
import com.rabbitmq.client.AMQP
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

class WrabbitEvent<MESSAGE: Serializable>(val wrabbitTopic: WrabbitTopic, val eventName: String) {

   private val standardHeaders = WrabbitHeader.standardHeaderForEvent(eventName)
   private val standardSendingProperties = AMQP.BasicProperties.Builder().headers(standardHeaders).build()

   fun send(message: MESSAGE) {
      val newChannel = NewChannel()
      newChannel.basicPublish(wrabbitTopic.topicName, "", standardSendingProperties, WrabbitObjectConverter.objectToByteArray(message!!))
      newChannel.close()
   }

   fun sendAndReceive() {

   }

   fun replier() {

   }

   fun listener(listener: WrabbitListener<MESSAGE>) {
      val newChannel = NewChannel()
      val queueName = "$eventName.LISTENER.${UUID.randomUUID()}"
      newChannel.queueDeclare(queueName, true, false, false, emptyMap())
      newChannel.queueBind(queueName, wrabbitTopic.topicName, "", listenerHeadersForQueue())
      newChannel.basicConsume(queueName, true, WrabbitConsumerSimple<MESSAGE>(newChannel, listener))
   }

   private fun listenerHeadersForQueue(): Map<String, Any?> {
      val headers: MutableMap<String, Any?> = HashMap()
      headers["x-match"] = "all"
      headers[eventName] = null
      headers[WrabbitHeader.LISTENER.key] = null
      return headers
   }

}