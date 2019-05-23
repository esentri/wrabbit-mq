package com.esentri.wrabbitmq.exceptions

import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.esentri.wrabbitmq.internal.extensions.wrabbitHeader
import com.rabbitmq.client.AMQP

class WrabbitReplyTimeoutException(sendingProperties: AMQP.BasicProperties, throwable: Throwable)
   : WrabbitException(
   message = "Timeout occurred on topic ${sendingProperties.wrabbitHeader(WrabbitHeader.TOPIC)} on event " +
      "${sendingProperties.wrabbitHeader(WrabbitHeader.EVENT)}. Timeout is currently set to ${com.esentri.wrabbitmq.WrabbitTimeout()}",
   throwable = throwable)