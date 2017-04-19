package Components;

/**
 * Representation of a cell in a drag'n'drop assessment.
 * Inherits from abstract class Cell.
 * DragCells do not contain any additional information, like normal cells (cellIdentifier, cellValue, isHead).
 * A drag cell is always an empty cell with no identifier or value.
 * A drag cell cannot be head cell (not editable).
 *
 * @author Dominik Liehr
 * @version 0.05
 */
public class DragCell extends Cell {
    // region object variables
    private String cellValue;
    private Boolean isWriteable = false;
    private String dragIdentifier = "";
    // endregion

    // region constructors
    public DragCell() {
        this.setCellValue("");
        this.setIdentifier("");
    }

    public DragCell(String identifier, String cellValue) {
        this.setIdentifier(identifier);
        this.setCellValue(cellValue);
    }
    // endregion

    // region getter & setter
    public String getCellValue() {
        return cellValue;
    }

    public void setCellValue(String cellValue) {
        this.cellValue = cellValue;
    }

    public Boolean getWriteable() {
        return this.isWriteable;
    }

    public String getDragIdentifier() {
        return dragIdentifier;
    }

    public void setDragIdentifier(String dragIdentifier) {
        this.dragIdentifier = dragIdentifier;
    }

    // endregion
}
