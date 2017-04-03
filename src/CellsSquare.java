import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class CellsSquare {

    private Cell[][] cells = new Cell[3][3];

    CellsSquare() {
        cells = new Cell[3][3];
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                cells[i][j] = new Cell();
    }

    Cell[][] getCells() {
        return cells;
    }

    Cell[] getCellsArray() {
        Cell[] cellsArray = new Cell[9];

        List<Cell> cellsList = new LinkedList<>();

        for (int i = 0; i < 3; i++) {
            cellsList.addAll(Arrays.asList(cells[i]));
        }
        return cellsList.toArray(cellsArray);
    }

    Cell getCell(int line, int column) {
        return cells[line][column];
    }

    Cell[] getLine(int index) {
        return cells[index];
    }

    Cell[] getColumn(int index) {
        Cell[] line = new Cell[3];
        for (int i = 0; i < 3; i++) {
            line[i] = cells[i][index];
        }
        return line;
    }

    void setCells(int[][] values) {
        if (values.length != 3 || values[0].length != 3)
            return;

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                cells[i][j].setContent(values[i][j]);
    }

    void setCells(int[] values) {
        if (values.length != 9)
            return;

        for (int i = 0; i < 9; i++)
            cells[i / 3][i % 3].setContent(values[i]);
    }

    void setLine(int index, int[] values) {
        if (values.length != 3)
            return;

        for (int j = 0; j < 3; j++)
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
