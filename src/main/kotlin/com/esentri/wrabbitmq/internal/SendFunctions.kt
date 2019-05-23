package com.esentri.wrabbitmq.internal

import com.esentri.wrabbitmq.ThreadChannel
import com.esentri.wrabbitmq.exceptions.WrabbitReplyBasicException
import com.esentri.wrabbitmq.exceptions.WrabbitReplyTimeoutException
import com.esentri.wrabbitmq.internal.consumer.WrabbitConsumerReplyListener
import com.esentri.wrabbitmq.internal.converter.WrabbitObjectConverter
import com.rabbitmq.client.AMQP
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun SendMessage(topicName: String, sendingProperties: AMQP.BasicProperties, message: Any) {
   val newChannel = ThreadChannel()
   newChannel.basicPublish(topicName, "", sendingProperties, WrabbitObjectConverter.objectToByteArray(message))
}

fun <RETURN> SendAndReceiveMessage(
   topicName: String,
   sendingProperties: AMQP.BasicProperties,
   message: Any,
   timeoutMS: Long
): CompletableFuture<RETURN> {

   val replyQueue = ThreadChannel().queueDeclare(UUID.randomUUID().toString(), false, false, true, emptyMap()).queue

   SendMessage(topicName, sendingProperties.builder().replyTo(replyQueue).build(), message)

   return WaitForMessage<RETURN>(replyQueue, timeoutMS).handle { value, exception ->
      if (value == null) {
         val wrappedException = when (exception) {
            is TimeoutException -> WrabbitReplyTimeoutException(sendingProperties, exception)
            else -> {
               WrabbitReplyBasicException(sendingProperties, exception!!)
            }
         }
         ReplyLogger.error("{} -> {}", wrappedException.message, exception.toString())
         throw wrappedException
      }
      return@handle value
   }
}

private fun <RETURN> WaitForMessage(queueName: String, timeoutMS: Long): CompletableFuture<RETURN> {
   val replyChannel = ThreadChannel()
   val replyFuture = CompletableFuture<RETURN>()
   val consumer = WrabbitConsumerReplyListener<RETURN>(replyChannel, replyFuture)
   replyChannel.basicConsume(queueName, true, consumer)

   replyFuture.orTimeout(timeoutMS, TimeUnit.MILLISECONDS).exceptionally {
      replyChannel.basicCancel(consumer.consumerTag)
      throw it
   }
   return replyFuture
}