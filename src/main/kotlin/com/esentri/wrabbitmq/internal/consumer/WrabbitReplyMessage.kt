package com.esentri.wrabbitmq.internal.consumer

import java.io.Serializable

data class WrabbitReplyMessage<REPLY>(
   val value: REPLY? = null,
   val exception: Exception? = null
): Serializable
