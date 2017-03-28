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

        }
    }

    class RunAnalyzeBehaviour extends CyclicBehaviour {

        private int step = 0;

//        private int[] analyze(int[] cells) {
//            HashMap<Integer, Boolean> map = new HashMap<>();
////            int[]
//
//            for (int i = 1; i <= 9; i++) {
//                map.put(i, false);
//            }
//
//            for (int i = 1; i <= cells.length; i++) {
//                if (cells[i-1] == 0)
//                    continue;
//
//                if (!map.get(cells[i-1]))
//                    map.put(i, true);
////                else
//            }
//
//
//        }

        @Override
        public void action() {

            switch (step) {
                case 0:
                    break;
                case 1:
                    break;
            }

        }
    }

    protected void setup() {
        System.out.println("Agent created ! Name : " + getLocalName());
//        addBehaviour(new RunAnalyzeBehaviour());
        addBehaviour(new SubscribeBehaviour());
    }

}