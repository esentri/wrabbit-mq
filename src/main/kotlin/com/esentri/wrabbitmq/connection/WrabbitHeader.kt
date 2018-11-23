package com.esentri.wrabbitmq.connection

enum class WrabbitHeader(val key: String) {
   REPLIER("replier"),
   LISTENER("listener"),
   TOPIC_LISTENER("listener-topic");

   fun asAMQPProperty() = hashMapOf<String, Any?>(Pair(this.key, null))

   companion object {
      fun standardHeaderForEvent(eventName: String): Map<String, Any?> {
         val returnMap: MutableMap<String, Any?> = HashMap()
         WrabbitHeader.values().forEach {
            returnMap[it.key] = null
         }
         returnMap[eventName] = null
         return returnMap
      }
   }

}
