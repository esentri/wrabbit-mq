package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.esentri.wrabbitmq.internal.consumer.WrabbitConsumerReplier
import com.esentri.wrabbitmq.internal.consumer.WrabbitConsumerReplyListener
import com.esentri.wrabbitmq.internal.converter.WrabbitObjectConverter
import java.io.Serializable
import java.util.*
import java.util.concurrent.CompletableFuture

class WrabbitEventWithReply<MESSAGE: Serializable, RETURN: Serializable>(
   wrabbitTopic: WrabbitTopic,
   eventName: String): WrabbitEvent<MESSAGE>(wrabbitTopic, eventName) {

   fun sendAndReceive(message: MESSAGE): CompletableFuture<RETURN> {
      val replyChannel = NewChannel()
      val replyQueue = replyChannel.queueDeclare().queue
      val correlationID = UUID.randomUUID().toString()
      sendAndReceiveInternalSend(correlationID, replyQueue, message)
      val future = CompletableFuture<RETURN>()
      replyChannel.basicConsume(replyQueue, true, WrabbitConsumerReplyListener<RETURN>(replyChannel,
         {it -> future.complete(it)},
         replyQueue))
      return future
   }

   private fun sendAndReceiveInternalSend(correlationID: String, replyQueue: String?, message: MESSAGE) {
      val sendChannel = NewChannel()
      sendChannel.basicPublish(wrabbitTopic.topicName, "",
         super.standardSendingProperties.builder().correlationId(correlationID).replyTo(replyQueue).build(),
         WrabbitObjectConverter.objectToByteArray(message))
      sendChannel.close()
   }

   fun replier(replier: WrabbitReplier<MESSAGE, RETURN>) {
      val newChannel = NewChannel()
      val queueName = "$eventName.REPLIER"
      newChannel.queueDeclare(queueName, true, false, false, emptyMap())
      newChannel.queueBind(queueName, wrabbitTopic.topicName, "", replierHeadersForEvent())
      newChannel.basicConsume(queueName, false, WrabbitConsumerReplier<MESSAGE, RETURN>(newChannel, replier, queueName))
   }

   private fun replierHeadersForEvent(): MutableMap<String, Any?> {
      val headers: MutableMap<String, Any?> = HashMap()
      headers["x-match"] = "all"
      headers[eventName] = null
      headers[WrabbitHeader.REPLIER.key] = null
      return headers
   }

}