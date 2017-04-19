package Components;

import java.util.ArrayList;
import java.util.List;

/**
 * Class Table.
 * Multiple tables can belong to a single table assessment.
 * Each table contains a list of rows.
 */
public class Table {
    // region object variables
    private List<Row> rowList;
    // endregion

    // region constructors
    public Table() {
        this.rowList = new ArrayList<Row>();
    }

    public Table(List<Row> rowList) {
        this.setRowList(rowList);
    }
    // endregion

    // region object methods
    /**
     * Direct method for adding a row.
     * @param row Row to add.
     */
    public void addRow(Row row) throws Exception {
        if(row == null) {
            throw new Exception("Row object is null");
        }
        if(this.rowList == null) {
            this.rowList = new ArrayList<Row>();
        }
        this.rowList.add(row);
    }

    /**
     * Direct methods for adding multiple rows.
     * For each row, the internal method addRow(in row: Row) will called.
     *
     * @param rows Multiple objects of type Row.
     * @throws Exception Thrown, if one row object is null.
     */
    public void addRows(Row... rows) throws Exception {
        for(Row row:rows) {
            this.addRow(row);
        }
    }
    // endregion

    // region getter & setter

    public List<Row> getRowList() {
        return rowList;
    }

    public void setRowList(List<Row> rowList) {
        this.rowList = rowList;
    }

    // endregion

    // region nested enum Type
    public enum Type {
        Table,
        DragAndDrop,
        Support
    }
    // endregion
}