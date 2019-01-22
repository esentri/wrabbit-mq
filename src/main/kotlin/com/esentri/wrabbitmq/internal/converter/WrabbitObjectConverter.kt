package com.esentri.wrabbitmq.internal.converter

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object WrabbitObjectConverter {

   fun objectToByteArray(obj: Any): ByteArray {
      val byteArrayOutputStream = ByteArrayOutputStream()
      val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
      objectOutputStream.writeObject(obj)
      return byteArrayOutputStream.toByteArray()
   }

   fun <T> byteArrayToObject(byteArray: ByteArray): T {
      val bis = ByteArrayInputStream(byteArray)
      val inputStream = ObjectInputStream(bis)
      @Suppress("UNCHECKED_CAST")
      return inputStream.readObject() as T
   }
}
