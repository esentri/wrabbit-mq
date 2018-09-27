package com.esentri.wrabbitmq;

import com.esentri.wrabbitmq.domain.TestDomainJava;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IntegrationTest {

   private static final Logger LOGGER = LoggerFactory.getLogger("IntegrationTest");

   @Test
   public void testAuditor() {
      TestDomainJava.SimpleTopic.Event1_StringToNumber.listener(
          (MessageListener<String>) string -> LOGGER.info("SimpleTopic.Event1.auditor: " + string)
      );
      TestDomainJava.SimpleTopic.Event2_NumberToString.listener(
          (MessageListener<Integer>) number -> LOGGER.info("SimpleTopic.Event2.auditor: " + number)
      );
      TestDomainJava.SimpleTopic.listener(
          (MessageListener<Object>) obj -> LOGGER.info("SimpleTopic.auditor: " + obj)
      );

      TestDomainJava.SimpleTopic.Event1_StringToNumber.send("1234");
      TestDomainJava.SimpleTopic.Event2_NumberToString.send(321);

      waitSeconds(5);
   }

   @Test
   public void testListener() {
      TestDomainJava.SimpleTopic.Event1_StringToNumber.listener(
          (MessageListener<String>) string -> LOGGER.info("SimpleTopic.Event1.listener: " + string)
      );
      TestDomainJava.SimpleTopic.Event2_NumberToString.listener(
          (MessageListener<Integer>) number -> LOGGER.info("SimpleTopic.Event2.listener: " + number)
      );

      TestDomainJava.SimpleTopic.Event1_StringToNumber.send("1234");
      TestDomainJava.SimpleTopic.Event2_NumberToString.send(321);

      waitSeconds(5);
   }

   @Test
   public void testReplier() {
      TestDomainJava.SimpleTopic.NestedTopic.Event1_IncrementNumber.replier(number -> ++number);
      TestDomainJava.SimpleTopic.NestedTopic.Event2_DecrementNumber.replier(number -> --number);

      TestDomainJava.SimpleTopic.NestedTopic.Event1_IncrementNumber
         .sendAndReceive(7).thenAccept(returnValue -> LOGGER.info("Incremented number: " + returnValue));

      TestDomainJava.SimpleTopic.NestedTopic.Event2_DecrementNumber
         .sendAndReceive(7).thenAccept(returnValue -> LOGGER.info("Decremented number: " + returnValue));

      waitSeconds(5);
   }

   private void waitSeconds(int secondsToWait) {
      int passedSeconds = 0;
      while(passedSeconds < secondsToWait) {
         try {
            Thread.sleep(1000);
            passedSeconds++;
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }
}
