package com.esentri.wrabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.lang.RuntimeException

abstract class WrabbitConsumer(val internalChannel: Channel) : DefaultConsumer(internalChannel) {
   internal fun getContext(properties: AMQP.BasicProperties?): MutableMap<String, Any?> {
      val context: MutableMap<String, Any?> = HashMap()
      properties?.headers?.forEach { k, v ->
         if (WrabbitHeader.isWrabbitHeader(k)) return@forEach
         context[k] = v?.toString()
      }
      return context
   }
}

class WrabbitReplier<MESSAGE_TYPE, RETURN_TYPE>(
   replyChannel: Channel,
   val replier: IMessageReplier<MESSAGE_TYPE, RETURN_TYPE>) : WrabbitConsumer(replyChannel) {

   override fun handleDelivery(consumerTag: String?,
                               envelope: Envelope,
                               properties: AMQP.BasicProperties?,
                               body: ByteArray?) {
      val message: MESSAGE_TYPE = WrabbitConverter.byteArrayToObject(body!!)

      val result: RETURN_TYPE = when (replier) {
         is MessageReplier -> {
            replier.reply(message)
         }
         is MessageWithContextReplier -> {
            replier.reply(message, getContext(properties))
         }
         else -> {
            throw RuntimeException("Replier type not integrated: ${replier.javaClass.name}")
         }
      }
      super.internalChannel.basicPublish("",
         properties!!.replyTo,
         AMQP.BasicProperties
            .Builder()
            .correlationId(properties.correlationId)
            .build(),
         WrabbitConverter.objectToByteArray(result!!))

      super.internalChannel.basicAck(envelope.deliveryTag, false)
   }
}

class WrabbitListener<MESSAGE_TYPE>(
   internalChannel: Channel,
   val listener: IMessageListener<MESSAGE_TYPE>): WrabbitConsumer(internalChannel) {
   override fun handleDelivery(consumerTag: String?, envelope: Envelope, properties: AMQP.BasicProperties?, body: ByteArray?) {
      val message: MESSAGE_TYPE = WrabbitConverter.byteArrayToObject(body!!)
      when (listener) {
         is MessageListener -> {
            listener.listen(message)
         }
         is MessageWithContextListener -> {
            listener.listen(message, getContext(properties))
         }
         else -> {
            throw RuntimeException("Replier type not integrated: ${listener.javaClass.name}")
         }
      }
      super.internalChannel.basicAck(envelope.deliveryTag, false)
   }
}

class WrabbitReplyListener(
   internalChannel: Channel,
   val callback: ((ByteArray) -> Unit)): WrabbitConsumer(internalChannel) {
   override fun handleDelivery(consumerTag: String?, envelope: Envelope, properties: AMQP.BasicProperties?, body: ByteArray?) {
      callback(body!!)
      super.internalChannel.close()
   }
}
