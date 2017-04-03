import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

            addBehaviour(new AnalyzeBehaviour());
        }
    }

    class AnalyzeBehaviour extends CyclicBehaviour {

        private void answer(Cell[] cells, ACLMessage message) {

//            System.out.println(getLocalName() + " ANSWERS");

            ACLMessage answer = message.createReply();
            answer.setPerformative(ACLMessage.INFORM);

            ObjectMapper mapper = new ObjectMapper();
            String s;

            try {
                s = mapper.writeValueAsString(cells);
                answer.setContent(s);

//                System.out.println(SimulatorAgent.ANSI_PURPLE +
//                        "Msg of " + getLocalName() + " sent to " + ((AID) answer.getAllReceiver().next()).getLocalName() +
//                        ": " + answer.getContent() + SimulatorAgent.ANSI_RESET);

                send(answer);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }

        }

        private void calculatePossibilities(Cell cell, List<Integer> remainingValues) {
            int content = cell.getContent();
            List<Integer> possibilities = cell.getPossibilities();

            if (content != 0 || (remainingValues.size() >= possibilities.size() && !possibilities.isEmpty()))
                return;

            cell.setPossibilities(remainingValues);
        }

        private void runAnalysis(ACLMessage message) {

            String s = message.getContent();
            ObjectMapper mapper = new ObjectMapper();
            Cell[] cells;

            try {
                cells = mapper.readValue(s, Cell[].class);

//                System.out.println(SimulatorAgent.ANSI_YELLOW + getLocalName() + " received: " + s + SimulatorAgent.ANSI_RESET);

                Integer[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9};
                List<Integer> valuesToFind = new LinkedList<>(Arrays.asList(values));
                List<Integer> knownValues = new LinkedList<>();
                for (Cell cell : cells) {
                    if (cell.getContent() != 0)
                        knownValues.add(cell.getContent());
                }

                valuesToFind.removeAll(knownValues);

                for (Cell cell : cells) {
//                    System.out.println(SimulatorAgent.ANSI_CYAN + getLocalName() + " received cell: " + cell.getContent() + ", " + cell.getPossibilities() + SimulatorAgent.ANSI_RESET);
                    calculatePossibilities(cell, valuesToFind);
                }

                answer(cells, message);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null) {
                runAnalysis(requestMessage);
            } else
                block();
        }

    }

    protected void setup() {
        System.out.println("Agent created ! Name : " + getLocalName());
        addBehaviour(new SubscribeBehaviour());
    }

}