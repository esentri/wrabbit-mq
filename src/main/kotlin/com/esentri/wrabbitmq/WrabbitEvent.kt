package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.esentri.wrabbitmq.internal.consumer.WrabbitConsumerSimple
import com.esentri.wrabbitmq.internal.converter.WrabbitObjectConverter
import com.rabbitmq.client.AMQP
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

open class WrabbitEvent<MESSAGE: Serializable>(val wrabbitTopic: WrabbitTopic, val eventName: String) {

   private val standardHeaders = WrabbitHeader.standardHeaderForEvent(eventName)
   internal val standardSendingProperties = AMQP.BasicProperties.Builder().headers(standardHeaders).build()
   private val standardListenersForEvent = listenerHeadersForEvent()

   fun send(message: MESSAGE) {
      val newChannel = NewChannel()
      newChannel.basicPublish(wrabbitTopic.topicName, "", standardSendingProperties, WrabbitObjectConverter.objectToByteArray(message!!))
      newChannel.close()
   }

   fun listener(listener: WrabbitListener<MESSAGE>) {
      val newChannel = NewChannel()
      val queueName = "$eventName.LISTENER.${UUID.randomUUID()}"
      newChannel.queueDeclare(queueName, true, true, false, emptyMap())
      newChannel.queueBind(queueName, wrabbitTopic.topicName, "", standardListenersForEvent)
      newChannel.basicConsume(queueName, true, WrabbitConsumerSimple<MESSAGE>(newChannel, listener, queueName))
   }

   private fun listenerHeadersForEvent(): Map<String, Any?> {
      val headers: MutableMap<String, Any?> = HashMap()
      headers["x-match"] = "all"
      headers[eventName] = null
      headers[WrabbitHeader.LISTENER.key] = null
      return headers
   }

}