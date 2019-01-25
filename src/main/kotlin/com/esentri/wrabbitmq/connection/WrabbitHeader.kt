package com.esentri.wrabbitmq.connection

open class WrabbitHeader<VALUE>(val key: String) {
   object TOPIC: WrabbitHeader<String>("topic")
   object EVENT: WrabbitHeader<String>("event")

   companion object {
      fun standardHeaderForEvent(topicName: String, eventName: String): Map<String, Any?> {
         val headerMap: MutableMap<String, Any?> = HashMap()
         headerMap[TOPIC.key] = topicName
         headerMap[EVENT.key] = eventName
         return headerMap
      }
   }
}

