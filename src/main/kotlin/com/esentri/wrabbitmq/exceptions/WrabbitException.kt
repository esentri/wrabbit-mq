package com.esentri.wrabbitmq.exceptions

import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.esentri.wrabbitmq.internal.extensions.wrabbitHeader
import com.rabbitmq.client.AMQP

open class WrabbitException(message: String, throwable: Throwable): RuntimeException(message, throwable) {
   companion object {
      fun generateMessage(sendingProperties: AMQP.BasicProperties) = "Exception while receiving reply to " +
         "${sendingProperties.wrabbitHeader(WrabbitHeader.TOPIC)}::${sendingProperties.wrabbitHeader(WrabbitHeader.EVENT)}." +
         " Check nested exceptions for details."
   }
}
