package Components;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for releated assessment groups
 *
 * @author Dominik Liehr
 * @version 0.01
 */
public class RelatedGroup {
    // region object variables
    private int id; // db id
    private String uuid;
    private int creationTimestamp;
    private String categoryTags;
    private String title;
    private Boolean shuffle;
    private List<String> itemUuids;
    // endregion

    // region object methods
    public RelatedGroup() {
        this.setUuid("");
        this.setCreationTimestamp(0);
        this.setCategoryTags("");
        this.setTitle("");
        this.setShuffle(false);
        this.setItemUuids(new ArrayList<String>());
    }
    // endregion

    // region getter & setter

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String id) {
        this.uuid = id;
    }

    public int getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(int creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getCategoryTags() {
        return categoryTags;
    }

    public void setCategoryTags(String categoryTags) {
        this.categoryTags = categoryTags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getShuffle() {
        return shuffle;
    }

    public void setShuffle(Boolean shuffle) {
        this.shuffle = shuffle;
    }

    public List<String> getItemUuids() {
        return itemUuids;
    }

    public void setItemUuids(List<String> itemIds) {
        this.itemUuids = itemIds;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // endregion
}
