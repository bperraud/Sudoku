import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

                String s = informMessage.getContent();
                ObjectMapper mapper = new ObjectMapper();

                try {
                    SudokuGrid sudokuGrid = mapper.readValue(s, SudokuGrid.class);
                    sudokuGrid.printGrid();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

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

        SudokuGrid sudokuGrid;

        InitSimulationBehaviour(SudokuGrid sudokuGrid) {
            this.sudokuGrid = sudokuGrid;
        }

        private void sendSudokuGrid() {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setContent(SudokuMain.serializeToJson(sudokuGrid));
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

    private SudokuGrid createSudokuGrid(File sudoku) {
        SudokuGrid sudokuGrid = new SudokuGrid();

        try {
            Scanner sc = new Scanner(sudoku);
            int[] values = new int[81];
            int i = 0;

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("//"))
                    continue;

                Scanner lineScanner = new Scanner(line);
                while (lineScanner.hasNext()) {
                values[i++] = Integer.parseInt(lineScanner.next());
                }
            }

            sudokuGrid.setCells(values);
            sudokuGrid.initPossibilities(values);

            System.out.println("Initial grid :");
            sudokuGrid.printGrid();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return sudokuGrid;
    }

    protected void setup() {
        SudokuGrid sudokuGrid = createSudokuGrid((File) getArguments()[0]);

        analyzers = new AID[NB_TYPES][ANALYZERS_PER_TYPE];

        types = new int[NB_TYPES];
        types[LINE_TYPE] = LINE_TYPE;
        types[COLUMN_TYPE] = COLUMN_TYPE;
        types[SQUARE_TYPE] = SQUARE_TYPE;

        HandleSimulationBehaviour seq = new HandleSimulationBehaviour();
        seq.addSubBehaviour(new WaitForSubscriptionBehaviour());
        seq.addSubBehaviour(new InitSimulationBehaviour(sudokuGrid));
        seq.addSubBehaviour(new TerminateSimulationBehaviour());
        addBehaviour(seq);
    }

}