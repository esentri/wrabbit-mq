package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.exceptions.WrabbitMQNoReplyException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.support.CorrelationData
import org.springframework.amqp.support.converter.SimpleMessageConverter
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @WrabbitTopic is one of the two main classes you need. The other one is @WrabbitChannel.
 */

// (1) Rename WrabbitChannel to WrabbitChannnel
// (1.1) with following channel-types
//       - send-channel
//       - send-and-reply-channel
//       - send-typed-channel
//       - send-and-reply-typed-channel

// (2) Provide WrabbitChannelBuilder,
// (3) Replace Auditor with Topic.Listener.
// (4) Enable user defined Topic.Listener.
//
open class WrabbitTopic @JvmOverloads constructor(private val name: String,
                                                  private val template: RabbitTemplate  = RabbitTemplate(WrabbitConnectionFactory),
                                                  private val exchange: HeadersExchange = HeadersExchange(name)) {
   init {
      WrabbitAdmin.declareExchange(exchange)
      logger.debug("Created new Wrabbit-Topic['$name']")
      template.exchange = name
      template.messageConverter = SimpleMessageConverter()
      logger.debug("Create new Publisher on Wrabbit-Topic['$name'].")
   }

   companion object {
      val logger: Logger = LoggerFactory.getLogger(WrabbitTopic::class.java)
   }

   internal fun <M, R> createAndBindQueue(queueName: String = UUID.randomUUID().toString(),
                                          headers: List<String>,
                                          replier:  IMessageReplier<M, R>? = null,
                                          listener: IMessageListener<M>? = null):Queue {
      val queue = Queue(queueName)
      WrabbitAdmin.declareQueue(queue)
      logger.debug("Bind Queue['$queueName'] on Wrabbit-Topic['$name'::$headers].")
      WrabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).whereAll(*headers.toTypedArray()).exist())
      if (replier  != null) addReplier(queue, replier)
      if (listener != null) addListener(queue, listener)
      return queue
   }

   private fun <M, R> addReplier(queue: Queue, replier: IMessageReplier<M, R>) {
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
         val correlation  = CorrelationData(UUID.randomUUID().toString())




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
              headers   = listOf(WrabbitHeader.TOPIC_LISTENER),
              listener  = listener)
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