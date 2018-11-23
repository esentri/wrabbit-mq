package com.esentri.wrabbitmq.internal.consumer

import com.esentri.wrabbitmq.WrabbitListener
import com.esentri.wrabbitmq.internal.converter.WrabbitObjectConverter
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Envelope

class WrabbitConsumerReplyListener<MESSAGE_TYPE>(exculsiveChannel: Channel,
                                                 val wrabbitConsumerListener: WrabbitListener<MESSAGE_TYPE>,
                                                 exclusiveQueueName: String) : WrabbitConsumerBase(exculsiveChannel, exclusiveQueueName) {
   override fun handleDelivery(consumerTag: String?, envelope: Envelope, properties: AMQP.BasicProperties?, body: ByteArray?) {
      val message: MESSAGE_TYPE = WrabbitObjectConverter.byteArrayToObject(body!!)
      wrabbitConsumerListener(message)
      super.exculsiveChannel.close()
   }
}