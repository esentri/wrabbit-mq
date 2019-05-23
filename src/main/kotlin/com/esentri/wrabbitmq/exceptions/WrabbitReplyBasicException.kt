package com.esentri.wrabbitmq.exceptions

import com.rabbitmq.client.AMQP

@Suppress("unused")
open class WrabbitReplyBasicException(sendingProperties: AMQP.BasicProperties, throwable: Throwable) :
   WrabbitException(generateMessage(sendingProperties), throwable)
