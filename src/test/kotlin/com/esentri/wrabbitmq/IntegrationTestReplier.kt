package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.domain.TestDomain


fun main(args: Array<String>) {
   var wait = 0
   val waitNumber = 2

   TestDomain.SimpleTopic.Event1_StringToNumber.replier {
      LOGGER.info("Event1: received string: $it")
      it.toInt()
   }

   TestDomain.SimpleTopic.Event2_NumberToString.replier {
      LOGGER.info("Event2: received number: $it")
      it.toString()
   }

   TestDomain.SimpleTopic.Event1_StringToNumber.sendAndReceive("1234").thenAccept {
      LOGGER.info("Event1 received reply: $it")
      wait++
   }

   TestDomain.SimpleTopic.Event2_NumberToString.sendAndReceive(123).thenAccept {
      LOGGER.info("Event2 received reply: $it")
      wait++
   }

   while(wait < waitNumber) {
      LOGGER.info("waiting for reply ($wait/$waitNumber)")
      Thread.sleep(1000)
   }

   LOGGER.info("success")
}