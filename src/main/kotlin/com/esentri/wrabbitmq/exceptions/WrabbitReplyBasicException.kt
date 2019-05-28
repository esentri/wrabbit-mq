package com.esentri.wrabbitmq.exceptions

import com.rabbitmq.client.AMQP

class WrabbitReplyBasicException(sendingProperties: AMQP.BasicProperties, throwable: Throwable) :
   WrabbitException(generateBasicErrorMessage(sendingProperties), throwable)
