package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.domain.TestDomain

fun main(args: Array<String>) {
   var wait = 0
   val waitNumber = 2
   val sentNumber = 7

   TestDomain.SimpleTopic.NestedTopic.Event1_IncrementNumber.replier { m:Int ->
      LOGGER.info("Event1: received string: $m")
      m + 1
   }

   TestDomain.SimpleTopic.NestedTopic.Event2_DecrementNumber.replier { m:Int ->
      LOGGER.info("Event2: received number: $m")
      m - 1
   }

   TestDomain.SimpleTopic.NestedTopic.Event1_IncrementNumber.sendAndReceive(sentNumber).thenAccept {
      LOGGER.info("Event1 received reply: $it")
      if(sentNumber+1 != it) {
         LOGGER.error("Unexpected result. $sentNumber -> $it")
         System.exit(1)
      }
      wait++
   }

   TestDomain.SimpleTopic.NestedTopic.Event2_DecrementNumber.sendAndReceive(sentNumber).thenAccept {
      LOGGER.info("Event2 received reply: $it")
      if(sentNumber-1 != it) {
         LOGGER.error("Unexpected result. $sentNumber -> $it")
         System.exit(1)
      }
      wait++
   }

   while(wait < waitNumber) {
      LOGGER.info("waiting for reply ($wait/$waitNumber)")
      Thread.sleep(1000)
   }

   LOGGER.info("success")
}