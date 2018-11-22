package com.esentri.wrabbitmq

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.ObjectInputStream

object WrabbitConverter {

   fun objectToByteArray(obj: Any): ByteArray {
      val byteArrayOutputStream = ByteArrayOutputStream()
      val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
      objectOutputStream.writeObject(obj)
      return byteArrayOutputStream.toByteArray()
   }

   fun <T> byteArrayToObject(byteArray: ByteArray): T {
      val bis = ByteArrayInputStream(byteArray)
      val inputStream = ObjectInputStream(bis)
      return inputStream.readObject() as T
   }
}