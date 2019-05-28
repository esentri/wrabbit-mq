package com.esentri.wrabbitmq.internal.consumer

import com.esentri.wrabbitmq.WrabbitReplierWithContext
import com.esentri.wrabbitmq.connection.WrabbitHeader
import com.esentri.wrabbitmq.exceptions.WrabbitSerializationException
import com.esentri.wrabbitmq.internal.ReplyLogger
import com.esentri.wrabbitmq.internal.converter.WrabbitObjectConverter
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Envelope
import java.io.NotSerializableException

// TODO replace with new ThreadChannel method
class WrabbitConsumerReplier<MESSAGE_TYPE, RETURN_TYPE>(channel: Channel,
                                                        val wrabbitReplier: WrabbitReplierWithContext<MESSAGE_TYPE, RETURN_TYPE>)
   : WrabbitConsumerBase(channel) {

   override fun handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: ByteArray) {
      val message: MESSAGE_TYPE = WrabbitObjectConverter.byteArrayToObject(body)
      try {
         val result = wrabbitReplier(properties.headers, message)
         sendAnswer(properties, responseByteArray(result))
      } catch (e: Exception) {
         ReplyLogger.error("Error while processing received data on {}::{}",
            properties.headers[WrabbitHeader.TOPIC.key],
            properties.headers[WrabbitHeader.EVENT.key],
            e)

         var serializedException: ByteArray
         try {
            serializedException = responseByteArray(e)
            sendAnswer(properties, serializedException)
         } catch (serializationException: Exception) {
            sendAnswer(properties, responseByteArray(serializationException))
         }
      } finally {
         super.getChannel().basicAck(envelope.deliveryTag, false)
      }
   }

   private fun responseByteArray(result: RETURN_TYPE): ByteArray =
      WrabbitObjectConverter.objectToByteArray(WrabbitReplyMessage(value = result))

   private fun responseByteArray(exception: Exception): ByteArray =
      WrabbitObjectConverter.objectToByteArray(WrabbitReplyMessage<RETURN_TYPE>(exception = exception))


   private fun sendAnswer(receivedProperties: AMQP.BasicProperties, body: ByteArray) {
      super.getChannel().basicPublish("",
         receivedProperties.replyTo,
         replyProperties(receivedProperties),
         body)
   }

   private fun replyProperties(properties: AMQP.BasicProperties): AMQP.BasicProperties = AMQP.BasicProperties
      .Builder()
      .correlationId(properties.correlationId)
      .headers(properties.headers.filter { it.key == WrabbitHeader.EVENT.key || it.key == WrabbitHeader.TOPIC.key })
      .build()
}