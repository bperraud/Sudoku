import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static jade.tools.sniffer.Agent.i;

public class EnvironmentAgent extends Agent {

    private SudokuGrid sudokuGrid = new SudokuGrid();
    private boolean gridIsInitialized = false;

    private Map<AID, Integer> agentsRolesMap = new HashMap<>();

    class handleRequestsBehaviour extends CyclicBehaviour {

        private void propagateRequest(ACLMessage message, int agentCode) {

            ACLMessage newMessage = new ACLMessage(ACLMessage.REQUEST);

            int type = agentCode / SimulatorAgent.ANALYZERS_PER_TYPE;
            int index = agentCode % SimulatorAgent.ANALYZERS_PER_TYPE;

//            if (!((type == SimulatorAgent.SQUARE_TYPE && index == 3) ||
//                    (type == SimulatorAgent.LINE_TYPE && index == 4) ||
//                    (type == SimulatorAgent.COLUMN_TYPE && index == 2)
//            ))
//                return;

            Cell[] cells = new Cell[9];

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

            ObjectMapper mapper = new ObjectMapper();
            String s;
            AID receiver = getAID(message.getInReplyTo());
            newMessage.addReceiver(receiver);
            agentsRolesMap.put(receiver, agentCode);

            try {
                s = mapper.writeValueAsString(cells);
                newMessage.setContent(s);

//                System.out.println(SimulatorAgent.ANSI_BLUE +
//                        "Msg sent to " + ((AID) newMessage.getAllReceiver().next()).getLocalName() +
//                        ": " + newMessage.getContent() + SimulatorAgent.ANSI_RESET);

                send(newMessage);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage requestMessage = receive(mt);

            if (requestMessage != null) {
                propagateRequest(requestMessage, Integer.parseInt(requestMessage.getContent()));
            } else
                block();
        }
    }

    class handleAnswersBehaviour extends CyclicBehaviour {

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

            if (type == SimulatorAgent.SQUARE_TYPE && index == 3) {

                System.out.println("BEFORE UPDATE :");
                sudokuGrid.printGrid();

//                for (int i = 0; i < newCells.length; i++) {
//                    Cell newCell = newCells[i];
//                    System.out.println("new cell " + i + " : " + newCell.getContent());
//                    System.out.println("new cell " + i + " : " + newCell.getPossibilities());
//                }

            }

            for (int i = 0; i < 9; i++) {
                Cell currentCell = currentCells[i];
                Cell newCell = newCells[i];

//                if (type == SimulatorAgent.SQUARE_TYPE && index == 3) {
//
//                    System.out.println("cur cell : " + currentCell.getContent());
//                    System.out.println("cur cell : " + currentCell.getPossibilities());
//                    System.out.println("new cell : " + newCell.getContent());
//                    System.out.println("new cell : " + newCell.getPossibilities());
//                }


                // This cell is already set, we pass by
                if (currentCell.getContent() != 0)
                    continue;

                if (newCell.getContent() != 0) {
                    currentCell.setContent(newCell.getContent());
                    currentCell.getPossibilities().clear();
                } else {
                    currentCell.updatePossibilities(newCell.getPossibilities(), type, index);
                }
            }


            if (type == SimulatorAgent.SQUARE_TYPE && index == 3) {

                Cell[] cellsArray = sudokuGrid.getCellsSquare(3).getCellsArray();
//                for (int i = 0; i < cellsArray.length; i++) {
//                    Cell cell = cellsArray[i];
//
//                    System.out.println("cell " + i + " " + cell.getContent());
//                    System.out.println("cell " + i + " " + cell.getPossibilities());
//
//                }

            }


//            List<Integer> valuesToFind = SudokuGrid.getValuesToFindFromCells(currentCells);
//            for (Integer value : valuesToFind) {
//                Cell cellCandidate = null;
//                for (Cell currentCell : currentCells) {
//                    if (currentCell.getPossibilities().contains(value)) {
//                        if (cellCandidate != null) {
//                            cellCandidate = null;
//                            break;
//                        }
//
//                        cellCandidate = currentCell;
//                    }
//                }
//
//                if (cellCandidate != null) {
////                    if (type == SimulatorAgent.SQUARE_TYPE && index == 3) {
////                        System.out.println("cell candidate : " + cellCandidate.getContent());
////                        System.out.println("cell candidate : " + cellCandidate.getPossibilities());
////                        System.out.println("type : " + type);
////                        System.out.println("index : " + index);
////                    }
//                    cellCandidate.setContent(value);
//                    cellCandidate.getPossibilities().clear();
//                }
//            }


//            if (type == SimulatorAgent.SQUARE_TYPE && index == 3) {
//
                System.out.println("AFTER UPDATE :");
                sudokuGrid.printGrid();
//
//            }


        }

        private void updateGrid(ACLMessage message) {

            String s = message.getContent();
            ObjectMapper mapper = new ObjectMapper();
            Cell[] newCells;
            Cell[] currentCells = null;


//            for (Cell cell : sudokuGrid.getCellsFromSquare(3)) {
//                System.out.println(SimulatorAgent.ANSI_YELLOW + "cur square cell : " + cell.getContent() + SimulatorAgent.ANSI_RESET);
//                System.out.println(SimulatorAgent.ANSI_YELLOW + "cur square cell : " + cell.getPossibilities() + SimulatorAgent.ANSI_RESET);
//            }

            try {
                newCells = mapper.readValue(s, Cell[].class);

                //System.out.println(SimulatorAgent.ANSI_RED + getLocalName() + " received from " + message.getSender().getLocalName() + ": " + s + SimulatorAgent.ANSI_RESET);


                int agentCode = agentsRolesMap.get(message.getSender());
                int type = agentCode / SimulatorAgent.ANALYZERS_PER_TYPE;
                int index = agentCode % SimulatorAgent.ANALYZERS_PER_TYPE;


                System.out.println(SimulatorAgent.ANSI_RED + (type == SimulatorAgent.LINE_TYPE ? "Line" : (type == SimulatorAgent.COLUMN_TYPE ? "Column" : "Square")) +
                        " " + (index +1) + SimulatorAgent.ANSI_RESET);

//                if (type == SimulatorAgent.SQUARE_TYPE && index == 3) {
                    for (int i = 0; i < newCells.length; i++) {
                        Cell newCell = newCells[i];
                        System.out.println(SimulatorAgent.ANSI_BLUE + "newcell " + i + " : " + newCell.getContent() + SimulatorAgent.ANSI_RESET);
                        System.out.println(SimulatorAgent.ANSI_BLUE + "newcell " + i + " : " + newCell.getPossibilities() + SimulatorAgent.ANSI_RESET);
                    }
//                }

                switch (type) {
                    case SimulatorAgent.LINE_TYPE:
                        currentCells = sudokuGrid.getLine(index);
//                        sudokuGrid.setLine(index, currentCells);
                        break;
                    case SimulatorAgent.COLUMN_TYPE:
                        currentCells = sudokuGrid.getColumn(index);
//                        sudokuGrid.setColumn(index, currentCells);
                        break;
                    case SimulatorAgent.SQUARE_TYPE:
                        currentCells = sudokuGrid.getCellsFromSquare(index);

//                        if (index == 3) {

//                            Cell[] cellsArray = sudokuGrid.getCellsSquare(index).getCellsArray();

//                            Cell[][] cells1 = sudokuGrid.getCellsSquare(index).getCells();
//                            for (int i1 = 0; i1 < cells1.length; i1++) {
//                                Cell[] cells = cells1[i1];
//                                for (int i2 = 0; i2 < cells.length; i2++) {
//                                    Cell cell = cells[i2];
//                                    System.out.println("SQUARE, cell " + (i1*3 + i2) + " : " + cell.getContent());
//                                    System.out.println("SQUARE, cell " + (i1*3 + i2) + " : " + cell.getPossibilities());
//                                }
//                            }
//                        }
//                        sudokuGrid.setCellsSquare(index, currentCells);
                        break;
                }

                updateCells(currentCells, newCells, type, index);

                for (int i1 = 0; i1 < currentCells.length; i1++) {
                    Cell currentCell = currentCells[i1];
                    System.out.println(SimulatorAgent.ANSI_CYAN + "curcell " + i1 + " (" + currentCell.getIndex() + ") : " + currentCell.getContent() + SimulatorAgent.ANSI_RESET);
                    System.out.println(SimulatorAgent.ANSI_CYAN + "curcell " + i1 + " (" + currentCell.getIndex() + ") : " + currentCell.getPossibilities() + SimulatorAgent.ANSI_RESET);
                }


            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage informMessage = receive(mt);

            if (informMessage != null) {
                updateGrid(informMessage);

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
            sudokuGrid.initPossibilities(values);

            int index = 0;

            for (i = 0; i < 9; i++) {
                for (Cell cell : sudokuGrid.getLine(i)) {
                    cell.setIndex(index++);
                }
            }

            gridIsInitialized = true;

            sudokuGrid.printGrid();



            Cell[][] cells1 = sudokuGrid.getCellsSquare(3).getCells();
            for (int i1 = 0; i1 < cells1.length; i1++) {
                Cell[] cells = cells1[i1];
                for (int i2 = 0; i2 < cells.length; i2++) {
                    Cell cell = cells[i2];
//                    System.out.println("SQUARE, cell " + (i1*3 + i2) + " : " + cell.getContent());
//                    System.out.println("SQUARE, cell " + (i1*3 + i2) + " : " + cell.getPossibilities());
                }
            }


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
        System.out.println("Agent created ! Name : " + getLocalName());

        addBehaviour(new InitSudokuGridBehaviour());
    }

}