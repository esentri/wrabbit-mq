package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.WrabbitHeader.LISTENER
import com.esentri.wrabbitmq.WrabbitHeader.REPLIER
import com.esentri.wrabbitmq.WrabbitHeader.buildStandardHeader
import java.io.Serializable
import java.util.*
import java.util.concurrent.CompletableFuture

open class WrabbitChannel<MESSAGE_TYPE : Serializable, RETURN_TYPE: Serializable>(
   private val topic: WrabbitTopic,
   private val queueName: String) {
   private val properties: MutableList<Pair<String, Any?>> = ArrayList()
   private val header = buildStandardHeader(queueName)


   private fun replier(replier: IMessageReplier<MESSAGE_TYPE, RETURN_TYPE>) {
      topic.createAndBindQueue(
              queueName = "$queueName.$REPLIER",
              headers = listOf(queueName, REPLIER),
              replier = replier
      )
   }

   fun replier(replier: MsgReplier<MESSAGE_TYPE, RETURN_TYPE>) {
      val replierObj = object : MessageReplier<MESSAGE_TYPE, RETURN_TYPE> {
         override fun reply(message: MESSAGE_TYPE): RETURN_TYPE {
            return replier.invoke(message)
         }
      }
      replier(replierObj)
   }

   fun replier(replier: MsgCtxReplier<MESSAGE_TYPE, RETURN_TYPE>) {
      val replierObj = object : MessageWithContextReplier<MESSAGE_TYPE, RETURN_TYPE> {
         override fun reply(message: MESSAGE_TYPE, context: Map<String, Any?>): RETURN_TYPE {
            return replier.invoke(message, context)
         }
      }
      replier(replierObj)
   }

   fun listener(listener: IMessageListener<MESSAGE_TYPE>) {
      topic.createAndBindQueue<MESSAGE_TYPE, Unit>(
              queueName = "$queueName.$LISTENER.${UUID.randomUUID()}",
              headers = listOf(queueName, LISTENER),
              listener = listener
      )
   }

   fun listener(listener: MsgListener<MESSAGE_TYPE>) {
      val listenerObj = object : MessageListener<MESSAGE_TYPE> {
         override fun listen(message: MESSAGE_TYPE) {
            listener.invoke(message)
         }
      }
      listener(listenerObj)
   }

   fun listener(listener: MsgCtxListener<MESSAGE_TYPE>) {
      val listenerObj = object : MessageWithContextListener<MESSAGE_TYPE> {
         override fun listen(message: MESSAGE_TYPE, context: Map<String, Any?>) {
            listener.invoke(message, context)
         }
      }
      listener(listenerObj)
   }

   fun send(message: MESSAGE_TYPE) {
      topic.send(message, header.plus(properties))
      properties.clear()
   }

   fun send(message: MESSAGE_TYPE, properties: List<Pair<String, Any?>>) =
           topic.send(message, header.plus(properties))

   fun sendAndReceive(message: MESSAGE_TYPE): CompletableFuture<RETURN_TYPE> {
      val rs: CompletableFuture<RETURN_TYPE> = topic.sendAndReceive(message, header.plus(properties))
      properties.clear()
      return rs
   }

   fun sendAndReceive(message: MESSAGE_TYPE, properties: List<Pair<String, Any?>>): CompletableFuture<RETURN_TYPE> =
           topic.sendAndReceive(message, header.plus(properties))

   fun append(key: String, value: Any): WrabbitChannel<MESSAGE_TYPE, RETURN_TYPE> {
      properties.add(Pair(key, value))
      return this
   }

   fun append(key: Enum<*>, value: Any): WrabbitChannel<MESSAGE_TYPE, RETURN_TYPE> {
      properties.add(Pair(key.name, value))
      return this
   }
}