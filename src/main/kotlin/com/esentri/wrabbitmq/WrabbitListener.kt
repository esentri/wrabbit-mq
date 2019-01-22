package com.esentri.wrabbitmq

typealias WrabbitListener<M> = (message: M) -> Unit

typealias WrabbitListenerWithContext<M> = (context: Map<String, Any?>, message: M) -> Unit
