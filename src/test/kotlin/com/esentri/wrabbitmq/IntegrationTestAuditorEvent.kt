package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.domain.TestDomain


fun main(args: Array<String>) {
   var wait = 0
   val waitNumber = 2

   TestDomain.SimpleTopic.Event1_StringToNumber.listener{ m: String ->
      LOGGER.info("Event1: received string: $m")
      wait++
   }

   TestDomain.SimpleTopic.Event2_NumberToString.listener { m :Int ->
      LOGGER.info("Event2: received number: $m")
      wait++
   }

   TestDomain.SimpleTopic.Event1_StringToNumber.send("1234")

   TestDomain.SimpleTopic.Event2_NumberToString.send(123)

   while(wait < waitNumber) {
      LOGGER.info("waiting for reply ($wait/$waitNumber)")
      Thread.sleep(1000)
   }

   LOGGER.info("success")
}