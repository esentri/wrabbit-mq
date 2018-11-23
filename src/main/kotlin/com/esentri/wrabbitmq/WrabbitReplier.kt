package com.esentri.wrabbitmq

typealias WrabbitReplier<M, R> = (message: M) -> R

