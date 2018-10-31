package com.esentri.wrabbitmq

import com.rabbitmq.client.Channel
import org.springframework.amqp.AmqpRejectAndDontRequeueException
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import java.lang.IllegalStateException

internal class WrabbitMessageListenerAdapter<M, R>(
        private val template: RabbitTemplate,
        private val replier:  IMessageReplier<M, R>? = null,
        private val listener: IMessageListener<M>?   = null)
      : MessageListenerAdapter() {

   override fun onMessage(msg: Message, channel: Channel) {
      try {
         val context: MutableMap<String, Any?> = HashMap()
         if (msg.messageProperties != null) {
             msg.messageProperties.headers.forEach { k, v -> context[k] = v?.toString() }
         }
         @Suppress("UNCHECKED_CAST")
         val data: M = template.messageConverter.fromMessage(msg) as M
         if (replier != null) {
            if (replier is MessageWithContextReplier)
               handleResult(replier.reply(data, context), msg, channel)
            else if (replier is MessageReplier)
               handleResult(replier.reply(data), msg, channel)
            return
         }
         if (listener != null) {
            if (listener is MessageWithContextListener)
               listener.listen(data, context)
            else if (listener is MessageListener)
               listener.listen(data)
         }
      }
      catch (th: Throwable) {
         th.printStackTrace()
         // Throwing a exception within an 'replier' leads
         // in case of the rabbit default behaviour to a
         // infinite loop on the receiver side, because the
         // unprocessed message gets reenqeued. Throwing
         // 'AmqpRejectAndDontRequeueException' indicates
         // the basic.reject will be sent with requeue=false.
         throw AmqpRejectAndDontRequeueException(th)
      }
   }
}