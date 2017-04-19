package Comprehensive;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import Components.*;

/**
 * Class for import assessments, related groups and support into objects, stored in lists
 *
 * @author Dominik Liehr
 * @version 0.05
 */
public class Import_Xml2Obj {
    private Boolean lastImportSuccessful = false;
    private String importDirectory = "";
    private List<Assessment> importedAssessments;
    private List<RelatedGroup> relatedGroups;
    private List<Support> supports;

    public Import_Xml2Obj(String impD) {
        this.lastImportSuccessful = true;
        this.importDirectory = impD;
        this.importedAssessments = new ArrayList<Assessment>();
    }

    // region import start, info

    public Boolean getLastImportSuccessful() {
        return lastImportSuccessful;
    }

    public void startImportProgress() {
        // reset list
        this.importedAssessments.clear();

        // import two main elements
        Boolean standard = this.importStandard();
        Boolean extended = this.importExtended();

        if(standard && extended) {
            this.lastImportSuccessful = true;

            Boolean related = this.importRelated();
            Boolean support = this.importSupport();
        } else {
            this.lastImportSuccessful = false;
        }
    }

    public List<Assessment> getImportedAssessments() {
        if(this.getLastImportSuccessful()) {
            return this.importedAssessments;
        }

        return null;
    }

    public List<RelatedGroup> getImportedRelatedGroups() {
        return this.relatedGroups;
    }

    public List<Support> getImportedSupport() {
        return this.supports;
    }
    // endregion

    // region import assessments

    private Boolean importStandard() {
        // file standard
        File standardXmlFile = new File(this.importDirectory + "/assessments_standard.xml");
        List<Assessment> assessmentsToImport = new ArrayList<Assessment>();

        // file exists?
        if(standardXmlFile.exists()) {
            // exist, read
            try {
                // factory
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(standardXmlFile);

                // document
                doc.getDocumentElement().normalize();

                // iterate through all assessment items
                NodeList assessmentItems = doc.getElementsByTagName("assessmentItem");
                for(int i=0;i < assessmentItems.getLength();i++) {
                    // check type
                    NamedNodeMap assessmentItemAttributes = assessmentItems.item(i).getAttributes();

                    // switch type
                    String assessmentType = assessmentItemAttributes.getNamedItem("identifier").getNodeValue().toString();
                    switch (assessmentType) {
                        case "choice": assessmentsToImport.add(this.readSingleChoiceAssessment((Element) assessmentItems.item(i), assessmentItemAttributes)); break;
                        case "choiceMultiple": assessmentsToImport.add(this.readMultipleChoiceAssessment((Element) assessmentItems.item(i), assessmentItemAttributes)); break;
                        case "positionObjects": assessmentsToImport.add(this.readHotspotAssessment((Element) assessmentItems.item(i), assessmentItemAttributes)); break;
                        default: return false;
                    }
                }

                for(Assessment a:assessmentsToImport) {
                    this.importedAssessments.add(a);
                }

                return true;
            } catch (javax.xml.parsers.ParserConfigurationException pce) {
                Log.e("Error", "Error while importing standard assessments: " + pce.getMessage());
            } catch (Exception e) {
                Log.e("Error", "Error while importing standard assessments: " + e.getMessage());
            }
        }

        return false;
    }

    private Boolean importExtended() {
        // file standard
        File extendedXmlFile = new File(this.importDirectory + "/assessments_extended.xml");
        List<Assessment> assessmentsToImport = new ArrayList<Assessment>();

        // file exists?
        if(extendedXmlFile.exists()) {
            // exist, read
            try {
                // factory
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(extendedXmlFile);

                // document
                doc.getDocumentElement().normalize();

                // iterate through all assessment items
                NodeList assessmentItems = doc.getElementsByTagName("assessmentItem");
                for(int i=0;i < assessmentItems.getLength();i++) {
                    // check type
                    NamedNodeMap assessmentItemAttributes = assessmentItems.item(i).getAttributes();

                    // switch type
                    String assessmentType = assessmentItemAttributes.getNamedItem("identifier").getNodeValue().toString();
                    switch (assessmentType) {
                        case "table":  assessmentsToImport.add(this.readTableAssessment((Element) assessmentItems.item(i), assessmentItemAttributes)); break;
                        case "dragndropTable": assessmentsToImport.add(this.readDragAndDropAssessment((Element) assessmentItems.item(i), assessmentItemAttributes)); break;
                        default: return false;
                    }
                }

                for(Assessment a:assessmentsToImport) {
                    this.importedAssessments.add(a);
                }

                return true;
            } catch (javax.xml.parsers.ParserConfigurationException pce) {
                Log.e("Error", "Error while importing extended assessments: " + pce.getMessage());
            } catch (Exception e) {
                Log.e("Error", "Error while importing extended assessments: " + e.getMessage());
            }
        }

        return false;
    }

    // endregion

    // region methods reading other stuff

    private Boolean importRelated() {
        // list
        if(this.relatedGroups == null) {
            this.relatedGroups = new ArrayList<RelatedGroup>();
        } else {
            this.relatedGroups.clear();
        }

        // file related
        File relatedXmlFile = new File(this.importDirectory + "/assessments_related.xml");

        // file exists?
        if(relatedXmlFile.exists()) {
            // exist, read
            try {
                // factory
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(relatedXmlFile);

                // document
                doc.getDocumentElement().normalize();

                // iterate through all assessment items
                Element rootElement = (Element) doc.getElementsByTagName("relatedAssessments").item(0);
                NodeList groupElements = rootElement.getElementsByTagName("group");
                for(int i=0;i < groupElements.getLength();i++) {
                    Element currentXmlGroup = (Element) groupElements.item(i);
                    RelatedGroup group = new RelatedGroup();

                    // related group information
                    group.setUuid(currentXmlGroup.getAttribute("id"));
                    group.setCreationTimestamp(Integer.valueOf(currentXmlGroup.getAttribute("creationTimestamp")));
                    group.setCategoryTags(currentXmlGroup.getAttribute("categoryTags"));
                    group.setTitle(currentXmlGroup.getAttribute("title"));
                    group.setShuffle(Boolean.valueOf(currentXmlGroup.getAttribute("shuffle")));

                    // items of group
                    NodeList groupItems = currentXmlGroup.getElementsByTagName("item");
                    for(int j=0;j < groupItems.getLength();j++) {
                        Element currentItem = (Element) groupItems.item(j);
                        group.getItemUuids().add(this.readElementTextContent(currentItem.getTextContent()));
                    }

                    // add related group to list
                    this.relatedGroups.add(group);
                }

                return true;
            } catch (javax.xml.parsers.ParserConfigurationException pce) {
                Log.e("Error", "Error while importing a related group: " + pce.getMessage());
            } catch (Exception e) {
                Log.e("Error", "Error while importing a related group: " + e.getMessage());
            }
        }

        return false;
    }

    private Boolean importSupport() {
        // list
        if(this.supports == null) {
            this.supports = new ArrayList<Support>();
        } else {
            this.supports.clear();
        }

        // file related
        File supportsXmlFile = new File(this.importDirectory + "/assessment_support.xml");

        // file exists?
        if(supportsXmlFile.exists()) {
            // exist, read
            try {
                // factory
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(supportsXmlFile);

                // document
                doc.getDocumentElement().normalize();

                // iterate through all assessment items
                NodeList supportElements = doc.getElementsByTagName("support");
                for(int i=0;i < supportElements.getLength();i++) {
                    Element currentXmlSupport = (Element) supportElements.item(i);
                    String supportType = currentXmlSupport.getAttribute("identifier");
                    Support support;

                    switch (supportType) {
                        case "textbox":
                            support = new TextboxSupport(currentXmlSupport.getAttribute("id"), this.readElementTextContent(currentXmlSupport.getElementsByTagName("textbox").item(0).getTextContent()));
                            break;

                        case "image":
                            String imagePrompt = this.readElementTextContent(currentXmlSupport.getElementsByTagName("prompt").item(0).getTextContent());
                            String imageSource = ((Element) currentXmlSupport.getElementsByTagName("img").item(0)).getAttribute("src");
                            support = new MediaSupport(currentXmlSupport.getAttribute("id"), imageSource, imagePrompt, supportType);
                            break;

                        case "video":
                            String videoPrompt = this.readElementTextContent(currentXmlSupport.getElementsByTagName("prompt").item(0).getTextContent());
                            String videoSource = ((Element) currentXmlSupport.getElementsByTagName("vid").item(0)).getAttribute("src");
                            support = new MediaSupport(currentXmlSupport.getAttribute("id"), videoSource, videoPrompt, supportType);
                            break;

                        case "selection":
                            List<String> selectionList = new ArrayList<String>();
                            Element elementSelection = (Element) currentXmlSupport.getElementsByTagName("selection").item(0);
                            NodeList selections = elementSelection.getElementsByTagName("select");
                            for(int j=0;j < selections.getLength();j++) {
                                selectionList.add(this.readElementTextContent(selections.item(j).getTextContent()));
                            }

                            String selectionPrompt = this.readElementTextContent(currentXmlSupport.getElementsByTagName("prompt").item(0).getTextContent());
                            support = new SelectionSupport(currentXmlSupport.getAttribute("id"), selectionPrompt, selectionList);
                            break;

                        default:
                            // table support
                            Table table = this.readTable(currentXmlSupport, Table.Type.Support);
                            String tablePrompt = this.readElementTextContent(currentXmlSupport.getElementsByTagName("prompt").item(0).getTextContent());
                            support = new TableSupport(currentXmlSupport.getAttribute("id"), tablePrompt, table);
                            break;
                    }

                    // add main data
                    if(support != null) {
                        support.setUuid(currentXmlSupport.getAttribute("id"));
                        support.setAssessmentUuid(currentXmlSupport.getAttribute("assessmentId"));
                        support.setCreationTimestamp(Integer.valueOf(currentXmlSupport.getAttribute("creationTimestamp")));
                        support.setIdentifier(currentXmlSupport.getAttribute("identifier"));
                    }

                    // add related group to list
                    this.supports.add(support);
                }

                return true;
            } catch (javax.xml.parsers.ParserConfigurationException pce) {
                Log.e("Error", "Error while importing a support element: " + pce.getMessage());
            } catch (Exception e) {
                Log.e("Error", "Error while importing a support element: " + e.getMessage());
            }
        }

        return false;
    }

    // endregion

    // region methods reading assessments

    private Assessment readSingleChoiceAssessment(Element assessmentItem, NamedNodeMap assessmentItemAttributes) {
        // region shuffle attribute
        Element elementChoiceInteraction = (Element) assessmentItem.getElementsByTagName("choiceInteraction").item(0);
        String shuffle = elementChoiceInteraction.getAttribute("shuffle");
        // endregion

        // region main assessment data
        SingleChoiceAssessment assessment = new SingleChoiceAssessment(Boolean.valueOf(shuffle));
        assessment.setUuid(assessmentItemAttributes.getNamedItem("id").getNodeValue().toString());
        assessment.setCreationTimestamp(Integer.valueOf(assessmentItemAttributes.getNamedItem("creationTimestamp").getNodeValue().toString()));
        assessment.setCategoryTags(assessmentItemAttributes.getNamedItem("categoryTags").getNodeValue().toString());
        assessment.setTitle(assessmentItemAttributes.getNamedItem("title").getNodeValue().toString());
        assessment.setAdaptive(Boolean.valueOf(assessmentItemAttributes.getNamedItem("adaptive").getNodeValue().toString()));
        assessment.setTimeDependent(Boolean.valueOf(assessmentItemAttributes.getNamedItem("timeDependent").getNodeValue().toString()));
        // endregion

        // region response declaration
        Element elementResponseDeclaration = (Element) assessmentItem.getElementsByTagName("responseDeclaration").item(0);
        Element elementCorrectResponse = (Element) elementResponseDeclaration.getElementsByTagName("correctResponse").item(0);
        Element elementValue = (Element) elementCorrectResponse.getElementsByTagName("value").item(0);
        String value = elementValue.getTextContent();

        // correct response value
        assessment.setCorrectValueIdentifier(value);
        // endregion

        // region prompt
        assessment.setPrompt(this.readPrompt(elementChoiceInteraction));
        // endregion

        // region simple choice list
        // choice list
        NodeList simpleChoices = elementChoiceInteraction.getElementsByTagName("simpleChoice");
        for(int j=0;j < simpleChoices.getLength();j++) {
            // switch type
            Element currentElement = (Element) simpleChoices.item(j);
            String identifier = currentElement.getAttribute("identifier");
            String imageSource = null;

            if(currentElement.hasAttribute("imgSrc")) {
                imageSource = currentElement.getAttribute("imgSrc");
            }

            String caption = "";
            if(currentElement.hasChildNodes()) {
                caption = this.readElementTextContent(currentElement.getChildNodes().item(0).getTextContent());
            }

            assessment.getSimpleChoiceList().add(new SimpleChoice(identifier, caption, imageSource));
        }

        // endregion

        // region item body content
        assessment.setItemBodyParagraphList(this.readItemBodyParagraphs(assessmentItem));
        // endregion

        return assessment;
    }

    private Assessment readMultipleChoiceAssessment(Element assessmentItem, NamedNodeMap assessmentItemAttributes) throws Exception {
        // region shuffle attribute
        Element elementChoiceInteraction = (Element) assessmentItem.getElementsByTagName("choiceInteraction").item(0);
        String shuffle = elementChoiceInteraction.getAttribute("shuffle");
        String maxChoices = elementChoiceInteraction.getAttribute("maxChoices");
        // endregion

        // region main assessment data
        MultipleChoiceAssessment assessment = new MultipleChoiceAssessment();
        assessment.setUuid(assessmentItemAttributes.getNamedItem("id").getNodeValue().toString());
        assessment.setCreationTimestamp(Integer.valueOf(assessmentItemAttributes.getNamedItem("creationTimestamp").getNodeValue().toString()));
        assessment.setCategoryTags(assessmentItemAttributes.getNamedItem("categoryTags").getNodeValue().toString());
        assessment.setTitle(assessmentItemAttributes.getNamedItem("title").getNodeValue().toString());
        assessment.setAdaptive(Boolean.valueOf(assessmentItemAttributes.getNamedItem("adaptive").getNodeValue().toString()));
        assessment.setTimeDependent(Boolean.valueOf(assessmentItemAttributes.getNamedItem("timeDependent").getNodeValue().toString()));
        assessment.setMaxChoices(Byte.valueOf(maxChoices));
        assessment.setShuffleChoices(Boolean.valueOf(shuffle));
        // endregion

        // region response declaration
        Element elementResponseDeclaration = (Element) assessmentItem.getElementsByTagName("responseDeclaration").item(0);

        // correct response
        Element elementCorrectResponse = (Element) elementResponseDeclaration.getElementsByTagName("correctResponse").item(0);
        NodeList correctvalues = elementCorrectResponse.getElementsByTagName("value");
        for(int j=0;j < correctvalues.getLength();j++) {
            assessment.getCorrectValueList().add(this.readElementTextContent(correctvalues.item(j).getTextContent()));
        }

        // mapping
        Element mappingElement = (Element) elementResponseDeclaration.getElementsByTagName("mapping").item(0);
        Byte lowerBound = Byte.valueOf(mappingElement.getAttribute("lowerBound"));
        Byte upperBound = Byte.valueOf(mappingElement.getAttribute("upperBound"));
        Byte defaultValue = Byte.valueOf(mappingElement.getAttribute("defaultValue"));

        MultipleChoiceAssessment.KeyValueMappingGroup mappingGroup = new MultipleChoiceAssessment.KeyValueMappingGroup(lowerBound, upperBound, defaultValue);

        NodeList mapEntries = mappingElement.getElementsByTagName("mapEntry");
        for(int j=0;j < mapEntries.getLength();j++) {
            Element currentMapEntry = (Element) mapEntries.item(j);

            mappingGroup.addKeyValueMapping(new MultipleChoiceAssessment.KeyValueMappingGroup.KeyValueMapping(currentMapEntry.getAttribute("mapKey"), Byte.valueOf(currentMapEntry.getAttribute("mappedValue"))));
        }

        assessment.setKeyValueMappingGroup(mappingGroup);
        // endregion

        // region simple choice list
        // region prompt
        assessment.setPrompt(this.readPrompt(elementChoiceInteraction));
        // endregion

        // choice list
        NodeList simpleChoices = elementChoiceInteraction.getElementsByTagName("simpleChoice");
        for(int j=0;j < simpleChoices.getLength();j++) {
            // switch type
            Element currentElement = (Element) simpleChoices.item(j);
            String identifier = currentElement.getAttribute("identifier");
            String imageSource = null;

            if(currentElement.hasAttribute("imgSrc")) {
                imageSource = currentElement.getAttribute("imgSrc");
            }

            String caption = "";
            if(currentElement.hasChildNodes()) {
                caption = this.readElementTextContent(currentElement.getChildNodes().item(0).getTextContent());
            }

            assessment.getSimpleChoiceList().add(new SimpleChoice(identifier, caption, imageSource));
        }

        // max choices
        int maxChoicesInt = Integer.valueOf(maxChoices);
        if(maxChoicesInt <= 0) {
            assessment.setMaxChoices(Byte.valueOf(String.valueOf(simpleChoices.getLength())));
        } else {
            assessment.setMaxChoices(Byte.valueOf(String.valueOf(maxChoices)));
        }
        // endregion

        // region item body content
        assessment.setItemBodyParagraphList(this.readItemBodyParagraphs(assessmentItem));
        // endregion

        return assessment;
    }

    private Assessment readHotspotAssessment(Element assessmentItem, NamedNodeMap assessmentItemAttributes) throws Exception {
        // region main assessment data
        HotspotAssessment assessment = new HotspotAssessment();
        assessment.setUuid(assessmentItemAttributes.getNamedItem("id").getNodeValue().toString());
        assessment.setCreationTimestamp(Integer.valueOf(assessmentItemAttributes.getNamedItem("creationTimestamp").getNodeValue().toString()));
        assessment.setCategoryTags(assessmentItemAttributes.getNamedItem("categoryTags").getNodeValue().toString());
        assessment.setTitle(assessmentItemAttributes.getNamedItem("title").getNodeValue().toString());
        assessment.setAdaptive(Boolean.valueOf(assessmentItemAttributes.getNamedItem("adaptive").getNodeValue().toString()));
        assessment.setTimeDependent(Boolean.valueOf(assessmentItemAttributes.getNamedItem("timeDependent").getNodeValue().toString()));
        // endregion

        // region response declaration
        Element elementResponseDeclaration = (Element) assessmentItem.getElementsByTagName("responseDeclaration").item(0);
        Element correctResponse = (Element) elementResponseDeclaration.getElementsByTagName("correctResponse").item(0);
        NodeList crValues = correctResponse.getElementsByTagName("value");
        for(int j=0;j < crValues.getLength();j++) {
            assessment.getCorrectValueList().add(readElementTextContent(crValues.item(j).getTextContent()));
        }
        // endregion

        // region area mapping
        Element elementAreaMapping = (Element) elementResponseDeclaration.getElementsByTagName("areaMapping").item(0);
        NodeList amEntries = elementAreaMapping.getElementsByTagName("areaMapEntry");
        for(int j=0;j < amEntries.getLength();j++) {
            // current xml map entry
            Element currentAreaMapEntry = (Element) amEntries.item(j);

            // hotspot object map entries
            HotspotAssessment.AreaMapEntry mapEntry = new HotspotAssessment.AreaMapEntry();
            mapEntry.setShape(currentAreaMapEntry.getAttribute("shape"));
            mapEntry.setCoords(currentAreaMapEntry.getAttribute("coords"));
            mapEntry.setMappedValue(Integer.valueOf(currentAreaMapEntry.getAttribute("mappedValue")));

            // assign to assessment
            assessment.getAreaMapEntryList().add(mapEntry);
        }
        assessment.setAreaMappingDefaultValue(Integer.valueOf(elementAreaMapping.getAttribute("defaultValue")));
        // endregion

        // region position object stage
        Element elementPositionObjectStage = (Element) assessmentItem.getElementsByTagName("positionObjectStage").item(0);
        Element elementObjectOuter = (Element) elementPositionObjectStage.getElementsByTagName("object").item(0);

        // outer object
        HotspotAssessment.Object outerObject = new HotspotAssessment.Object();
        outerObject.setType(elementObjectOuter.getAttribute("type"));
        outerObject.setData(elementObjectOuter.getAttribute("data"));
        outerObject.setWidth(Integer.valueOf(elementObjectOuter.getAttribute("width")));
        outerObject.setHeight(Integer.valueOf(elementObjectOuter.getAttribute("height")));

        // assign outer object to assessment
        assessment.getPositionObjectInteraction().setOuterObject(outerObject);

        Element elementPositionObjectInteraction = (Element) elementPositionObjectStage.getElementsByTagName("positionObjectInteraction").item(0);
        assessment.setMaxChoices(Byte.valueOf(elementPositionObjectInteraction.getAttribute("maxChoices")));

        Element elementObjectInner = (Element) elementPositionObjectInteraction.getElementsByTagName("object").item(0);

        // inner object
        HotspotAssessment.Object innerObject = new HotspotAssessment.Object();
        innerObject.setType(elementObjectInner.getAttribute("type"));
        innerObject.setData(elementObjectInner.getAttribute("data"));
        innerObject.setWidth(Integer.valueOf(elementObjectInner.getAttribute("width")));
        innerObject.setHeight(Integer.valueOf(elementObjectInner.getAttribute("height")));

        // assign inner object to assessment
        assessment.getPositionObjectInteraction().setInnerObject(innerObject);

        // endregion

        // region item body content
        assessment.setItemBodyParagraphList(this.readItemBodyParagraphs(assessmentItem));
        // endregion

        return assessment;
    }

    private Assessment readTableAssessment(Element assessmentItem, NamedNodeMap assessmentItemAttributes) throws Exception {
        // region main assessment data
        TableAssessment assessment = new TableAssessment();
        assessment.setUuid(assessmentItemAttributes.getNamedItem("id").getNodeValue().toString());
        assessment.setCreationTimestamp(Integer.valueOf(assessmentItemAttributes.getNamedItem("creationTimestamp").getNodeValue().toString()));
        assessment.setCategoryTags(assessmentItemAttributes.getNamedItem("categoryTags").getNodeValue().toString());
        assessment.setTitle(assessmentItemAttributes.getNamedItem("title").getNodeValue().toString());
        assessment.setAdaptive(Boolean.valueOf(assessmentItemAttributes.getNamedItem("adaptive").getNodeValue().toString()));
        assessment.setTimeDependent(Boolean.valueOf(assessmentItemAttributes.getNamedItem("timeDependent").getNodeValue().toString()));
        // endregion

        // region response declaration
        Element elementResponseDeclaration = (Element) assessmentItem.getElementsByTagName("responseDeclaration").item(0);
        Element elementCorrectResponse = (Element) elementResponseDeclaration.getElementsByTagName("correctResponse").item(0);
        NodeList crValues = elementCorrectResponse.getElementsByTagName("value");

        for(int j=0;j < crValues.getLength();j++) {
            Element currentValue = (Element) crValues.item(j);
            assessment.getValueList().add(new TableAssessment.Value(currentValue.getAttribute("cellIdentifier"), this.readElementTextContent(currentValue.getTextContent())));
        }
        // endregion

        // region item body content
        assessment.setItemBodyParagraphList(this.readItemBodyParagraphs(assessmentItem));
        // endregion

        // region table interaction
        Element elementTableInteraction = (Element) assessmentItem.getElementsByTagName("tableInteraction").item(0);
        // endregion

        // region prompt
        assessment.setPrompt(this.readPrompt(elementTableInteraction));
        // endregion

        //  region iterate each table
        NodeList xmlTables = elementTableInteraction.getElementsByTagName("table");
        for(int j=0;j < xmlTables.getLength();j++) {
            Element currentXmlTable = (Element) xmlTables.item(j);
            Table tmpTable = this.readTable(currentXmlTable, Table.Type.Table);

            // add table to table lists
            assessment.addTable(tmpTable);
        }
        // endregion

        return assessment;
    }

    private Assessment readDragAndDropAssessment(Element assessmentItem, NamedNodeMap assessmentItemAttributes) throws Exception {
        // region drag mode
        Element elementDragInteraction = (Element) assessmentItem.getElementsByTagName("dragInteraction").item(0);
        String dragMode = elementDragInteraction.getAttribute("mode");

        DragAssessment assessment;
        if(dragMode.equals("column")) {
            assessment = new DragAssessment(DragAssessment.DragMode.COL);
        } else {
            assessment = new DragAssessment(DragAssessment.DragMode.ROW);
        }
        // endregion

        // region main assessment data
        assessment.setUuid(assessmentItemAttributes.getNamedItem("id").getNodeValue().toString());
        assessment.setCreationTimestamp(Integer.valueOf(assessmentItemAttributes.getNamedItem("creationTimestamp").getNodeValue().toString()));
        assessment.setCategoryTags(assessmentItemAttributes.getNamedItem("categoryTags").getNodeValue().toString());
        assessment.setTitle(assessmentItemAttributes.getNamedItem("title").getNodeValue().toString());
        assessment.setAdaptive(Boolean.valueOf(assessmentItemAttributes.getNamedItem("adaptive").getNodeValue().toString()));
        assessment.setTimeDependent(Boolean.valueOf(assessmentItemAttributes.getNamedItem("timeDependent").getNodeValue().toString()));
        // endregion

        // region prompt
        assessment.setPrompt(this.readPrompt(elementDragInteraction));
        // endregion

        // region item body content
        assessment.setItemBodyParagraphList(this.readItemBodyParagraphs(assessmentItem));
        // endregion

        // region table interaction
        // iterate each table
        NodeList xmlTables = elementDragInteraction.getElementsByTagName("table");
        for(int j=0;j < xmlTables.getLength();j++) {
            Element currentXmlTable = (Element) xmlTables.item(j);
            Table tmpTable = this.readTable(currentXmlTable, Table.Type.DragAndDrop);

            // add table to table lists
            assessment.addTable(tmpTable);
        }
        // endregion

        // region dragitems
        Element elementDragItems = (Element) elementDragInteraction.getElementsByTagName("dragItems").item(0);
        NodeList dragItems = elementDragItems.getElementsByTagName("dragItem");
        for(int j=0;j < dragItems.getLength();j++) {
            Element currentDragItem = (Element) dragItems.item(j);

            switch (dragMode) {
                case "column": assessment.getDragItemList().add(new DragAssessment.DragItem(currentDragItem.getAttribute("columnIdentifier"), this.readElementTextContent(currentDragItem.getTextContent()))); break;
                case "row": assessment.getDragItemList().add(new DragAssessment.DragItem(currentDragItem.getAttribute("rowIdentifier"), this.readElementTextContent(currentDragItem.getTextContent()))); break;
                default: break;
            }
        }

        // endregion

        return assessment;
    }



    // endregion

    // region helper methods

    /**
     * Reads a single table element from the xml input.
     * Distinguish between table types
     * @param currentXmlTable The table xml element
     * @param type The Type of the table, for distinguish
     * @return
     * @throws Exception
     */
    private Table readTable(Element currentXmlTable, Table.Type type) throws Exception {
        Table tmpTable = new Table();
        String tableType = type.toString();

        // iterate each row
        NodeList xmlRows = currentXmlTable.getElementsByTagName("row");
        for(int k=0;k < xmlRows.getLength();k++) {
            Element currentXmlRow = (Element) xmlRows.item(k);
            Row tmpRow = new Row();

            // iterate each cell
            NodeList xmlCells = currentXmlRow.getElementsByTagName("cell");
            for(int l=0;l < xmlCells.getLength();l++) {
                // current xml element
                Element currentXmlCell = (Element) xmlCells.item(l);

                // dtd:
                // <!ATTLIST cell
                // colspan CDATA #IMPLIED
                // cellIdentifier CDATA #IMPLIED
                // writeable (true|false) "true"
                // head (true|false) "false"
                // columnIdentifier CDATA #IMPLIED
                // rowIdentifier CDATA #IMPLIED
                // >

                // distinguish between assessment types
                switch (tableType) {
                    case "Table":
                        // region table
                        // standard cell
                        StandardCell standardCell = new StandardCell();

                        // colspan
                        try {
                            if(currentXmlCell.hasAttribute("colspan")) {
                                String attribute = currentXmlCell.getAttribute("colspan");
                                Integer int_attribute = Integer.parseInt(attribute);

                                //standardCell.setColspan(Integer.valueOf(currentXmlCell.getAttribute("colspan")));
                                standardCell.setColspan(int_attribute);
                            }
                        } catch (Exception e) {
                            // no number?
                            standardCell.setColspan(1);
                        }


                        // cell identifier
                        if(currentXmlCell.hasAttribute("cellIdentifier")) {
                            standardCell.setIdentifier(currentXmlCell.getAttribute("cellIdentifier"));
                        } else {
                            standardCell.setIdentifier("");
                        }

                        // writeable
                        if(currentXmlCell.hasAttribute("writeable")) {
                            if(standardCell.isHead()) {
                                // is head, => cell not writeable
                                standardCell.setWriteable(false);
                            } else {
                                standardCell.setWriteable(Boolean.valueOf(currentXmlCell.getAttribute("writeable")));
                            }
                        } else {
                            if(standardCell.isHead()) {
                                // is head, => cell not writeable
                                standardCell.setWriteable(false);
                            } else {
                                standardCell.setWriteable(true);
                            }
                        }

                        // head
                        if(currentXmlCell.hasAttribute("head")) {
                            standardCell.setHead(Boolean.valueOf(currentXmlCell.getAttribute("head")));
                        } else {
                            standardCell.setHead(false);
                        }

                        // cell value
                        if(currentXmlCell.hasChildNodes()) {
                            if(currentXmlCell.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
                                standardCell.setCellValue(this.readElementTextContent(currentXmlCell.getChildNodes().item(0).getTextContent()));
                            }
                        }

                        // add cell to row
                        tmpRow.addCell(standardCell);

                        break;
                        // endregion
                    case "Support":
                        // region support
                        // standard cell
                        StandardCell supportCell = new StandardCell();

                        // cell identifier
                        supportCell.setIdentifier("");

                        // head
                        if(currentXmlCell.hasAttribute("head")) {
                            supportCell.setHead(Boolean.valueOf(currentXmlCell.getAttribute("head")));
                        } else {
                            supportCell.setHead(false);
                        }

                        // writeable: every cell in support mode is not writeable
                        supportCell.setWriteable(false);

                        // colspan
                        try {
                            if(currentXmlCell.hasAttribute("colspan")) {
                                supportCell.setColspan(Integer.valueOf(currentXmlCell.getAttribute("colspan")));
                            }
                        } catch (Exception e) {
                            // no number?
                            supportCell.setColspan(1);
                        }

                        // cell value
                        if(currentXmlCell.hasChildNodes()) {
                            if(currentXmlCell.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
                                supportCell.setCellValue(this.readElementTextContent(currentXmlCell.getChildNodes().item(0).getTextContent()));
                            }
                        }

                        // add cell to row
                        tmpRow.addCell(supportCell);
                        break;
                        // endregion
                    case "DragAndDrop":
                        // region drag and drop
                        // drag cell
                        DragCell dragCell = new DragCell();

                        // cell identifier: on drag n drop interactions, especially on column or row mode, cells do not have an identifier
                        dragCell.setIdentifier("");

                        // head
                        if(currentXmlCell.hasAttribute("head")) {
                            dragCell.setHead(Boolean.valueOf(currentXmlCell.getAttribute("head")));
                        } else {
                            dragCell.setHead(false);
                        }

                        // writeable: a drag cell is never writeable

                        // colspan
                        try {
                            if(currentXmlCell.hasAttribute("colspan")) {
                                dragCell.setColspan(Integer.valueOf(currentXmlCell.getAttribute("colspan")));
                            }
                        } catch (Exception e) {
                            // no number?
                            dragCell.setColspan(1);
                        }

                        // cell value
                        if(currentXmlCell.hasChildNodes()) {
                            if(currentXmlCell.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
                                dragCell.setCellValue(this.readElementTextContent(currentXmlCell.getChildNodes().item(0).getTextContent()));
                            }
                        }

                        // column identifier
                        try {
                            if(currentXmlCell.hasAttribute("columnIdentifier")) {
                                dragCell.setDragIdentifier(currentXmlCell.getAttribute("columnIdentifier"));
                            }
                        } catch (Exception e) {
                            // no number?
                            dragCell.setDragIdentifier("");
                        }

                        // row identifier
                        try {
                            if(currentXmlCell.hasAttribute("rowIdentifier")) {
                                dragCell.setDragIdentifier(currentXmlCell.getAttribute("rowIdentifier"));
                            }
                        } catch (Exception e) {
                            // no number?
                            dragCell.setDragIdentifier("");
                        }

                        // add cell to row
                        tmpRow.addCell(dragCell);

                        break;
                        // endregion

                } // end switch case
            }

            // add row to table
            tmpTable.addRow(tmpRow);
        }

        return tmpTable;
    }

    /**
     * Reads the prompt element out of any interaction-element
     * @param parentElement The xml-element, which contains probably a prompt element
     * @return A String, maybe empty, if prompt element does not exist
     */
    private String readPrompt(Element parentElement) {
        try {
            Element elementPrompt = (Element) parentElement.getElementsByTagName("prompt").item(0);

            return readElementTextContent(elementPrompt.getTextContent());
        } catch (Exception e) {
            // prompt does not exist
        }

        return "";
    }

    /**
     * Reads the content between tags (an element).
     * May be the inner content of an element can contain newline-elements or not needed empty spaces.
     * @param input Text from element
     * @return Content of an element, without \n and blanks
     */
    private String readElementTextContent(String input) {
        String output = input.replace("\n", "");
        output = output.trim();

        return output;
    }

    /**
     * Reads the itemBody content between <p>-tags
     * @param assessmentItem
     * @return
     */
    private List<String> readItemBodyParagraphs(Element assessmentItem) {
        List<String> paragraphs = new ArrayList<String>();

        try {
            Element elementItemBody = (Element) assessmentItem.getElementsByTagName("itemBody").item(0);
            NodeList paragraphChilds = elementItemBody.getElementsByTagName("p");

            for(int i=0;i < paragraphChilds.getLength();i++) {
                StringBuilder paragraph = new StringBuilder();

                // <p>...</p> contains image?
                if(paragraphChilds.item(i).hasChildNodes()) {
                    for(int j=0;j < paragraphChilds.item(i).getChildNodes().getLength();j++) {
                        if(paragraphChilds.item(i).getChildNodes().item(j).getNodeType() == Node.TEXT_NODE) {
                            paragraph.append(paragraphChilds.item(i).getChildNodes().item(j).getTextContent());
                        } else if(paragraphChilds.item(i).getChildNodes().item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element imageNode = (Element) paragraphChilds.item(i).getChildNodes().item(j);
                            if(imageNode.getNodeName().equals("img")) {
                                paragraph.append("[img:" + imageNode.getAttribute("src") + "]");
                            }
                        }
                    }
                }

                paragraphs.add(paragraph.toString());
                //paragraphs.add(this.readElementTextContent(paragraphChilds.item(i).getTextContent()));
            }
        } catch (Exception e) {
            // return empty list
            return new ArrayList<String>();
        }

        return paragraphs;
    }

    // endregion
}