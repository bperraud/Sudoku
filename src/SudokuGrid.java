import java.util.*;

class SudokuGrid {

    private CellsSquare[][] cellsSquares;

    SudokuGrid() {
        cellsSquares = new CellsSquare[3][3];
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                cellsSquares[i][j] = new CellsSquare();
    }

    CellsSquare[][] getCellsSquares() {
        return cellsSquares;
    }

    CellsSquare getCellsSquare(int line, int column) {
        return cellsSquares[line][column];
    }

    CellsSquare getCellsSquare(int index) {
        return getCellsSquare(index / 3, index % 3);
    }

    private CellsSquare[] getLineSquares(int index) {
        return cellsSquares[index];
    }

    Cell[] getLine(int index) {

        CellsSquare[] lineSquare = getLineSquares(index / 3);
        Cell[] line = new Cell[9];

        List<Cell> cellsList = new LinkedList<>();

        for (int j = 0; j < 3; j++) {
            CellsSquare square = lineSquare[j];
            cellsList.addAll(Arrays.asList(square.getLine(index % 3)));
        }

        return cellsList.toArray(line);
    }

    Cell[] getColumn(int index) {

        CellsSquare[] columnSquare = getColumnSquares(index / 3);
        Cell[] column = new Cell[9];

        List<Cell> cellsList = new LinkedList<>();

        for (int i = 0; i < 3; i++) {
            CellsSquare square = columnSquare[i];
            cellsList.addAll(Arrays.asList(square.getColumn(index % 3)));
        }

        return cellsList.toArray(column);
    }

    Cell[] getCellsFromSquare(int index) {
        return getCellsSquare(index).getCellsArray();
    }

    static Set<Integer> getValuesToFindFromCells(Cell[] cells) {
        Integer[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        Set<Integer> valuesToFind = new HashSet<>(Arrays.asList(values));
        Set<Integer> knownValues = getKnownValuesFromCells(cells);
        valuesToFind.removeAll(knownValues);
        return valuesToFind;
    }

    static Set<Integer> getKnownValuesFromCells(Cell[] cells) {
        Set<Integer> knownValues = new HashSet<>();
        for (Cell cell : cells) {
            if (cell.isSet())
                knownValues.add(cell.getContent());
        }
        return knownValues;
    }

    private CellsSquare[] getColumnSquares(int index) {
        CellsSquare[] line = new CellsSquare[3];
        for (int i = 0; i < 3; i++) {
            line[i] = cellsSquares[i][index];
        }
        return line;
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
            setLine(i, values[i]);
        }
    }

    void setCells(int[] values) {
        if (values.length != 81)
            return;

        for (int i = 0; i < 9; i++) {
            int[] line = new int[9];
            System.arraycopy(values, i * 9, line, 0, 9);

            setLine(i, line);
        }
    }

    void setCells(CellsSquare[][] cellsSquares) {
        this.cellsSquares = cellsSquares;
    }

    void setLine(int index, int[] values) {
        if (values.length != 9)
            return;

        CellsSquare[] lineSquare = getLineSquares(index / 3);

        for (int j = 0; j < 3; j++) {

            int[] line = new int[3];
            System.arraycopy(values, j * 3, line, 0, 3);

            CellsSquare square = lineSquare[j];
            square.setLine(index % 3, line);
        }
    }

    void setColumn(int index, int[] values) {
        if (values.length != 9)
            return;

        CellsSquare[] columnSquare = getColumnSquares(index / 3);

        for (int i = 0; i < 3; i++) {

            int[] column = new int[3];
            System.arraycopy(values, i * 3, column, 0, 3);

            CellsSquare square = columnSquare[i];
            square.setColumn(index % 3, column);
        }
    }

    void setCellsSquare(int index, int[][] values) {
        if (values.length != 9 || values[0].length != 9)
            return;

        getCellsSquare(index).setCells(values);
    }

    void setCellsSquare(int index, int[] values) {
        if (values.length != 9) {
            return;
        }
        int[][] newVals = new int[3][3];
        for (int i = 0; i < 9; i++) {
            newVals[i / 3][i % 3] = values[i];
        }
        getCellsSquare(index).setCells(newVals);
    }

    void setCell(int line, int column, int val) {
        int squareLine = line / 3;
        int squareColumn = column / 3;
        getCellsSquare(squareLine, squareColumn).setCell(line, column, val);
    }

    void printGrid() {
        synchronized (System.out) {
            System.out.println("-------------------");

            for (int i = 0; i < 9; i++) {

                System.out.print("|");

                for (int j = 0; j < 9; j++) {

                    Cell cell = getLine(i)[j];

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
    }

    boolean isCompleted() {
        boolean completed = true;
        for (CellsSquare[] lineSquares : cellsSquares) {
            for (CellsSquare square : lineSquares) {
                if (!square.isCompleted()) {
                    completed = false;
                    break;
                }
            }
        }
        return completed;
    }
}
