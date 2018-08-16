package com.esentri.wrabbitmq

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory

object WrabbitConnectionFactory : CachingConnectionFactory() {

   init {
      this.host = WrabbitHost()
      this.port = WrabbitPort()
      this.username = WrabbitUsername()
      this.setPassword(WrabbitPassword())
      this.setRequestedHeartBeat(WrabbitHeartBeat())
      this.setConnectionTimeout(WrabbitTimeout())
   }

}
