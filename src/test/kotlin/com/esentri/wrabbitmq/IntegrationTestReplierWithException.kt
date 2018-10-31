package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.domain.TestDomain
import java.lang.IllegalStateException


fun main(args: Array<String>) {
   var wait = 0

   // Throwing a exception within an 'replier' leads
   // in case of the rabbit default behaviour to a
   // infinite loop on the receiver side, because the
   // unprocessed message get to reenqeued.

   TestDomain.SimpleTopic.Event1_StringToNumber.replier { m: String ->
      throw IllegalStateException("- TEST EXCEPTION -")
   }

   TestDomain.SimpleTopic.Event1_StringToNumber.sendAndReceive("1234").thenAccept {
      LOGGER.info("Event1 received reply: $it")
      wait++
   }

   Thread.sleep(1000)

   LOGGER.info("success")
}