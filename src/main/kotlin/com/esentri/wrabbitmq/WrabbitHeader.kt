package com.esentri.wrabbitmq

object WrabbitHeader {

   private const val HEADER_LENGTH = 4

   const val REPLIER = "replier"
   const val LISTENER = "listener"
   const val TOPIC_LISTENER = "auditor"

   private val standardHeaderList = listOf(
      Pair(REPLIER, null),
      Pair(LISTENER, null),
      Pair(TOPIC_LISTENER, null)
   )

   fun buildStandardHeader(queueName: String) =
      standardHeaderList.toMutableList().add(Pair(queueName, null))

   fun extractStandardHeader(header: List<Pair<String, Any?>>) = header.subList(0, HEADER_LENGTH)

   fun isWrabbitHeader(key: String): Boolean {
      return standardHeaderList.filter { it.first == key }.isNotEmpty()
   }
}