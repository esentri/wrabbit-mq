package com.esentri.wrabbitmq.exceptions

class WrabbitDeserializationException(originalError: Throwable) :
   WrabbitException(generateDeserializationErrorMessage(originalError)) {
   companion object {
      fun generateDeserializationErrorMessage(originalError: Throwable) =
         """
            All exceptions and classes must be deserializable.
            Original error message was: ${originalError.message} by ${originalError::class.java}.
         """.trimIndent()
   }
}
