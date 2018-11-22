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
   val contentTypePublish: WrabbitContentType = WrabbitContentType.PLAIN_TEXT,
   val contentTypeReceive: WrabbitContentType = WrabbitContentType.PLAIN_TEXT,
   val deliveryMode: WrabbitDeliveryMode = WrabbitDeliveryMode.PERSISTENT,
   val priority: Int? = null
) {

   fun toAMQPBasicProperties(): AMQP.BasicProperties =
      AMQP.BasicProperties.Builder()
         .contentType(contentTypePublish.rabbitKey)
         .deliveryMode(deliveryMode.rabbitKey)
         .priority(priority)
         .build()


   class Header(val allHeaderExists: List<String>) {
      fun build(): WrabbitBinding {
         val arguments: MutableMap<String, Any> = hashMapOf(Pair("x-match", "any"))
         allHeaderExists.forEach {
            arguments.put(it, "")
         }
         return WrabbitBinding(bindingArguments = arguments)
      }
   }
}