package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.exceptions.WrabbitMQNoReplyException
import org.springframework.amqp.core.HeadersExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.rabbit.support.CorrelationData
import org.springframework.amqp.support.converter.SimpleMessageConverter
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * @WrabbitTopic is one of the two main classes you need. The other one is @WrabbitEvent.
 */
open class WrabbitTopic {

   private val name: String
   private val template: RabbitTemplate
   private val exchange: HeadersExchange
   private val auditorHeaders = listOf("auditor")

   @JvmOverloads
   constructor(
      name: String,
      template: RabbitTemplate = RabbitTemplate(WrabbitConnectionFactory),
      exchange: HeadersExchange = HeadersExchange(name)
   ) {
      this.name = name
      this.template = template
      this.exchange = exchange
      WrabbitAdmin.declareExchange(exchange)
      logger.debug("Created new Wrabbit-Topic['${name}']")
      template.exchange = name
      template.messageConverter = SimpleMessageConverter()
      logger.debug("Create new Publisher on Wrabbit-Topic['$name'].")
   }

   companion object {
      val logger: Logger = LoggerFactory.getLogger(WrabbitTopic::class.java)
   }

   internal fun <MESSAGE_TYPE, RETURN_TYPE> createAndBindQueue(queueName: String = UUID.randomUUID().toString(), headers: List<String>,
                                                               replier: ((MESSAGE_TYPE) -> RETURN_TYPE)? = null,
                                                               listener: ((MESSAGE_TYPE) -> Unit)? = null):
      Queue {
      val queue = Queue(queueName)
      WrabbitAdmin.declareQueue(queue)
      logger.debug("Bind Queue['$queueName'] on Wrabbit-Topic['$name'::$headers].")
      WrabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).whereAll(*headers.toTypedArray()).exist())
      if (replier != null) addReplier(queue, replier)
      if (listener != null) addListener(queue, listener)
      return queue
   }

   private fun <MESSAGE_TYPE, RETURN_TYPE> addReplier(queue: Queue, handleMessage: (MESSAGE_TYPE) -> RETURN_TYPE) {
      val container = SimpleMessageListenerContainer()
      container.connectionFactory = WrabbitConnectionFactory
      container.setQueueNames(queue.name)
      container.messageListener =
         MessageListenerAdapter(java.util.function.Function<MESSAGE_TYPE, RETURN_TYPE> { handleMessage(it) }, "apply")
      container.start()
   }

   private fun <MESSAGE_TYPE> addListener(queue: Queue, handleMessage: (MESSAGE_TYPE) -> Unit) {
      val container = SimpleMessageListenerContainer()
      container.connectionFactory = WrabbitConnectionFactory
      container.setQueueNames(queue.name)
      container.messageListener =
         MessageListenerAdapter(java.util.function.Consumer<MESSAGE_TYPE> { handleMessage(it) }, "accept")
      container.start()
   }

   internal fun <T> send(message: T, headers: List<String>) {
      logger.debug("Publish Message['$message'] on Wrabbit-Topic['$name::$headers'].")
      val rabbitMessage: Message = template.messageConverter.toMessage(message, messageProperties(headers))
      template.send(rabbitMessage)
   }

   internal fun <MESSAGE_TYPE, RETURN_TYPE> sendAndReceive(message: MESSAGE_TYPE, headers: List<String>):
      CompletableFuture<RETURN_TYPE> {
      return CompletableFuture.supplyAsync {
         logger.debug("Publish Request-Message['$message'] on Wrabbit-Topic['$name'::$headers].")
         val rabbitMessage: Message = template.messageConverter.toMessage(message, messageProperties(headers))
         var rabbitReply: Message = template.sendAndReceive(rabbitMessage, CorrelationData(UUID.randomUUID().toString()))
            ?: throw WrabbitMQNoReplyException(name)
         val replyData: Any = template.messageConverter.fromMessage(rabbitReply)
         logger.debug("Received Reply-Message[${replyData.javaClass.simpleName} = '$replyData'] from Request-Message['$message'].")
         @Suppress("UNCHECKED_CAST")
         return@supplyAsync replyData as RETURN_TYPE
      }
   }

   private fun messageProperties(headers: List<String>): MessageProperties {
      val messageProperties = MessageProperties()
      headers.forEach { messageProperties.setHeader(it, null) }
      return messageProperties
   }

   fun <MESSAGE_TYPE> auditor(handleMessage: (MESSAGE_TYPE) -> Unit) {
      createAndBindQueue<MESSAGE_TYPE, Unit>(
         queueName = "$name.audit.${UUID.randomUUID()}",
         listener = handleMessage,
         headers = auditorHeaders)
   }

   // helper method for our java friends
   fun <MESSAGE_TYPE> addAuditor(handleMessage: Consumer<MESSAGE_TYPE>) {
      auditor<MESSAGE_TYPE> { handleMessage.accept(it) }
   }

}