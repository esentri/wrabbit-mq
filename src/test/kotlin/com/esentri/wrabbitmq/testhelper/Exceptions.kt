package com.esentri.wrabbitmq.testhelper

class TestException(message: String): RuntimeException(message)

class CustomException(message: String): RuntimeException(message)


class NotSerializable(val test: String)

class NonSerializableException(val test: NotSerializable = NotSerializable("Not serializable") ): RuntimeException()