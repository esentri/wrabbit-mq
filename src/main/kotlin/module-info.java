module com.esentri.wrabbitmq {
   requires transitive kotlin.stdlib;
   requires kotlin.stdlib.jdk8;
   requires com.rabbitmq.client;
   requires slf4j.api;
   exports com.esentri.wrabbitmq;
}