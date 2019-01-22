package com.esentri.wrabbitmq.java;

import java.io.Serializable;

class TestObjectObject implements Serializable {
   private final TestObjectNumberText obj;

   public TestObjectObject(TestObjectNumberText obj) {
      this.obj = obj;
   }

   public TestObjectNumberText getObj() {
      return obj;
   }
}
