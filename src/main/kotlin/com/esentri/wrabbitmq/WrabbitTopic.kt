package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.exceptions.WrabbitMQNoReplyException
import com.rabbitmq.client.MessageProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @WrabbitTopic is one of the two main classes you need. The other one is @WrabbitChannel.
 */
open class WrabbitTopic @JvmOverloads constructor(private val name: String) {

   private val wrabbitExchange: WrabbitExchange = WrabbitAdmin.declareExchange(WrabbitExchangeType.HEADER, name)

   init {
      logger.debug("Created new Wrabbit-Topic['$name']")
//      template.declareExchange = name
//      template.messageConverter = SimpleMessageConverter()
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
      val wrabbitMessenger = WrabbitAdmin.bindQueue(
         exchange = wrabbitExchange,
         queue = queue,
         binding = WrabbitBinding.Header(allHeaderExists = headers).build()
      )
      logger.debug("Bind Queue['$queueName'] on Wrabbit-Topic['$name'::$headers].")
      if (replier != null) wrabbitMessenger.addReplier(replier)
      if (listener != null) wrabbitMessenger.addListener(listener)
   }

   private fun <M, R> addReplier(queue: WrabbitQueue, replier: IMessageReplier<M, R>) {
      val container = SimpleMessageListenerContainer()
      container.connectionFactory = WrabbitConnectionFactory
      container.setQueueNames(queue.name)
      container.messageListener = WrabbitMessageListenerAdapter(template, replier)
      container.start()
   }

   private fun <M> addListener(queue: Queue, listener: IMessageListener<M>) {
      val container = SimpleMessageListenerContainer()
      container.connectionFactory = WrabbitConnectionFactory
      container.setQueueNames(queue.name)
      container.messageListener = WrabbitMessageListenerAdapter<M, Unit>(template, replier = null, listener = listener)
      container.start()
   }

   internal fun <T> send(message: T, properties: List<Pair<String, Any?>>) {
      logger.debug("Publish Message['$message'] on Wrabbit-Topic['$name::$properties'].")
      val rabbitMessage: Message = template.messageConverter.toMessage(message, toMessageProperties(properties))
      template.send(rabbitMessage)
   }

   internal fun <MESSAGE_TYPE, RETURN_TYPE> sendAndReceive(message: MESSAGE_TYPE, properties: List<Pair<String, Any?>>): CompletableFuture<RETURN_TYPE> {
      return CompletableFuture.supplyAsync {
         logger.debug("Publish Request-Message['$message'] on Wrabbit-Topic['$name'::${WrabbitHeader.extractStandardHeader(properties)}].")
         val msg: Message = template.messageConverter.toMessage(message, toMessageProperties(properties))
         val correlation = CorrelationData(UUID.randomUUID().toString())


         val reply: Message = template.sendAndReceive(msg, correlation) ?: throw WrabbitMQNoReplyException(name)
         val result: Any = template.messageConverter.fromMessage(reply)
         logger.debug("Received Reply-Message[${result.javaClass.simpleName} = '$result']" + " from Request-Message['$msg'].")
         @Suppress("UNCHECKED_CAST")
         return@supplyAsync result as RETURN_TYPE
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

   private fun toMessageProperties(entries: List<Pair<String, Any?>>): MessageProperties {
      val mp = MessageProperties()
      entries.forEach { mp.headers[it.first] = it.second }
      return mp
   }
}