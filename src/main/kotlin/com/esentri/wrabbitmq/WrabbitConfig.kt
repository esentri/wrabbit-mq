package com.esentri.wrabbitmq

// DEFAULTS
private const val WrabbitDefaultHost = "localhost"
private const val WrabbitDefaultPort: Int = 5672
private const val WrabbitDefaultUsername = "guest"
private const val WrabbitDefaultPassword = "guest"
private const val WrabbitDefaultTimeout: Int= 30000
private const val WrabbitDefaultHeartBeat = 30


// CONFIGS (system property or default)
fun WrabbitHost() = System.getProperty("spring.rabbitmq.host")?: WrabbitDefaultHost
fun WrabbitPort(): Int = System.getProperty("spring.rabbitmq.port")?.toInt()?: WrabbitDefaultPort
fun WrabbitUsername() = System.getProperty("spring.rabbitmq.username")?: WrabbitDefaultUsername
fun WrabbitPassword() = System.getProperty("spring.rabbitmq.password")?: WrabbitDefaultPassword
fun WrabbitTimeout(): Int = System.getProperty("spring.rabbitmq.connection-timeout")?.toInt()?: WrabbitDefaultTimeout
fun WrabbitHeartBeat(): Int = System.getProperty("spring.rabbitmq.requested-heartbeat")?.toInt()?: WrabbitDefaultHeartBeat
