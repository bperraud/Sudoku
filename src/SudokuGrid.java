import java.util.*;

class SudokuGrid {

    private Cell[][] cells;

    SudokuGrid() {
        cells = new Cell[9][9];
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                cells[i][j] = new Cell();
    }

    Cell[] getCells() {
        Cell[] allCells = new Cell[81];
        for (int i = 0; i < 9; i++) {
            Cell[] line = getLine(i);
            System.arraycopy(line, 0, allCells, i * 9, 9);
        }
        return allCells;
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

//        System.out.println("initPossibilities");
//        for (int i = 0; i < values.length; i++) {
//            int value = values[i];
//            System.out.println("v " + i + " : " + value);
//        }


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
//                    System.out.println("setPossibility!!!");
//                    System.out.println(cell.getIndex());

                    cell.setPossibilities(maxPossibilities);
                }
            }
        }

    }

    void setCells(int[][] values) {
        if (values.length != 9 || values[0].length != 9)
            return;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setContent(values[i][j]);
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

    void setLine(int index, int[] values) {
        if (values.length != 9)
            return;

        for (int j = 0; j < 9; j++)
            cells[index][j].setContent(values[j]);
    }

    void setColumn(int index, int[] values) {
        if (values.length != 3)
            return;

        for (int i = 0; i < 3; i++)
            cells[i][index].setContent(values[i]);
    }

    void setCell(int line, int column, int val) {
        cells[line][column].setContent(val);
    }

    void printGrid() {
        printGrid(null);
    }

    void printGrid(int[] newCellPosition) {
        System.out.println("-------------------");

        for (int i = 0; i < 9; i++) {

            System.out.print("|");

            for (int j = 0; j < 9; j++) {

                Cell cell = cells[i][j];

                if (newCellPosition != null && newCellPosition[0] == i && newCellPosition[1] == j)
                    System.out.print(SimulatorAgent.ANSI_GREEN + cell.getContent() + SimulatorAgent.ANSI_RESET);
                else
                    System.out.print(cell.getContent());

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
