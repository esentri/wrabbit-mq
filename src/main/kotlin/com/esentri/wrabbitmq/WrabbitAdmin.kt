package com.esentri.wrabbitmq

import com.rabbitmq.client.Channel
import java.util.*

// https://www.rabbitmq.com/java-client.html
// https://www.rabbitmq.com/api-guide.html#exchanges-and-queues

enum class WrabbitExchangeType(val rabbitKey: String) {
   HEADER("header")
}

object WrabbitAdmin {
   val connection = WrabbitConnectionFactory.newConnection()
   val channel: Channel = connection.createChannel()

   fun declareExchange(type: WrabbitExchangeType, name: String, durable: Boolean = true): WrabbitExchange {
      channel.exchangeDeclare(name, type.rabbitKey, durable)
      return WrabbitExchange(type = type, name = name)
   }

   fun declareQueue(name: String = UUID.randomUUID().toString(),
                    durable: Boolean = true,
                    exclusive: Boolean = false,
                    autoDelete: Boolean = false,
                    arguments: Map<String, Any> = emptyMap()): WrabbitQueue {
      channel.queueDeclare(name, durable, exclusive, autoDelete, arguments)
      return WrabbitQueue(name = name)
   }

   fun bindQueue(exchange: WrabbitExchange,
                 queue: WrabbitQueue,
                 binding: WrabbitBinding): WrabbitMessenger {
      channel.queueBind(queue.name,
         exchange.name,
         binding.routingKey,
         binding.bindingArguments)
      return WrabbitMessenger(queue, exchange, binding, this.connection.createChannel())
   }

   fun createConsumer(queue: WrabbitQueue,
                      autoAcknowledgement: Boolean = true,
                      consumerTag: String = "") {
      channel.basicConsume(queue.name,
         autoAcknowledgement,
         consumerTag,
         WrabbitReplier(connection.createChannel())
      )
   }

}
