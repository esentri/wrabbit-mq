package com.esentri.wrabbitmq

object WrabbitHeader {

   private const val HEADER_LENGTH = 4;

   const val REPLIER    = "replier"
   const val LISTENER   = "listener"
   const val TOPIC_LISTENER    = "auditor"

   fun buildStandardHeader(queueName: String) =
      listOf(
         Pair(queueName,  null),
         Pair(REPLIER,    null),
         Pair(LISTENER,   null),
         Pair(TOPIC_LISTENER,    null)
      )

   fun extractStandardHeader(header: List<Pair<String, Any?>>)
      = header.subList(0, HEADER_LENGTH)
}