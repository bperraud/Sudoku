import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

public class Cell {

    private int content;
    private Set<Integer> possibilities;

    Cell() {
        this.content = 0;
        this.possibilities = new HashSet<>();
    }

    public Cell(@JsonProperty("content") int content, @JsonProperty("possibilities") Set<Integer> possibilities) {
        this.content = content;
        this.possibilities = possibilities;
    }

    int getContent() {
        return content;
    }

    Set<Integer> getPossibilities() {
        return possibilities;
    }

    boolean isSet() {
        return content != 0;
    }

    void setContent(int val) {
        this.content = val;
    }

    void setPossibilities(Set<Integer> newPossibilities) {
        this.possibilities = newPossibilities;
    }

    void addPossibility(int p) {
        if (!this.possibilities.contains(p))
            this.possibilities.add(p);
    }

    void removePossibility(int p) {
        if (this.possibilities.contains(p))
            this.possibilities.remove(p);
    }

    void updatePossibilities(Set<Integer> newPossibilities, int type, int index) {

        Set<Integer> newList = new HashSet<>();
        for (Integer possibility : this.possibilities) {
            if (newPossibilities.contains(possibility)) {
                newList.add(possibility);
            }
        }

        setPossibilities(newList);
    }

    boolean isCompleted() {
        return content != 0;
    }
}
