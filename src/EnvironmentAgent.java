import java.util.*;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EnvironmentAgent extends Agent {

    private ArrayList<Cell> sudokuGrid = new ArrayList<>();

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

            for (Cell cell :
                    sudokuGrid) {
                sb.append(cell.getContent()).append(" ");
            }

            inlineGrid = sb.toString();

            message.setContent(inlineGrid);
            AID receiver = getAID("Simulator");
            message.addReceiver(receiver);
            send(message);
        }

        private boolean gridIsCompleted() {
            boolean isCompleted = true;

            Iterator<Cell> it = sudokuGrid.iterator();
            while (it.hasNext() && isCompleted) {
                Cell c = it.next();
                if (c.getContent() == 0) {
                    isCompleted = false;
                }
            }

            return isCompleted;
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

            } else if (gridIsCompleted()) {

                sendCompletedSudokuGrid();

            } else
                block();

        }
    }

    class InitSudokuGridBehaviour extends Behaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null) {
                String inlineSudoku = requestMessage.getContent();

                Scanner sc = new Scanner(inlineSudoku);

                while (sc.hasNext()) {
                    String cell = sc.next();
                    Cell newcCell = new Cell(sudokuGrid.size() + 1, Integer.parseInt(cell));
                    sudokuGrid.add(newcCell);
                }

                sudokuGrid.forEach(cell -> System.out.println(cell.getIndex() + " : " + cell.getContent()));
                addBehaviour(new handleRequestsBehaviour());

            } else
                block();

        }

        @Override
        public boolean done() {
            return !sudokuGrid.isEmpty();
        }
    }

    protected void setup() {
        System.out.println("Agent created ! Name : " + getLocalName());

        sudokuGrid = new ArrayList<>();

        addBehaviour(new InitSudokuGridBehaviour());

    }

}