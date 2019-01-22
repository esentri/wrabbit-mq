package com.esentri.wrabbitmq.java;

import java.io.Serializable;

class TestObjectNumberText implements Serializable {

   private final int number;
   private final String text;

   TestObjectNumberText(int number, String text) {
      this.number = number;
      this.text = text;
   }

   public int getNumber() {
      return number;
   }

   public String getText() {
      return text;
   }
}
