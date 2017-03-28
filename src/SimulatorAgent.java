import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SimulatorAgent extends Agent {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private List<String> analyzers = new LinkedList<>();
    private int SUBSCRIBERS_WANTED = 27;

    private String inlineSudoku = "";


    private class HandleSimulationBehaviour extends SequentialBehaviour {
    }

    class waitForSubscriptionBehaviour extends Behaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null) {
                System.out.println("subscription received!");
                String agentName = requestMessage.getContent();
                analyzers.add(agentName);
            } else if (analyzers.size() < SUBSCRIBERS_WANTED)
                block();
        }

        @Override
        public boolean done() {
            return analyzers.size() == SUBSCRIBERS_WANTED;
        }
    }

    protected void setup() {
        System.out.println("Agent created ! Name : " + getLocalName());

        System.out.println(Arrays.toString(getArguments()));

        File sudoku = (File) getArguments()[0];

        try {
            Scanner sc = new Scanner(sudoku);
            StringBuilder sb = new StringBuilder();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                sb.append(line).append(" ");
            }

            inlineSudoku = sb.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        class ClockBehaviour extends TickerBehaviour {

            private void sendTaskRequest(String agentName, int code) {

                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.setContent(String.valueOf(code));
                message.setInReplyTo(agentName);
                AID receiver = getAID("Environment");
                message.addReceiver(receiver);
                send(message);

            }

            ClockBehaviour(Agent a, long period) {
                super(a, period);
            }

            @Override
            protected void onTick() {
                int index = 0;

                for (String analyzer :
                        analyzers) {
                    sendTaskRequest(analyzer, ++index);
                }

            }
        }


        class InitSimulationBehaviour extends OneShotBehaviour {

            private void sendSudokuGrid() {
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setContent(inlineSudoku);
                AID receiver = getAID("Environment");
                message.addReceiver(receiver);
                send(message);
            }

            private void initTickerBehaviour() {
                addBehaviour(new ClockBehaviour(getAgent(), 1000));
            }

            @Override
            public void action() {

                sendSudokuGrid();
                initTickerBehaviour();

            }
        }

        SequentialBehaviour seq = new HandleSimulationBehaviour();
        seq.addSubBehaviour(new waitForSubscriptionBehaviour());
        seq.addSubBehaviour(new InitSimulationBehaviour());
        addBehaviour(seq);
    }

}