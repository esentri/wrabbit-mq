package com.esentri.wrabbitmq

object TestDomain {
   object ListenerTopic1: WrabbitTopic("TestTopic-1") {
      val StringEvent = WrabbitEvent<String>(this, "TT1-TE-1")
      val TestObjectObjectEvent = WrabbitEvent<TestObjectObject>(this, "TT1-TE-2")
   }

   object ReplierTopic1: WrabbitTopic("TestTopic-2") {
      val StringToInt = WrabbitEventWithReply<String, Int>(this, "TT2-TE1")
      val TestObjectObjectToString = WrabbitEventWithReply<TestObjectObject, String>(this, "TT2-TE2")
      val TestObjectObjectToTestObjectNumberText = WrabbitEventWithReply<TestObjectObject, TestObjectNumberText>(this, "TT2-TE3")
   }
}
