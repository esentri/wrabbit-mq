package com.esentri.wrabbitmq.domain

import com.esentri.wrabbitmq.WrabbitEvent
import com.esentri.wrabbitmq.WrabbitTopic

object TestDomain {

   object SimpleTopic : WrabbitTopic(name = "test.topic.simple") {
      val Event1_StringToNumber = WrabbitEvent<String, Int>(this, "test.topic.simple.Event1")
      val Event2_NumberToString = WrabbitEvent<Int, String>(this, "test.topic.simple.Event2")

      object NestedTopic : WrabbitTopic(name = "test.topic.simple.nestedTopic") {
         val Event1_IncrementNumber = WrabbitEvent<Int, Int>(this, "test.topic.simple.nestedTopic.Event1")
         val Event2_DecrementNumber = WrabbitEvent<Int, Int>(this, "test.topic.simple.nestedTopic.Event2")
      }
   }

}
