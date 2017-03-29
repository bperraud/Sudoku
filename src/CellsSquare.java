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
        for (int i = 0; i < 3; i++)
            System.arraycopy(cells[i], 0, cellsArray, i * 3, 3);
        return cellsArray;
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

    void setCells(Cell[][] cells) {
        this.cells = cells;
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
