import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Cell {

    private int content;
    private List<Integer> possibilities = new ArrayList<>();

    Cell() {
        this(0);
    }

    private Cell(int content) {
        this.content = content;
    }

    public Cell(@JsonProperty("content")int content, @JsonProperty("possibilities")List<Integer> possibilities) {
        this.content = content;
        this.possibilities = possibilities;
    }

    public int getContent() {
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
