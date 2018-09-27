package com.esentri.wrabbitmq

typealias MsgReplier<M, R> = (M) -> R

typealias MsgCtxReplier<M, R> = (M, Map<String, Any?>) -> R

interface IMessageReplier<M, R>

interface MessageReplier<M, R> : IMessageReplier<M, R> {

    fun reply(message: M): R
}

interface MessageWithContextReplier<M, R> : IMessageReplier<M, R> {

    fun reply(message: M, context: Map<String, Any?>): R
}



typealias MsgListener<M> = (M) -> Unit

typealias MsgCtxListener<M> = (M, Map<String, Any?>) -> Unit

interface IMessageListener<M>

interface MessageListener<M> : IMessageListener<M> {

    fun listen(message: M)
}

interface MessageWithContextListener<M> : IMessageListener<M> {

    fun listen(message: M, context: Map<String, Any?>)
}
