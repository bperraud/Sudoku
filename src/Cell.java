import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cell {

    private int content;
    private Set<Integer> possibilities;
    private int index;

    Cell() {
        this(0);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private Cell(int content) {
        this.content = content;
        this.possibilities = new HashSet<>();
    }

    public Cell(@JsonProperty("content")int content, @JsonProperty("possibilities")Set<Integer> possibilities) {
        this.content = content;
        this.possibilities = possibilities;
    }

    public int getContent() {
        return content;
    }

    public Set<Integer> getPossibilities() {
        return possibilities;
    }

    public int getIndex() {
        return index;
    }

    boolean isSet() {
        return content != 0;
    }

//    int getIndex() {
//        return index;
//    }

    void setContent(int val) {
//        System.out.println("setContent : old val = " + this.content + ", new val = " + val);
//        if (this.content != 0)
//            System.out.println(SimulatorAgent.ANSI_RED + "PROBLEME" + SimulatorAgent.ANSI_RESET);
        this.content = val;
    }

    void setPossibilities(Set<Integer> newPossibilities) {
//        System.out.println("setPossibilities : old = " + this.possibilities + ", new = " + newPossibilities);
        this.possibilities = newPossibilities;
//        System.out.println("setPossibilities : normally = " + this.possibilities);
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

//        System.out.println("UPDATE --- ");
//        System.out.println("cur pos : " + this.possibilities);
//        System.out.println("new pos : " + newPossibilities);

        Set<Integer> newList = new HashSet<>();
        for (Integer possibility : this.possibilities) {
            if (newPossibilities.contains(possibility)) {
                newList.add(possibility);
            }
        }

//        System.out.println("index " + this.index);
//
//        if (this.index == 38) {
//            if (type == SimulatorAgent.COLUMN_TYPE && index == 2) {
//                System.out.println("cellP : " + getContent());
//                System.out.println("cellP : " + getPossibilities());
//                System.out.println("colonne");
//                System.out.println("list : " + newList);
//            } else if (type == SimulatorAgent.LINE_TYPE && index == 4) {
//                System.out.println("ligne");
//                System.out.println("list : " + newList);
//            }
//        }

        setPossibilities(newList);

//        System.out.println("new2 pos : " + this.possibilities);
    }

    boolean isCompleted() {
        return content != 0;
    }
}
