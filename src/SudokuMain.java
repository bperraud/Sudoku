import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.io.File;

public class SudokuMain {

    static String serializeToJson(Object o) {
        String s = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            s = mapper.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        return s;
    }

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