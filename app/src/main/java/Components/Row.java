package Components;

import java.util.ArrayList;
import java.util.List;

/**
 * Class Row.
 * Is part of a table in a table assessment or drag'n'drop assessment.
 *
 * @author Dominik Liehr
 * @version 0.02
 */
public class Row {
    // region object variables
    private List<Cell> cellList;
    // endregion

    // region constructors

    /**
     * Constructor 1 of class Row.
     * Initialises internal cell list:
     */
    public Row() {
        this.cellList = new ArrayList<Cell>();
    }

    /**
     * Contructor 2 of class Row.
     * Sets the internal list to the delivered List cellList.
     * @param cellList List with cells.
     */
    public Row(List<Cell> cellList) {
        this.setCellList(cellList);
    }
    // endregion

    // region object methods

    /**
     * Adds a single (non-null) cell to the internal cell list.
     * Does not create a new cell.
     * Checks first, if list != null (creates new list).
     *
     * @param cell A StandardCell or DragCell object.
     * @throws Exception Throws an exception, if parameter cell is an empty object.
     */
    public void addCell(Cell cell) {
        if(this.cellList == null) {
            this.cellList = new ArrayList<Cell>();
        }

        this.cellList.add(cell);
    }

    /**
     * Method for adding multiple cells at once.
     * For each cell, the internal addCell(in cell:Cell):void will be called.
     *
     * @param cells Multiple objects of type Cell.
     * @throws Exception Is thrown, if one cell object is null.
     */
    public void addCells(Cell... cells) {
        for(Cell cell:cells) {
            this.addCell(cell);
        }
    }
    // endregion

    // region getter & setter

    public List<Cell> getCellList() {
        return cellList;
    }

    public void setCellList(List<Cell> cellList) {
        this.cellList = cellList;
    }



    // endregion
}
