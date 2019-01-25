package com.esentri.wrabbitmq.internal.consumer

import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer

abstract class WrabbitConsumerBase(channel: Channel): DefaultConsumer(channel)
