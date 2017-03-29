import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class AnalyzerAgent extends Agent {

    class SubscribeBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
            message.setContent(getLocalName());
            AID receiver = getAID("Simulator");
            message.addReceiver(receiver);
            send(message);
            System.out.println("subscription sent!");

            addBehaviour(new RunAnalyzeBehaviour());
        }
    }

    class RunAnalyzeBehaviour extends CyclicBehaviour {

        @Override
        public void action() {

        }
    }

    protected void setup() {
        System.out.println("Agent created ! Name : " + getLocalName());
        addBehaviour(new SubscribeBehaviour());
    }

}