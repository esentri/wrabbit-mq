package com.esentri.wrabbitmq

typealias WrabbitReplier<M, R> = (message: M) -> R

typealias WrabbitReplierWithContext<M, R> = (context: Map<String, Any?>, message: M) -> R
