package com.esentri.wrabbitmq.internal

import com.esentri.wrabbitmq.NewChannel
import com.esentri.wrabbitmq.internal.consumer.WrabbitConsumerReplyListener
import com.esentri.wrabbitmq.internal.converter.WrabbitObjectConverter
import com.rabbitmq.client.AMQP
import java.util.*
import java.util.concurrent.CompletableFuture

fun SendMessage(topicName: String, sendingProperties: AMQP.BasicProperties, message: Any) {
   val newChannel = NewChannel()
   newChannel.basicPublish(topicName, "", sendingProperties, WrabbitObjectConverter.objectToByteArray(message))
   newChannel.close()
}

fun <RETURN> SendAndReceiveMessage(topicName: String, sendingProperties: AMQP.BasicProperties, message: Any): CompletableFuture<RETURN> {
   val replyChannel = NewChannel()
   val replyQueue = replyChannel.queueDeclare().queue
   val correlationID = UUID.randomUUID().toString()
   val sendChannel = NewChannel()
   sendChannel.basicPublish(topicName, "",
      sendingProperties.builder().correlationId(correlationID).replyTo(replyQueue).build(),
      WrabbitObjectConverter.objectToByteArray(message))
   sendChannel.close()
   val future = CompletableFuture<RETURN>()
   replyChannel.basicConsume(replyQueue, true, WrabbitConsumerReplyListener<RETURN>(replyChannel,
      {it -> future.complete(it)},
      replyQueue))
   return future
}
