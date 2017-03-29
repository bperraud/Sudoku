import java.util.*;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EnvironmentAgent extends Agent {

    private SudokuGrid sudokuGrid = new SudokuGrid();
    private boolean gridIsInitialized = false;

    class handleRequestsBehaviour extends CyclicBehaviour {

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null) {
                String code = requestMessage.getContent();

                ACLMessage reply = requestMessage.createReply();
                reply.setPerformative(ACLMessage.REQUEST);
//                reply.setContent(message);
                send(reply);

            } else
                block();

        }
    }

    class handleAnswersBehaviour extends CyclicBehaviour {

        private void sendCompletedSudokuGrid() {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            String inlineGrid;

            StringBuilder sb = new StringBuilder();

//            for (Cell cell :
//                    sudokuGrid) {
//                sb.append(cell.getContent()).append(" ");
//            }

            inlineGrid = sb.toString();

            message.setContent(inlineGrid);
            AID receiver = getAID("Simulator");
            message.addReceiver(receiver);
            send(message);
        }

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null) {
                String code = requestMessage.getContent();

                ACLMessage reply = requestMessage.createReply();
                reply.setPerformative(ACLMessage.REQUEST);
//                reply.setContent(message);
                send(reply);

            } else if (sudokuGrid.isCompleted()) {

                sendCompletedSudokuGrid();

            } else
                block();

        }
    }

    class InitSudokuGridBehaviour extends Behaviour {

        private void initGrid(String inlineSudoku) {
            Scanner sc = new Scanner(inlineSudoku);
            int[] values = new int[81];
            int i = 0;

            while (sc.hasNext()) {
                values[i++] = Integer.parseInt(sc.next());
            }

            sudokuGrid.setCells(values);
            gridIsInitialized = true;

            sudokuGrid.printGrid();
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null) {

                initGrid(requestMessage.getContent());

//                addBehaviour(new handleRequestsBehaviour());
            } else
                block();

        }

        @Override
        public boolean done() {
            return gridIsInitialized;
        }
    }

    protected void setup() {
        System.out.println("Agent created ! Name : " + getLocalName());

        addBehaviour(new InitSudokuGridBehaviour());
    }

}