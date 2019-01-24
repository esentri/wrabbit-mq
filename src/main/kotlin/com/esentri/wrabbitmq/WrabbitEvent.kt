package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.esentri.wrabbitmq.internal.SendMessage
import com.esentri.wrabbitmq.internal.consumer.WrabbitConsumerSimple
import com.rabbitmq.client.AMQP
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

open class WrabbitEvent<MESSAGE : Serializable>(val wrabbitTopic: WrabbitTopic, val eventName: String) {

   private val standardSendingHeaders = WrabbitHeader.standardHeaderForEvent(wrabbitTopic.topicName, eventName)
   internal val standardSendingProperties = AMQP.BasicProperties.Builder().headers(standardSendingHeaders).build()
   private val standardListenerHeadersForEvent = listenerHeadersForEvent()

   fun send(message: MESSAGE) {
      SendMessage(wrabbitTopic.topicName, standardSendingProperties, message)
   }

   open fun messageBuilder() = WrabbitMessageBuilder<MESSAGE>(wrabbitTopic.topicName, standardSendingProperties)

   fun listener(group: String = UUID.randomUUID().toString(), listener: WrabbitListener<MESSAGE>) {
      this.listener(group) { _, message ->
         listener(message)
      }
   }

   fun listener(group: String = UUID.randomUUID().toString(), listener: WrabbitListenerWithContext<MESSAGE>) {
      val newChannel = NewChannel()
      val queueName = "${wrabbitTopic.topicName}.$eventName.LISTENER.$group"
      newChannel.queueDeclare(queueName, true, false, false, emptyMap())
      newChannel.queueBind(queueName, wrabbitTopic.topicName, "", standardListenerHeadersForEvent)
      newChannel.basicConsume(queueName, true, WrabbitConsumerSimple(newChannel, listener, queueName))
   }

   private fun listenerHeadersForEvent(): Map<String, Any?> {
      val headers: MutableMap<String, Any?> = HashMap()
      headers["x-match"] = "all"
      headers[eventName] = null
      headers[WrabbitHeader.LISTENER.key] = null
      return headers
   }

}