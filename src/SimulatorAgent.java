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

    private String[][] analyzers;

    private int NB_TYPES = 3;

    private Integer LINE_TYPE = 0;
    private Integer COLUMN_TYPE = 1;
    private Integer SQUARE_TYPE = 2;

    private int[] types;

    private int ANALYZERS_PER_TYPE = 9;
    private int SUBSCRIBERS_WANTED = ANALYZERS_PER_TYPE * 3;

    private String inlineSudoku = "";

    private class HandleSimulationBehaviour extends SequentialBehaviour {
    }

    class waitForSubscriptionBehaviour extends Behaviour {

        private int nb_subscriptions_received = 0;

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null) {
                System.out.println("subscription received!");
                String agentName = requestMessage.getContent();
                analyzers[nb_subscriptions_received / ANALYZERS_PER_TYPE][nb_subscriptions_received % ANALYZERS_PER_TYPE] = agentName;
                nb_subscriptions_received++;
            } else if (nb_subscriptions_received < SUBSCRIBERS_WANTED)
                block();
        }

        @Override
        public boolean done() {
            return nb_subscriptions_received == SUBSCRIBERS_WANTED;
        }
    }

    class ClockBehaviour extends TickerBehaviour {

        private void sendTaskRequest(String agentName, int type, int index) {

            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setContent(String.valueOf(type * ANALYZERS_PER_TYPE + index));
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

            for (int type : types) {
                for (int i = 0; i < ANALYZERS_PER_TYPE; i++) {
                    sendTaskRequest(analyzers[type][i], type, i);
//                    System.out.println("analyzer " + analyzers[type][i] + " handles " + type + "; i: " + i);
                }
            }

//            System.out.println("-----------------------------------");
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

    private void storeInlineSudoku(File sudoku) {
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
    }

    protected void setup() {
        System.out.println("Agent created ! Name : " + getLocalName());

        storeInlineSudoku((File) getArguments()[0]);

        analyzers = new String[NB_TYPES][ANALYZERS_PER_TYPE];

        types = new int[NB_TYPES];
        types[LINE_TYPE] = LINE_TYPE;
        types[COLUMN_TYPE] = COLUMN_TYPE;
        types[SQUARE_TYPE] = SQUARE_TYPE;

        SequentialBehaviour seq = new HandleSimulationBehaviour();
        seq.addSubBehaviour(new waitForSubscriptionBehaviour());
        seq.addSubBehaviour(new InitSimulationBehaviour());
        addBehaviour(seq);
    }

}