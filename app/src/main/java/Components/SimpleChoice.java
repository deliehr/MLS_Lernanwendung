package Components;

/**
 * Class for an single choice item.
 * Contains an identifier, respective to a correct value (correct response).
 * Also contains a caption.
 * Only used in class ChoiceInteraction.
 *
 * @author Dominik Liehr
 * @version 0.01
 */
public class SimpleChoice {
    // region object variables
    private String identifier = "";
    private String caption = "";
    private String imageSource = "";
    // endregion

    // region constructors
    /**
     * Constructor for class SimpleChoice.
     * Assigns an identifier and a caption
     *
     * @author Dominik Liehr
     * @version 0.01
     */
    public SimpleChoice(String identifier, String caption, String imageSource) {
        this.setIdentifier(identifier);
        this.setCaption(caption);
        this.setImageSource(imageSource);
    }
    // endregion

    // region getter & setter
    public String getIdentifier() {
        return identifier;
    }

    public String getCaption() {
        return caption;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    // endregion
}