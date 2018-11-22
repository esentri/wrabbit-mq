package com.esentri.wrabbitmq

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

object WrabbitConnectionFactory : ConnectionFactory() {

   private var connection: Connection? = null

   init {
      this.host = WrabbitHost()
      this.port = WrabbitPort()
      this.username = WrabbitUsername()
      this.password = WrabbitPassword()
      this.requestedHeartbeat = WrabbitHeartBeat()
      this.connectionTimeout = WrabbitTimeout()
      this.isAutomaticRecoveryEnabled = true
   }

//   simulate CachingConnectionFactory behaviour (https://github.com/spring-projects/spring-amqp/blob/master/spring-rabbit/src/main/java/org/springframework/amqp/rabbit/connection/CachingConnectionFactory.java)
//   override fun newConnection(): Connection {
//      if (connection != null) return connection as Connection
//      this.connection = super.newConnection()
//      return connection as Connection
//   }

}
