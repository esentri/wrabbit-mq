package com.esentri.wrabbitmq

import com.esentri.wrabbitmq.internal.SendAndReceiveMessage
import com.esentri.wrabbitmq.internal.SendMessage
import com.rabbitmq.client.AMQP
import java.util.concurrent.CompletableFuture

open class WrabbitMessageBuilder<MESSAGE>(private val topicName: String,
                                          private val basicProperties: AMQP.BasicProperties) {

   internal val headers = HashMap<String, Any>()

   open fun property(key: String, value: Any): WrabbitMessageBuilder<MESSAGE> {
      this.headers[key] = value
      return this
   }

   fun send(content: MESSAGE) {
      this.headers.putAll(basicProperties.headers)
      SendMessage(topicName, basicProperties.builder().headers(this.headers).build(), content!!)
   }

}

class WrabbitMessageBuilderReplier<MESSAGE, RETURN>(private val topicName: String,
                                                    private val basicProperties: AMQP.BasicProperties) :
   WrabbitMessageBuilder<MESSAGE>(topicName, basicProperties) {

   override fun property(key: String, value: Any): WrabbitMessageBuilderReplier<MESSAGE, RETURN> {
      super.property(key, value)
      return this
   }

   @JvmOverloads
   fun sendAndReceive(content: MESSAGE, timeoutMS: Long = WrabbitReplyTimeoutMS()): CompletableFuture<RETURN> {
      this.headers.putAll(basicProperties.headers)
      return SendAndReceiveMessage<RETURN>(topicName, basicProperties.builder().headers(this.headers).build(), content!!, timeoutMS)
   }
}