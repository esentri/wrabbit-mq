package com.esentri.wrabbitmq.exceptions

class WrabbitSerializationException(notSerializableObject: Any, originalError: Throwable) :
   WrabbitException(generateSerializationErrorMessage(notSerializableObject, originalError)) {
   companion object {
      fun generateSerializationErrorMessage(notSerializableObject: Any, originalError: Throwable) =
         """
            It happened while trying to serialize ${notSerializableObject::class.java.canonicalName}. You need to fix this by implementing the interface
            'Serializable' and by making sure all the fields are indeed serializable.
            All exceptions and classes must be serializable.

            Original error message was: ${originalError.message} by ${originalError::class.java}.
         """.trimIndent()
   }
}
