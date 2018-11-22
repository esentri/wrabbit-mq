package com.esentri.wrabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.util.concurrent.CompletableFuture

class WrabbitReplier<MESSAGE_TYPE, RETURN_TYPE>(
   channel: Channel,
   val replier: IMessageReplier<MESSAGE_TYPE, RETURN_TYPE>) : DefaultConsumer(channel) {

   override fun handleDelivery(consumerTag: String?,
                               envelope: Envelope,
                               properties: AMQP.BasicProperties?,
                               body: ByteArray?) {
      CompletableFuture.runAsync {
         val context: MutableMap<String, Any?> = HashMap()
         properties?.headers?.forEach { k, v ->
            if (WrabbitHeader.isWrabbitHeader(k)) return@forEach
            context[k] = v?.toString()
         }
         val message: MESSAGE_TYPE = WrabbitConverter.byteArrayToObject(body!!)
         if (replier is MessageReplier) {
            val result = replier.reply(message)
            channel.basicAck(envelope.deliveryTag, false)
         }
      }
   }
}