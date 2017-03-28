import java.util.ArrayList;
import java.util.List;

public class Cell {

    private int index;
    private int content;
    private List<Integer> possibilities = new ArrayList<>();

    Cell(int index, int content) {
        this.index = index;
        this.content = content;
    }

    int getContent() {
        return content;
    }

    public List<Integer> getPossibilities() {
        return possibilities;
    }

    int getIndex() {
        return index;
    }
}
