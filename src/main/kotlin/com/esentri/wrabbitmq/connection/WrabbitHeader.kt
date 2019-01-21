package com.esentri.wrabbitmq.connection

enum class WrabbitHeader(val key: String) {
   REPLIER("replier"),
   LISTENER("listener"),
   TOPIC_LISTENER("listener-topic");

   companion object {
      fun standardHeaderForEvent(topicName: String, eventName: String): Map<String, Any?> {
         val returnMap: MutableMap<String, Any?> = HashMap()
         WrabbitHeader.values().forEach {
            returnMap[it.key] = null
         }
         returnMap[eventName] = null
         returnMap[topicName] = null
         return returnMap
      }

      fun isWrabbitHeader(key: String): Boolean {
         return WrabbitHeader.values().map { it.key }.contains(key)
      }
   }

}
