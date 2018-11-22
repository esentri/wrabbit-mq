package com.esentri.wrabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection

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

   fun publish(messageBody: ByteArray, additionalHeaders: Map<String, Any> = emptyMap()) {
      val amqpBasicProperties = binding.toAMQPBasicProperties()
      amqpBasicProperties.headers.putAll(additionalHeaders)
      publishChannel.basicPublish(exchange.name, binding.routingKey, amqpBasicProperties, messageBody)
   }

   fun <MESSAGE_TYPE, REPLY_TYPE> consume(autoAcknowledgement: Boolean = false, consumerTag: String = "") {
      val consumeChannel = connection.createChannel()
      consumeChannel.basicConsume(queue.name,
         autoAcknowledgement,
         consumerTag,
         WrabbitReplier<MESSAGE_TYPE, REPLY_TYPE>(consumeChannel))
   }

   fun <MESSAGE_TYPE, REPLY_TYPE> addReplier(replier: IMessageReplier<MESSAGE_TYPE, REPLY_TYPE>) {
      val consumeChannel = connection.createChannel()
      consumeChannel.basicConsume(queue.name,
         WrabbitReplier<MESSAGE_TYPE, REPLY_TYPE>(consumeChannel, replier))
   }
}
