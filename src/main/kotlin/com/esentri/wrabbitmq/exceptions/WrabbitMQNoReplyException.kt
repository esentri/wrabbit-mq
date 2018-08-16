package com.esentri.wrabbitmq.exceptions

class WrabbitMQNoReplyException(exchangeName: String) : Exception("No one replied to $exchangeName")