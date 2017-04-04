import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.io.File;

public class SudokuMain {

    public static void main(String[] args) {

        Runtime rt = Runtime.instance();
        ProfileImpl p;

        try {

            String SECONDARY_PROPERTIES_FILE = "secondary_container.txt";
            p = new ProfileImpl(SECONDARY_PROPERTIES_FILE);
            ContainerController cc = rt.createAgentContainer(p);

            File f = new File("src/sudoku.txt");
            Object[] sudoku = new Object[]{f};

            AgentController ac = cc.createNewAgent("Simulator", "SimulatorAgent", sudoku);
            ac.start();

            ac = cc.createNewAgent("Environment", "EnvironmentAgent", null);
            ac.start();


            for (int i = 1; i <= 27; i++) {
                ac = cc.createNewAgent("Analyzer_" + i, "AnalyzerAgent", null);
                ac.start();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}