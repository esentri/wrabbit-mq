package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.WrabbitAdmin.channel


class WrabbitExchange(val type: WrabbitExchangeType,
                      val name: String,
                      val durable: Boolean = true) {


}