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

            addBehaviour(new AnalyzeBehaviour());
        }
    }

    class AnalyzeBehaviour extends CyclicBehaviour {

        private void answer(Cell[] cells, ACLMessage message) {
            ACLMessage answer = message.createReply();
            answer.setPerformative(ACLMessage.INFORM);

            EnvironmentAgent.serializeCellsToMessage(cells, answer);
            send(answer);
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
        addBehaviour(new SubscribeBehaviour());
    }

}