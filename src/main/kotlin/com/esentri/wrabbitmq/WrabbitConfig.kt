package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.connection.WrabbitSharedConnectionFactory
import com.rabbitmq.client.Channel
import java.lang.ref.Cleaner

// DEFAULTS
const val WrabbitDefaultHost = "localhost"
const val WrabbitDefaultPort = "5672"
const val WrabbitDefaultUsername = "guest"
const val WrabbitDefaultPassword = "guest"
const val WrabbitDefaultTimeout = "3000"
const val WrabbitDefaultHeartBeat = "30"
const val WrabbitDefaultReplyTimeoutMS = "5000"

// Channels are closed by the cleaner
// when the channel object gets GC'ed.
private val cleaner: Cleaner = Cleaner.create()

private fun getConfig(alternative1: String, alternative2: String, default: String): String =
   System.getProperty(alternative1) ?: System.getProperty(alternative2) ?: default

// CONFIGS (system property or default)
fun WrabbitHost() = getConfig("wrabbit.host", "spring.rabbitmq.host", WrabbitDefaultHost)

fun WrabbitPort(): Int = getConfig("wrabbit.port", "spring.rabbitmq.port", WrabbitDefaultPort).toInt()
fun WrabbitUsername() = getConfig("wrabbit.username", "spring.rabbitmq.username", WrabbitDefaultUsername)
fun WrabbitPassword() = getConfig("wrabbit.password", "spring.rabbitmq.password", WrabbitDefaultPassword)
fun WrabbitTimeout(): Int = getConfig("wrabbit.timeout", "spring.rabbitmq.connection-timeout", WrabbitDefaultTimeout).toInt()
fun WrabbitHeartBeat(): Int = getConfig("wrabbit.heartbeat", "spring.rabbitmq.requested-heartbeat", WrabbitDefaultHeartBeat).toInt()
fun WrabbitReplyTimeoutMS(): Long = getConfig("wrabbit.reply-timeout-ms", "spring.rabbitmq.reply-timeout-ms",
   WrabbitDefaultReplyTimeoutMS).toLong()


// CONNECTION
fun SharedConnection() = WrabbitSharedConnectionFactory.newConnection()

val ConfigChannel = SharedConnection().createChannel()

fun ThreadChannel(): Channel {
   val channel = threadLocalChannel.get()
   if(channel.isOpen) return channel
   threadLocalChannel.set(SharedConnection().createChannel())
   return threadLocalChannel.get()
}

private val threadLocalChannel: ThreadLocal<Channel> = ThreadLocal.withInitial {
   val channel: Channel = SharedConnection().createChannel()
   cleaner.register(channel) { channel.close() }
   return@withInitial channel
}
