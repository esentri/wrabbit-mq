package com.esentri.wrabbitmq

import java.io.Serializable
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

class WrabbitEvent<MESSAGE_TYPE : Serializable, RETURN_TYPE: Serializable>  (
   private val topic: WrabbitTopic, private val uniqueName: String) {

   val standardSendHeaders: List<String> = listOf(uniqueName, "replier", "listener", "auditor")

   fun replier(handleMessage: (MESSAGE_TYPE) -> RETURN_TYPE) {
      topic.createAndBindQueue(
         queueName = "$uniqueName.replier",
         replier = handleMessage,
         headers = replierHeader())
   }

   fun listener(handleMessage: (MESSAGE_TYPE) -> Unit) {
      topic.createAndBindQueue<MESSAGE_TYPE, Unit>(
         queueName = "$uniqueName.listener.${UUID.randomUUID()}",
         listener = handleMessage,
         headers = listenerHeader())
   }

   fun auditor(handleMessage: (MESSAGE_TYPE) -> Unit) {
      topic.createAndBindQueue<MESSAGE_TYPE, Unit>(
         queueName = "$uniqueName.auditor.${UUID.randomUUID()}",
         listener = handleMessage,
         headers = auditorHeader())
   }

   fun send(message: MESSAGE_TYPE) = topic.send(message, standardSendHeaders)

   fun sendAndReceive(message: MESSAGE_TYPE): CompletableFuture<RETURN_TYPE> = topic.sendAndReceive(message, standardSendHeaders)

   private fun replierHeader(): List<String> = listOf(uniqueName, "replier")
   private fun listenerHeader(): List<String> = listOf(uniqueName, "listener")
   private fun auditorHeader(): List<String> = listOf(uniqueName, "auditor")

   // helper methods for our java friends
   fun addAuditor(handleMessage: Consumer<MESSAGE_TYPE>) {
      auditor { handleMessage.accept(it) }
   }

   fun addListener(handleMessage: Consumer<MESSAGE_TYPE>) {
      listener { handleMessage.accept(it) }
   }

   fun addReplier(handleMessage: Function<MESSAGE_TYPE, RETURN_TYPE>) {
      replier { handleMessage.apply(it) }
   }

}