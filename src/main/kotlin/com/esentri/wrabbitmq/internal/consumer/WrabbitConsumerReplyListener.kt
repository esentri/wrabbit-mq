package com.esentri.wrabbitmq.internal.consumer

import com.esentri.wrabbitmq.internal.converter.WrabbitObjectConverter
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Envelope
import java.util.concurrent.CompletableFuture

class WrabbitConsumerReplyListener<MESSAGE_TYPE>(exculsiveChannel: Channel,
                                                 val future: CompletableFuture<MESSAGE_TYPE>)
   : WrabbitConsumerBase(exculsiveChannel) {
   override fun handleDelivery(consumerTag: String?, envelope: Envelope, properties: AMQP.BasicProperties?, body: ByteArray?) {
      val message: WrabbitReplyMessage<MESSAGE_TYPE> = WrabbitObjectConverter.byteArrayToObject(body!!)
      if (message.value != null) {
         future.complete(message.value)
         return
      }
      future.completeExceptionally(message.exception!!)
   }

}