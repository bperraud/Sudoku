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

        for (int j = 0; j < 3; j++) {
            CellsSquare square = lineSquare[j];
            System.arraycopy(square.getLine(index % 3), 0, line, 3 * j, 3);
        }

        return line;
    }

    Cell[] getColumn(int index) {

        CellsSquare[] columnSquare = getColumnSquares(index / 3);
        Cell[] column = new Cell[9];

        for (int i = 0; i < 3; i++) {
            CellsSquare square = columnSquare[i];
            System.arraycopy(square.getColumn(index % 3), 0, column, 3 * i, 3);
        }

        return column;
    }

    Cell[] getCellsFromSquare(int index) {
        return getCellsSquare(index).getCellsArray();
    }

    private CellsSquare[] getColumnSquares(int index) {
        CellsSquare[] line = new CellsSquare[3];
        for (int i = 0; i < 3; i++) {
            line[i] = cellsSquares[i][index];
        }
        return line;
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
    
    void setLine(int index, Cell[] newCells) {
        if (newCells.length != 9)
            return;

        CellsSquare[] lineSquare = getLineSquares(index / 3);

        for (int j = 0; j < 3; j++) {

            Cell[] line = new Cell[3];
            System.arraycopy(newCells, j * 3, line, 0, 3);

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
    
    void setColumn(int index, Cell[] newCells) {
        if (newCells.length != 9)
            return;

        CellsSquare[] columnSquare = getColumnSquares(index / 3);

        for (int i = 0; i < 3; i++) {

            Cell[] column = new Cell[3];
            System.arraycopy(newCells, i * 3, column, 0, 3);

            CellsSquare square = columnSquare[i];
            square.setColumn(index % 3, column);
        }
    }
    
    void setCellsSquare(int index, Cell[] cells){
    	if (cells.length != 9){
    		return;
    	}
    	Cell[][] newCells = new Cell[3][3];
    	for (int i = 0; i < 9; i++){
    		newCells[i / 3][i % 3] = cells[i];
    	}
    	getCellsSquare(index).setCells(newCells);
    }

    void setCell(int line, int column, int val) {
        int squareLine = line / 3;
        int squareColumn = column / 3;
        getCellsSquare(squareLine, squareColumn).setCell(line, column, val);
    }

    void printGrid() {
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
