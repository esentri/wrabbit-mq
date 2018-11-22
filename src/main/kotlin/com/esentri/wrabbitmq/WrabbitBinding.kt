package com.esentri.wrabbitmq

import com.rabbitmq.client.AMQP

enum class WrabbitContentType(val rabbitKey: String) {
   PLAIN_TEXT("text/plain"),
   OCTET_STREAM("application/octet-stream")
}

enum class WrabbitDeliveryMode(val rabbitKey: Int) {
   NON_PERSISTENT(1),
   PERSISTENT(2)
}

class WrabbitBinding(
   val routingKey: String = "",
   val bindingArguments: Map<String, Any> = emptyMap(),
   val contentTypePublish: WrabbitContentType = WrabbitContentType.OCTET_STREAM,
   val contentTypeReceive: WrabbitContentType = WrabbitContentType.OCTET_STREAM,
   val deliveryMode: WrabbitDeliveryMode = WrabbitDeliveryMode.PERSISTENT,
   val priority: Int? = null
) {

   fun toAMQPBasicProperties(): AMQP.BasicProperties =
      AMQP.BasicProperties.Builder()
         .contentType(contentTypePublish.rabbitKey)
         .deliveryMode(deliveryMode.rabbitKey)
         .priority(priority)
         .headers(bindingArguments)
         .build()


   class Header(val allHeaderExists: List<String>) {
      fun build(): WrabbitBinding {
         val arguments: MutableMap<String, Any> = hashMapOf(Pair("x-match", "all"))
         allHeaderExists.forEach {
            arguments[it] = ""
         }
         return WrabbitBinding(bindingArguments = arguments)
      }
   }
}