package com.esentri.wrabbitmq.domain

import com.esentri.wrabbitmq.WrabbitChannel
import com.esentri.wrabbitmq.WrabbitTopic

object TestDomain {

   object SimpleTopic : WrabbitTopic(name = "test.topic.simple") {
      val Event1_StringToNumber = WrabbitChannel<String, Int>(this, "test.topic.simple.Event1")
      val Event2_NumberToString = WrabbitChannel<Int, String>(this, "test.topic.simple.Event2")

      object NestedTopic : WrabbitTopic(name = "test.topic.simple.nestedTopic") {
         val Event1_IncrementNumber = WrabbitChannel<Int, Int>(this, "test.topic.simple.nestedTopic.Event1")
         val Event2_DecrementNumber = WrabbitChannel<Int, Int>(this, "test.topic.simple.nestedTopic.Event2")
      }
   }

}
