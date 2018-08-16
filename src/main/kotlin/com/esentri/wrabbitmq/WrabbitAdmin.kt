package com.esentri.wrabbitmq

import org.springframework.amqp.rabbit.core.RabbitAdmin

object WrabbitAdmin : RabbitAdmin(WrabbitConnectionFactory)
