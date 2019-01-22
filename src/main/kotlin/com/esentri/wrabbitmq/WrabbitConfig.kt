package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.connection.WrabbitSharedConnectionFactory

// DEFAULTS
const val WrabbitDefaultHost = "localhost"
const val WrabbitDefaultPort = "5672"
const val WrabbitDefaultUsername = "guest"
const val WrabbitDefaultPassword = "guest"
const val WrabbitDefaultTimeout = "30000"
const val WrabbitDefaultHeartBeat = "30"


private fun getConfig(alternative1: String, alternative2: String, default: String): String =
   System.getProperty(alternative1)?: System.getProperty(alternative2) ?: default

// CONFIGS (system property or default)
fun WrabbitHost() = getConfig("wrabbit.host", "spring.rabbitmq.host", WrabbitDefaultHost)
fun WrabbitPort(): Int= getConfig("wrabbit.port", "spring.rabbitmq.port", WrabbitDefaultPort).toInt()
fun WrabbitUsername() = getConfig("wrabbit.username", "spring.rabbitmq.username", WrabbitDefaultUsername)
fun WrabbitPassword() = getConfig("wrabbit.password","spring.rabbitmq.password", WrabbitDefaultPassword)
fun WrabbitTimeout(): Int = getConfig("wrabbit.timeout", "spring.rabbitmq.connection-timeout", WrabbitDefaultTimeout).toInt()
fun WrabbitHeartBeat(): Int = getConfig("wrabbit.heartbeat", "spring.rabbitmq.requested-heartbeat", WrabbitDefaultHeartBeat).toInt()


// CONNECTION
fun SharedConnection() = WrabbitSharedConnectionFactory.newConnection()
val ConfigChannel = SharedConnection().createChannel()
fun NewChannel() = SharedConnection().createChannel()
