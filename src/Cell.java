import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * A cell is a Sudoku grid brick, with a content and a list of possibilities
 */
class Cell {
    /**
     * content : the value of the cell (0 if not determined yet)
     * possiblities : the possible values for the cell (empty if its content is set)
     */
    @JsonProperty("content")
    private int content;
    @JsonProperty("possibilities")
    private Set<Integer> possibilities;

    Cell() {
        this.content = 0;
        this.possibilities = new HashSet<>();
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

    void removePossibility(int p) {
        if (this.possibilities.contains(p))
            this.possibilities.remove(p);
    }

    void updatePossibilities(Set<Integer> newPossibilities) {

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
