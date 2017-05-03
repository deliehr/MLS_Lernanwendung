package Components;

/**
 * Representation of a normal cell.
 * Inherits from abstract class Cell.
 * Has additional attribute isHead:boolean.
 *
 * @author Dominik Liehr
 * @version 0.03
 */
public class StandardCell extends Cell {
    // region object variables
    private String cellValue;
    private boolean isWriteable;
    // endregion

    // region constructors
    public StandardCell() {
        this.setHead(false);
        this.setIdentifier("");
        this.setCellValue("");
        this.setWriteable(true);
    }
    /**
     * Constructor of class StandardCell.
     * Sets only internal variables with delivered parameters.
     * @param cellIdentifier The identifier of the cell.
     * @param cellValue The value or content of the cell (visible part).
     * @param isHead Marks cell as head (set colors, not editable).
     */
    public StandardCell(String cellIdentifier, String cellValue, boolean isHead) {
        this.setIdentifier(cellIdentifier);
        this.setCellValue(cellValue);
        this.setHead(isHead);
    }
    // endregion

    // region getter & setter

    public String getCellValue() {
        return cellValue;
    }

    public void setCellValue(String cellValue) {
        this.cellValue = cellValue;
    }

    public Boolean isWriteable() {
        return isWriteable;
    }

    public void setWriteable(Boolean writeable) {
        isWriteable = writeable;
    }

    // endregion
}
