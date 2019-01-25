package com.esentri.wrabbitmq.exceptions

import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.esentri.wrabbitmq.internal.extensions.wrabbitHeader
import com.rabbitmq.client.AMQP

@Suppress("unused")
class WrabbitBasicReplyException(sendingProperties: AMQP.BasicProperties, throwable: Throwable):
   RuntimeException(generateMessage(sendingProperties), throwable) {

   val topicName = sendingProperties.wrabbitHeader(WrabbitHeader.TOPIC)
   val eventName = sendingProperties.wrabbitHeader(WrabbitHeader.EVENT)

   companion object {
       fun generateMessage(sendingProperties: AMQP.BasicProperties) = "Exception while receiving reply to " +
          "${sendingProperties.wrabbitHeader(WrabbitHeader.TOPIC)}::${sendingProperties.wrabbitHeader(WrabbitHeader.EVENT)}." +
          " Check nested exceptions for details."
   }
}