package org.mpashka.tests.misc.akka;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.ActorSystemImpl;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.AskableActorRef;
import akka.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import scala.Function1;
import scala.PartialFunction;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import scala.util.Try;

@Slf4j
public class Sender {

    private ActorSystem system;
    private ActorRef receiver;
    private ActorRef callback;
    private CountDownLatch countDownLatch;

    public void doTest() throws InterruptedException {
        system = ActorSystem.create("test-system");
        receiver = system.actorOf(Props.create(AkkaReceiver.class));
        callback = system.actorOf(Props.create(SenderCallback.class, this));
        countDownLatch = new CountDownLatch(1);
        doSend();
        countDownLatch.await();
        log.info("Shutdown");
        system.shutdown();
        system.awaitTermination();
        log.info("Shutdown completed");
    }

    private void doSend() {
        log.info("Sending message");
//        receiver.tell("Hello from callback", callback);

        Future<Object> future = new AskableActorRef(receiver).ask("Hello from callback", new Timeout(10, TimeUnit.SECONDS));
        ActorSystemImpl as = (ActorSystemImpl) system;
        ExecutionContextExecutor dispatcher = as.dispatcher();
//        Function1<Try<Object>, Try<Object>> tryTryFunction1 = v1 -> {
//            callback();
//            return v1;
//        };

        future
                .onComplete(new Function1<Try<Object>, Object>() {
                    @Override
                    public Object apply(Try<Object> v1) {
                        log.info("Result received: {}", v1);
                        callback();
                        return v1;
                    }

                    @Override
                    public <A> Function1<A, Object> compose(Function1<A, Try<Object>> g) {
                        return null;
                    }

                    @Override
                    public <A> Function1<Try<Object>, A> andThen(Function1<Object, A> g) {
                        return null;
                    }

                    public void apply$mcVJ$sp(long l) {}
                    public long apply$mcJJ$sp(long l) {return l;}
                    public int apply$mcIJ$sp(long l) {return (int) l;}
                    public float apply$mcFJ$sp(long l) {return l;}
                    public double apply$mcDJ$sp(long l) {return l;}
                    public boolean apply$mcZJ$sp(long l) {return l != 0;}
                    public void apply$mcVI$sp(int l) {}
                    public long apply$mcJI$sp(int l) {return l;}
                    public int apply$mcII$sp(int l) {return l;}
                    public float apply$mcFI$sp(int l) {return l;}
                    public double apply$mcDI$sp(int l) {return l;}
                    public boolean apply$mcZI$sp(int l) {return l != 0;}
                    public void apply$mcVF$sp(float l) {}
                    public long apply$mcJF$sp(float l) {return (long) l;}
                    public int apply$mcIF$sp(float l) {return (int) l;}
                    public float apply$mcFF$sp(float l) {return l;}
                    public double apply$mcDF$sp(float l) {return l;}
                    public boolean apply$mcZF$sp(float l) {return l != 0;}
                    public void apply$mcVD$sp(double l) {}
                    public long apply$mcJD$sp(double l) {return (long) l;}
                    public int apply$mcID$sp(double l) {return (int) l;}
                    public float apply$mcFD$sp(double l) {return (float) l;}
                    public double apply$mcDD$sp(double l) {return l;}
                    public boolean apply$mcZD$sp(double l) {return l != 0;}
                    public void apply$mcVB$sp(boolean l) {}
                    public long apply$mcJB$sp(boolean l) {return 0;}
                    public int apply$mcIB$sp(boolean l) {return 0;}
                    public float apply$mcFB$sp(boolean l) {return 0;}
                    public double apply$mcDB$sp(boolean l) {return 0;}
                    public boolean apply$mcZB$sp(boolean l) {return true;}
                }, dispatcher);
    }

    private void callback() {
        log.info("Callback");
        countDownLatch.countDown();
    }

    public static class SenderCallback extends UntypedActor {

        private final Sender sender;

        public SenderCallback(Sender sender) {
            this.sender = sender;
//            getContext().dispatcher();
        }

        @Override
        public void onReceive(Object message) throws Exception, Exception {
            log.info("Receive message: {}", message);
            if (message instanceof AkkaReceiver.ReceiveCompleted) {
                sender.callback();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Sender().doTest();
    }
}
