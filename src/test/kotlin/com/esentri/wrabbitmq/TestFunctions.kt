package com.esentri.wrabbitmq

import org.fest.assertions.Assertions.assertThat
import java.io.Serializable
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun Await(latch: CountDownLatch, seconds: Long = 10) {
   assertThat(latch.await(seconds, TimeUnit.SECONDS)).isTrue()
}

fun <MESSAGE : Serializable, REPLY : Serializable> NewEventWithReply(topic: WrabbitTopic) =
   WrabbitEventWithReply<MESSAGE, REPLY>(topic, UUID.randomUUID().toString())
