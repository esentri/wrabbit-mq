package com.esentri.wrabbitmq.internal.extensions

import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.rabbitmq.client.AMQP

@Suppress("UNCHECKED_CAST")
fun <T> AMQP.BasicProperties.wrabbitHeader(header: WrabbitHeader<T>): T = this.headers[header.key] as T