package org.mpashka.tests.misc.akka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AkkaReceiver extends UntypedActor  {
    @Override
    public void onReceive(Object message) throws Exception, Exception {
        log.info("Receive message: {}", message);
//        getSender().tell(new ReceiveCompleted(), getSelf());

        log.info("Sleep 200 ms ...");
        Thread.sleep(200);
        log.info("Sleep done");
        getSender().tell("Done", ActorRef.noSender());
    }

    public static class ReceiveCompleted {
    }
}
