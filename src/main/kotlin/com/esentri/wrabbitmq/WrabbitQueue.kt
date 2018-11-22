package com.esentri.wrabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.GetResponse
import java.util.*
import java.util.concurrent.CompletableFuture

class WrabbitQueue(
   val name: String,
   val durable: Boolean = true,
   val exclusive: Boolean = false,
   val autoDelete: Boolean = false,
   val arguments: Map<String, Any> = emptyMap()
)

class WrabbitMessenger(
   val queue: WrabbitQueue,
   val exchange: WrabbitExchange,
   val binding: WrabbitBinding,
   val connection: Connection,
   val publishChannel: Channel = connection.createChannel()
) {

   fun send(messageBody: ByteArray, additionalHeaders: Map<String, Any> = emptyMap()) {
      publishChannel.basicPublish(
         exchange.name,
         binding.routingKey,
         createStandardProperties(additionalHeaders),
         messageBody)
   }

   fun sendAndReceive(messageBody: ByteArray, additionalHeaders: Map<String, Any> = emptyMap()): CompletableFuture<GetResponse> {
      return CompletableFuture.supplyAsync {
         val returnQueue = WrabbitAdmin.returnQueue()
         val amqpProperties = createStandardProperties(additionalHeaders).builder()
            .replyTo(returnQueue.name)
            .correlationId(UUID.randomUUID().toString())
            .build()
         val ownChannel = connection.createChannel()
         ownChannel.basicPublish(
            exchange.name,
            binding.routingKey,
            amqpProperties,
            messageBody)
         val reply = ownChannel.basicGet(returnQueue.name, false)
         ownChannel.close()
         return@supplyAsync reply
      }
   }

   private fun createStandardProperties(additionalHeaders: Map<String, Any>): AMQP.BasicProperties {
      val amqpBasicProperties = binding.toAMQPBasicProperties()
      amqpBasicProperties.headers.putAll(additionalHeaders)
      return amqpBasicProperties
   }

//   fun <MESSAGE_TYPE, REPLY_TYPE> consume(autoAcknowledgement: Boolean = false, consumerTag: String = "") {
//      CompletableFuture.supplyAsync {
//         val ownChannel = connection.createChannel()
//         ownChannel.basicConsume(queue.name,
//            autoAcknowledgement,
//            consumerTag,
//            WrabbitReplier<MESSAGE_TYPE, REPLY_TYPE>(ownChannel))
//      }
//   }

   fun <MESSAGE_TYPE, REPLY_TYPE> addReplier(replier: IMessageReplier<MESSAGE_TYPE, REPLY_TYPE>) {
      val consumeChannel = connection.createChannel()
      consumeChannel.basicConsume(queue.name,
         WrabbitReplier<MESSAGE_TYPE, REPLY_TYPE>(consumeChannel, replier))
   }

   fun <MESSAGE_TYPE> addListener(listener: IMessageListener<MESSAGE_TYPE>) {
      val consumeChannel = connection.createChannel()
      consumeChannel.basicConsume(queue.name,
         WrabbitListener<MESSAGE_TYPE>(consumeChannel, listener))
   }
}
