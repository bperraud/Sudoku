import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

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

    private ClockBehaviour clockBehaviour = null;

    private class HandleSimulationBehaviour extends SequentialBehaviour {
    }

    class TerminateSimulationBehaviour extends Behaviour {

        private boolean sudokuIsSolved = false;

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage informMessage = receive(mt);

            if (informMessage != null) {

                System.out.println(ANSI_GREEN + "Sudoku is finished !" + ANSI_RESET);
                sudokuIsSolved = true;
                clockBehaviour.stop();

                Scanner sc = new Scanner(informMessage.getContent());
                int[] values = new int[81];
                int i = 0;

                while (sc.hasNext()) {
                    values[i++] = Integer.parseInt(sc.next());
                }

                SudokuGrid finishedGrid = new SudokuGrid();
                finishedGrid.setCells(values);
                finishedGrid.printGrid();

            } else
                block();

        }

        @Override
        public boolean done() {
            return sudokuIsSolved;
        }
    }

    class WaitForSubscriptionBehaviour extends Behaviour {

        private int nb_subscriptions_received = 0;

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null) {
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

        ClockBehaviour(Agent a) {
            super(a, (long) 500);
        }

        @Override
        protected void onTick() {
            for (int type : types) {
                for (int i = 0; i < ANALYZERS_PER_TYPE; i++) {
                    sendTaskRequest(analyzers[type][i], type, i);
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
            clockBehaviour = new ClockBehaviour(getAgent());
            addBehaviour(clockBehaviour);
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
        storeInlineSudoku((File) getArguments()[0]);

        analyzers = new AID[NB_TYPES][ANALYZERS_PER_TYPE];

        types = new int[NB_TYPES];
        types[LINE_TYPE] = LINE_TYPE;
        types[COLUMN_TYPE] = COLUMN_TYPE;
        types[SQUARE_TYPE] = SQUARE_TYPE;

        HandleSimulationBehaviour seq = new HandleSimulationBehaviour();
        seq.addSubBehaviour(new WaitForSubscriptionBehaviour());
        seq.addSubBehaviour(new InitSimulationBehaviour());
        seq.addSubBehaviour(new TerminateSimulationBehaviour());
        addBehaviour(seq);
    }

}