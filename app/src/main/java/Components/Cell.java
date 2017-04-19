package Components;

/**
 * Abstract class Cell.
 * Gives a foundation for NormalCells (in table assessments) and DragCells (in dragndrop assessments)
 *
 * @author Dominik Liehr
 * @version 0.03
 */
public abstract class Cell {
    // region object variables
    private int id = -1;    // database id
    private int colspan = 1;
    private String identifier = "";
    private boolean isHead = false;
    // endregion

    // region getter & setter

    public int getColspan() {
        return colspan;
    }

    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isHead() {
        return isHead;
    }

    public void setHead(boolean head) {
        isHead = head;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // endregion
}