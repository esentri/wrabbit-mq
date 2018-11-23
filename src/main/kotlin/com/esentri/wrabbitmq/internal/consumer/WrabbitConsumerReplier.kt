package com.esentri.wrabbitmq.internal.consumer

import com.esentri.wrabbitmq.WrabbitReplier
import com.esentri.wrabbitmq.internal.converter.WrabbitObjectConverter
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Envelope

class WrabbitConsumerReplier<MESSAGE_TYPE, RETURN_TYPE>(exculsiveChannel: Channel,
                                           val wrabbitReplier: WrabbitReplier<MESSAGE_TYPE, RETURN_TYPE>,
                                           exclusiveQueueName: String) : WrabbitConsumerBase(exculsiveChannel, exclusiveQueueName) {
   override fun handleDelivery(consumerTag: String?, envelope: Envelope, properties: AMQP.BasicProperties?, body: ByteArray?) {
      val message: MESSAGE_TYPE = WrabbitObjectConverter.byteArrayToObject(body!!)
      val result = wrabbitReplier(message)
      super.exculsiveChannel.basicPublish("",
         properties!!.replyTo,
         AMQP.BasicProperties
            .Builder()
            .correlationId(properties.correlationId)
            .build(),
         WrabbitObjectConverter.objectToByteArray(result!!))
      super.exculsiveChannel.basicAck(envelope.deliveryTag, false)
   }
}