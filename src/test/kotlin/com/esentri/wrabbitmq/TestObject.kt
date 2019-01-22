package com.esentri.wrabbitmq

import java.io.Serializable

class TestObjectNumberText(val number: Int, val text: String): Serializable

class TestObjectObject(val obj: TestObjectNumberText): Serializable
