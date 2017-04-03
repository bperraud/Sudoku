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

    static final String ANSI_RESET = "\u001B[0m";
    static final String ANSI_BLACK = "\u001B[30m";
    static final String ANSI_RED = "\u001B[31m";
    static final String ANSI_GREEN = "\u001B[32m";
    static final String ANSI_YELLOW = "\u001B[33m";
    static final String ANSI_BLUE = "\u001B[34m";
    static final String ANSI_PURPLE = "\u001B[35m";
    static final String ANSI_CYAN = "\u001B[36m";
    static final String ANSI_WHITE = "\u001B[37m";

    private AID[][] analyzers;

    private int NB_TYPES = 3;

    static final int LINE_TYPE = 0;
    static final int COLUMN_TYPE = 1;
    static final int SQUARE_TYPE = 2;

    private int[] types;

    static int ANALYZERS_PER_TYPE = 9;
    private int SUBSCRIBERS_WANTED = ANALYZERS_PER_TYPE * NB_TYPES;

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
                analyzers[nb_subscriptions_received / ANALYZERS_PER_TYPE][nb_subscriptions_received % ANALYZERS_PER_TYPE] = requestMessage.getSender();
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

        private void sendTaskRequest(AID agent, int type, int index) {

            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setContent(String.valueOf(type * ANALYZERS_PER_TYPE + index));
            message.setInReplyTo(agent.getLocalName());
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
//                    System.out.println(ANSI_GREEN + "analyzer " + analyzers[type][i].getLocalName() + " handles the " +
//                            (type == LINE_TYPE ? "line" : (type == COLUMN_TYPE ? "column" : "square")) +
//                            " number " + (i + 1) + ANSI_RESET
//                    );
                }
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
            addBehaviour(new ClockBehaviour(getAgent(), 500));
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

        analyzers = new AID[NB_TYPES][ANALYZERS_PER_TYPE];

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