package com.esentri.wrabbitmq.internal.converter

import com.esentri.wrabbitmq.exceptions.WrabbitDeserializationException
import com.esentri.wrabbitmq.exceptions.WrabbitSerializationException
import java.io.*
import java.lang.Exception

object WrabbitObjectConverter {

   fun objectToByteArray(obj: Any): ByteArray {
      try {
         val byteArrayOutputStream = ByteArrayOutputStream()
         val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
         objectOutputStream.writeObject(obj)
         return byteArrayOutputStream.toByteArray()
      } catch (e: Exception) {
         throw WrabbitSerializationException(notSerializableObject = obj, originalError = e)
      }
   }

   fun <T> byteArrayToObject(byteArray: ByteArray): T {
      try {
         val bis = ByteArrayInputStream(byteArray)
         val inputStream = ObjectInputStream(bis)
         @Suppress("UNCHECKED_CAST")
         return inputStream.readObject() as T
      } catch (e: Exception) {
         throw WrabbitDeserializationException(e)
      }
   }
}
