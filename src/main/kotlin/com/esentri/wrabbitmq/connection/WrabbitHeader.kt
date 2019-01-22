package com.esentri.wrabbitmq.connection

enum class WrabbitHeader(val key: String) {
   REPLIER("replier"),
   LISTENER("listener"),
   TOPIC_LISTENER("listener-topic");

   companion object {
      fun standardHeaderForEvent(topicName: String, eventName: String): Map<String, Any?> {
         val headerMap: MutableMap<String, Any?> = HashMap()
         WrabbitHeader.values().forEach {
            headerMap[it.key] = null
         }
         headerMap[eventName] = null
         headerMap[topicName] = null
         return headerMap
      }

      fun isWrabbitHeader(key: String): Boolean {
         return WrabbitHeader.values().map { it.key }.contains(key)
      }
   }

}
