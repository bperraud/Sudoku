import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.*;

public class AnalyzerAgent extends Agent {

    class SubscribeBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
            message.setContent(getLocalName());
            AID receiver = getAID("Simulator");
            message.addReceiver(receiver);
            send(message);
//            System.out.println("subscription sent!");

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



        /**
         * Lorsqu'une cellule n'a plus qu'une valeur possible, celle-ci en devient son
         * contenu et la liste des possibles est vidée.
         *
         * @param cell the cell to analyze
         */
        private void analysis1(Cell cell) {
            Set<Integer> possibilities = cell.getPossibilities();
            if (possibilities.size() == 1) {
                cell.setContent(possibilities.iterator().next());
                possibilities.clear();
            }
        }

        /**
         * Si une cellule a un contenu déterminé alors il doit être retiré des possibles de
         * toutes les autres cellules non déterminées.
         *
         * @param cells the cells to analyze
         */
        private void analysis2(Cell[] cells) {
            Set<Integer> knownValues = new HashSet<>();
            for (Cell cell : cells) {
                if (cell.isSet())
                    knownValues.add(cell.getContent());
            }

            for (Cell cell : cells) {
                if (!cell.isSet()) {
                    for (Integer knownValue : knownValues) {
                        cell.removePossibility(knownValue);
                    }
                }
            }
        }

        /**
         * Une valeur ne se trouvant que dans une seule liste de possibles est la valeur
         * de cette cellule.
         *
         * @param cells the cells to analyze
         */
        private void analysis3(Cell[] cells) {
            Set<Integer> valuesToFind = SudokuGrid.getValuesToFindFromCells(cells);
            for (Integer value : valuesToFind) {
                Cell cellCandidate = null;
                for (Cell cell : cells) {
                    if (cell.getPossibilities().contains(value)) {
                        if (cellCandidate != null) {
                            cellCandidate = null;
                            break;
                        }
                        cellCandidate = cell;
                    }
                }

                if (cellCandidate != null) {
                    cellCandidate.setContent(value);
                    cellCandidate.getPossibilities().clear();
                }
            }
        }

        /**
         * Si seulement deux cellules contiennent les deux mêmes valeurs possibles
         * alors les possibles des autres cellules ne peuvent contenir ces valeurs.
         *
         * @param cells the cells to analyze
         */
        private void analysis4(Cell[] cells) {

            for (int i = 0; i < cells.length; i++) {
                Cell cell = cells[i];
                Set<Integer> possibilities = cell.getPossibilities();

                if (possibilities.size() != 2)
                    continue;

                for (int j = i + 1; j < cells.length; j++) {
                    Cell otherCell = cells[j];
                    Set<Integer> otherPossibilities = otherCell.getPossibilities();

                    if (otherPossibilities.size() != 2)
                        continue;

                    if (possibilities.equals(otherPossibilities)) {
                        for (int k = 0; k < cells.length; k++) {
                            if (k == i || k == j)
                                continue;

                            for (Object p : possibilities.toArray()) {
                                cells[k].removePossibility((Integer) p);
                            }
                        }
                    }
                }
            }


        }


        private void runAnalysis(ACLMessage message) {

            String s = message.getContent();
            ObjectMapper mapper = new ObjectMapper();
            Cell[] cells;

            try {
                cells = mapper.readValue(s, Cell[].class);

//                System.out.println(SimulatorAgent.ANSI_YELLOW + getLocalName() + " received: " + s + SimulatorAgent.ANSI_RESET);

//                List<Integer> valuesToFind = SudokuGrid.getValuesToFindFromCells(cells);
//                Integer[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9};
//                List<Integer> valuesToFind = new LinkedList<>(Arrays.asList(values));
//                List<Integer> knownValues = new LinkedList<>();
//                for (Cell cell : cells) {
//                    if (cell.getContent() != 0)
//                        knownValues.add(cell.getContent());
//                }
//
//                valuesToFind.removeAll(knownValues);

//                System.out.println(SimulatorAgent.ANSI_CYAN + "values to find : " + valuesToFind + SimulatorAgent.ANSI_RESET);
//                System.out.println(SimulatorAgent.ANSI_CYAN + "known values : " + knownValues + SimulatorAgent.ANSI_RESET);



//                    System.out.println(SimulatorAgent.ANSI_CYAN + getLocalName() + " received cell: " + cell.getContent() + ", " + cell.getPossibilities() + SimulatorAgent.ANSI_RESET);


                analysis4(cells);
                analysis2(cells);
                analysis3(cells);
                for (Cell cell : cells) {
                    analysis1(cell);
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