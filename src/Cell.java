import java.util.ArrayList;
import java.util.List;

public class Cell {

    //    private int index;
    private int content;
    private List<Integer> possibilities = new ArrayList<>();

    Cell() {
        this.content = 0;
    }

    //    Cell(int index, int content) {
    Cell(int content) {
//        this.index = index;
        this.content = content;
    }

    int getContent() {
        return content;
    }

    public List<Integer> getPossibilities() {
        return possibilities;
    }

//    int getIndex() {
//        return index;
//    }

    void setContent(int val) {
        this.content = val;
    }

    void setPossibilities(List<Integer> possibilities) {
        this.possibilities = possibilities;
    }

    void addPossibility(int p) {
        if (!this.possibilities.contains(p))
            this.possibilities.add(p);
    }

    void removePossibility(int p) {
        if (this.possibilities.contains(p))
            this.possibilities.remove(Integer.valueOf(p));
    }

    boolean isCompleted() {
        return content != 0;
    }
}
