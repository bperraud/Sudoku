import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * The SudokuGrid encapsulates the gris to solve
 * cells is a bi-dimensional array of references to the 81 cells of the grid
 */
class SudokuGrid {

    @JsonProperty("cells")
    private final Cell[][] cells;

    private SudokuGrid(@JsonProperty("cells") Cell[][] cells) {
        this.cells = cells;
    }

    SudokuGrid() {
        cells = new Cell[9][9];
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                cells[i][j] = new Cell();
    }

    int[] getCellPosition(Cell cell) {
        List<Cell[]> asList = Arrays.asList(cells);
        int[] position = {-1, -1};
        for (int i = 0; i < asList.size(); i++) {
            Cell[] line = asList.get(i);
            if ((position[1] = Arrays.asList(line).indexOf(cell)) != -1) {
                position[0] = i;
                break;
            }
        }
        return position;
    }

    Cell[] getLine(int index) {
        return cells[index];
    }

    Cell[] getColumn(int index) {
        Cell[] line = new Cell[9];
        for (int i = 0; i < 9; i++) {
            line[i] = cells[i][index];
        }
        return line;
    }

    Cell[] getCellsFromSquare(int index) {
        Cell[] square = new Cell[9];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(cells[(index / 3) * 3 + i], (index % 3) * 3, square, i * 3, 3);
        }
        return square;
    }

    static Set<Integer> getValuesToFindFromCells(Cell[] cells) {
        Integer[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        Set<Integer> valuesToFind = new HashSet<>(Arrays.asList(values));
        Set<Integer> knownValues = getKnownValuesFromCells(cells);
        valuesToFind.removeAll(knownValues);
        return valuesToFind;
    }

    private static Set<Integer> getKnownValuesFromCells(Cell[] cells) {
        Set<Integer> knownValues = new HashSet<>();
        for (Cell cell : cells) {
            if (cell.isSet())
                knownValues.add(cell.getContent());
        }
        return knownValues;
    }

    void initPossibilities(int[] values) {
        int i;
        Set<Integer> maxPossibilities = new HashSet<>();
        for (i = 1; i <= 9; i++) {
            maxPossibilities.add(i);
        }

        for (i = 0; i < 9; i++) {
            Cell[] cellsLine = getLine(i);
            for (int j = 0; j < cellsLine.length; j++) {
                Cell cell = cellsLine[j];
                if (values[i * 9 + j] == 0) {
                    cell.setPossibilities(maxPossibilities);
                }
            }
        }
    }

    void setCells(int[] values) {
        if (values.length != 81)
            return;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setContent(values[i * 9 + j]);
            }
        }
    }

    void printGrid() {
        printGrid(null);
    }

    void printGrid(int[] newCellPosition) {
        System.out.println("-------------------");

        for (int i = 0; i < 9; i++) {

            System.out.print("|");

            for (int j = 0; j < 9; j++) {

                int cellVal = cells[i][j].getContent();
                String s;

                if (newCellPosition != null && newCellPosition[0] == i && newCellPosition[1] == j)
                    s = SimulatorAgent.ANSI_GREEN + cellVal + SimulatorAgent.ANSI_RESET;
                else if (cellVal == 0)
                    s = SimulatorAgent.ANSI_BLUE + cellVal + SimulatorAgent.ANSI_RESET;
                else
                    s = String.valueOf(cellVal);

                System.out.print(s);

                if (j % 3 == 2)
                    System.out.print("|");
                else
                    System.out.print(" ");
            }

            if (i % 3 == 2)
                System.out.println("\n-------------------");
            else
                System.out.println();
        }
    }

    boolean isCompleted() {
        boolean completed = true;
        for (Cell[] lineCells : cells) {
            for (Cell cell : lineCells) {
                if (!cell.isCompleted()) {
                    completed = false;
                    break;
                }
            }
        }

        return completed;
    }
}
