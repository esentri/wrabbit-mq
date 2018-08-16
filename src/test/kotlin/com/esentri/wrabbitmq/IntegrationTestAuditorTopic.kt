package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.domain.TestDomain


fun main(args: Array<String>) {
   var wait = 0
   val waitNumber = 4

   TestDomain.SimpleTopic.auditor<Any> {
      LOGGER.info("SimpleTopic: received: $it")
      wait++
   }

   TestDomain.SimpleTopic.NestedTopic.auditor<Any> {
      LOGGER.info("NestedTopic: received: $it")
      wait++
   }

   TestDomain.SimpleTopic.Event1_StringToNumber.send("Hello world SIMPLE!")
   TestDomain.SimpleTopic.Event2_NumberToString.send(123)

   TestDomain.SimpleTopic.NestedTopic.Event1_IncrementNumber.send(320)
   TestDomain.SimpleTopic.NestedTopic.Event2_DecrementNumber.send(321)

   while(wait < waitNumber) {
      LOGGER.info("waiting for reply ($wait/$waitNumber)")
      Thread.sleep(1000)
   }

   LOGGER.info("success")
}