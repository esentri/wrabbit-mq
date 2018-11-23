package com.esentri.wrabbitmq

typealias WrabbitListener<M> = (message: M) -> Unit

