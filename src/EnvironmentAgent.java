import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EnvironmentAgent extends Agent {

    private SudokuGrid sudokuGrid = new SudokuGrid();
    private boolean gridIsInitialized = false;

    private Map<AID, Integer> agentsRolesMap = new HashMap<>();

    private Cell[] getCellsByAgentCode(int agentCode) {
        Cell[] cells = new Cell[9];
        int type = agentCode / SimulatorAgent.ANALYZERS_PER_TYPE;
        int index = agentCode % SimulatorAgent.ANALYZERS_PER_TYPE;
        switch (type) {
            case SimulatorAgent.LINE_TYPE:
                cells = sudokuGrid.getLine(index);
                break;
            case SimulatorAgent.COLUMN_TYPE:
                cells = sudokuGrid.getColumn(index);
                break;
            case SimulatorAgent.SQUARE_TYPE:
                cells = sudokuGrid.getCellsFromSquare(index);
                break;
        }
        return cells;
    }

    static void serializeCellsToMessage(Cell[] cells, ACLMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            String s = mapper.writeValueAsString(cells);
            message.setContent(s);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    class handleRequestsBehaviour extends Behaviour {

        private void propagateRequest(ACLMessage message, int agentCode) {

            ACLMessage newMessage = new ACLMessage(ACLMessage.REQUEST);

            Cell[] cells = getCellsByAgentCode(agentCode);

            // The structure is already set, no need to request an analysis for it
            if (SudokuGrid.getValuesToFindFromCells(cells).isEmpty())
                return;

            AID receiver = getAID(message.getInReplyTo());
            newMessage.addReceiver(receiver);
            agentsRolesMap.put(receiver, agentCode);

            serializeCellsToMessage(cells, newMessage);
            send(newMessage);
        }

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null)
                propagateRequest(requestMessage, Integer.parseInt(requestMessage.getContent()));
            else
                block();
        }

        @Override
        public boolean done() {
            return sudokuGrid.isCompleted();
        }
    }

    class handleAnswersBehaviour extends Behaviour {

        private void sendCompletedSudokuGrid() {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            String inlineGrid;

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < 9; i++) {
                for (Cell cell : sudokuGrid.getLine(i)) {
                    sb.append(cell.getContent()).append(" ");
                }
            }

            inlineGrid = sb.toString();

            message.setContent(inlineGrid);
            AID receiver = getAID("Simulator");
            message.addReceiver(receiver);
            send(message);
        }

        private void updateCells(Cell[] currentCells, Cell[] newCells, int type, int index) {

            for (int i = 0; i < 9; i++) {
                Cell currentCell = currentCells[i];
                Cell newCell = newCells[i];

                // This cell is already set, we pass by
                if (currentCell.getContent() != 0)
                    continue;

                if (newCell.getContent() != 0) {
                    currentCell.setContent(newCell.getContent());
                    currentCell.getPossibilities().clear();

                    System.out.println("Next step :");
                    sudokuGrid.printGrid(sudokuGrid.getCellPosition(currentCell));
                } else {
                    currentCell.updatePossibilities(newCell.getPossibilities(), type, index);
                }
            }

        }

        private void updateGrid(ACLMessage message) {

            String s = message.getContent();
            ObjectMapper mapper = new ObjectMapper();
            Cell[] newCells;

            try {
                newCells = mapper.readValue(s, Cell[].class);

                int agentCode = agentsRolesMap.get(message.getSender());
                int type = agentCode / SimulatorAgent.ANALYZERS_PER_TYPE;
                int index = agentCode % SimulatorAgent.ANALYZERS_PER_TYPE;

                Cell[] currentCells = getCellsByAgentCode(agentCode);

                updateCells(currentCells, newCells, type, index);


            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage informMessage = receive(mt);

            if (informMessage != null)
                updateGrid(informMessage);
            else
                block();
        }

        @Override
        public boolean done() {
            if (!sudokuGrid.isCompleted())
                return false;
            sendCompletedSudokuGrid();
            return true;
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
            sudokuGrid.initPossibilities(values);
            gridIsInitialized = true;

            System.out.println("Initial grid :");
            sudokuGrid.printGrid();
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null) {

                initGrid(requestMessage.getContent());

                addBehaviour(new handleRequestsBehaviour());
                addBehaviour(new handleAnswersBehaviour());
            } else
                block();

        }

        @Override
        public boolean done() {
            return gridIsInitialized;
        }
    }

    protected void setup() {
        addBehaviour(new InitSudokuGridBehaviour());
    }

}