package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.domain.TestDomain

fun main(args: Array<String>) {
   var wait = 0
   val waitNumber = 2

   TestDomain.SimpleTopic.Event1_StringToNumber.listener {
      LOGGER.info("received string: $it")
      wait++
   }

   TestDomain.SimpleTopic.Event2_NumberToString.listener {
      LOGGER.info("received number: $it")
      wait++
   }

   TestDomain.SimpleTopic.Event1_StringToNumber.send("hello world")
   TestDomain.SimpleTopic.Event2_NumberToString.send(123)

   while(wait < waitNumber) {
      LOGGER.info("waiting for reply ($wait/$waitNumber)")
      Thread.sleep(1000)
   }

   LOGGER.info("success")
}