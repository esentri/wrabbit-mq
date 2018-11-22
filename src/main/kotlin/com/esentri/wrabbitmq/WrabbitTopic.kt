package com.esentri.wrabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.impl.AMQBasicProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap

/**
 * @WrabbitTopic is one of the two main classes you need. The other one is @WrabbitChannel.
 */
open class WrabbitTopic(private val name: String) {

   private val wrabbitExchange: WrabbitExchange = WrabbitAdmin.declareExchange(WrabbitExchangeType.HEADER, name)
   private lateinit var wrabbitMessenger: WrabbitMessenger

   init {
      logger.debug("Created new Wrabbit-Topic['$name']")
      logger.debug("Create new Publisher on Wrabbit-Topic['$name'].")
   }

   companion object {
      val logger: Logger = LoggerFactory.getLogger(WrabbitTopic::class.java)
   }

   internal fun <M, R> createAndBindQueue(queueName: String = UUID.randomUUID().toString(),
                                          headers: List<String>,
                                          replier: IMessageReplier<M, R>? = null,
                                          listener: IMessageListener<M>? = null) {
      val queue = WrabbitAdmin.declareQueue(queueName)
      wrabbitMessenger = WrabbitAdmin.bindQueue(
         exchange = wrabbitExchange,
         queue = queue,
         binding = WrabbitBinding.Header(allHeaderExists = headers).build()
      )
      logger.debug("Bind Queue['$queueName'] on Wrabbit-Topic['$name'::$headers].")
      if (replier != null) wrabbitMessenger.addReplier(replier)
      if (listener != null) wrabbitMessenger.addListener(listener)
   }

   internal fun <T> send(message: T, properties: List<Pair<String, Any?>>) {
      logger.debug("Publish Message['$message'] on Wrabbit-Topic['$name::$properties'].")
//      WrabbitAdmin.connection.createChannel().basicPublish(
//         name,
//         "",
//         createStandardProperties(propertiesToHeaders(properties)),
//         WrabbitConverter.objectToByteArray(message!!))
      wrabbitMessenger.send(WrabbitConverter.objectToByteArray(message!!), propertiesToHeaders(properties))
   }

   private fun propertiesToHeaders(properties: List<Pair<String, Any?>>): MutableMap<String, Any?> {
      val additionalHeaders: MutableMap<String, Any?> = HashMap()
      properties.forEach { additionalHeaders.put(it.first, it.second) }
      return additionalHeaders
   }

   internal fun <MESSAGE_TYPE, RETURN_TYPE> sendAndReceive(message: MESSAGE_TYPE, properties: List<Pair<String, Any?>>): CompletableFuture<RETURN_TYPE> {
      logger.debug("Publish Request-Message['$message'] on Wrabbit-Topic['$name'::${WrabbitHeader.extractStandardHeader(properties)}].")
      return wrabbitMessenger.sendAndReceive(WrabbitConverter.objectToByteArray(message!!)).thenApply {
         logger.debug("Received Reply-Message from Request-Message['$message'].")
         WrabbitConverter.byteArrayToObject(it) as RETURN_TYPE
      }
   }

   fun <MESSAGE_TYPE> listener(listener: IMessageListener<MESSAGE_TYPE>) {
      createAndBindQueue<MESSAGE_TYPE, Unit>(
         queueName = "$name.${WrabbitHeader.TOPIC_LISTENER}.${UUID.randomUUID()}",
         headers = listOf(WrabbitHeader.TOPIC_LISTENER),
         listener = listener)
   }

   fun <MESSAGE_TYPE> listener(listen: MsgListener<MESSAGE_TYPE>) {
      val listenObj = object : MessageListener<MESSAGE_TYPE> {
         override fun listen(message: MESSAGE_TYPE) {
            listen.invoke(message)
         }
      }
      listener(listenObj)
   }

   fun <MESSAGE_TYPE> listener(listen: MsgCtxListener<MESSAGE_TYPE>) {
      val listenObj = object : MessageWithContextListener<MESSAGE_TYPE> {
         override fun listen(message: MESSAGE_TYPE, context: Map<String, Any?>) {
            listen.invoke(message, context)
         }
      }
      listener(listenObj)
   }
}