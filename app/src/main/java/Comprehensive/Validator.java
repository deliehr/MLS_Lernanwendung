package Comprehensive;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import mf.javax.xml.parsers.DocumentBuilder;
import mf.javax.xml.parsers.DocumentBuilderFactory;
import mf.org.w3c.dom.Document;

/**
 * Class for validating an xml file.
 * Implements the parse methods made available by the Apache Xerces library.
 * First: create new object from Comprehensive.Validator class (e.g. Validator v = new Validator("path", id) ).
 * Second: start validating method (e.g. v.validate() ).
 * Last: the validation result will be stored in isValidationSuccessful() (e.g. v.isValidationSuccessful() ).
 */
public class Validator {
    // region object variables
    private Context context = null;
    private String filePathToValidate = "";
    private String dtdRessourceName = "";
    private boolean validationSuccessful = true;    // if error occured, the result will be false
    private List<String> validationWarnings = new ArrayList<String>();
    private List<String> validationErrors = new ArrayList<String>();
    private List<String> validationFatalsErrors = new ArrayList<String>();
    // endregion

    // region constructors
    public Validator(Context context, String filePathToValidate, String dtdRessourceName) {
        this.setContext(context);
        this.setFilePathToValidate(filePathToValidate);
        this.setDtdRessourceName(dtdRessourceName);
    }
    // endregion

    // region object methods
    public void validate() {
        try {
            // first, create new file for checking
            File xmlFileToCheck = new File(this.getFilePathToValidate());

            // file existing?
            if(xmlFileToCheck.exists()) {
                // second, read dtd
                int dtdRessourceId = this.getContext().getResources().getIdentifier(this.getDtdRessourceName(), "raw", this.getContext().getPackageName());
                if(dtdRessourceId > 0) {
                    // read dtd
                    StringBuilder contentToCheck = new StringBuilder();
                    contentToCheck.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    contentToCheck.append(App.getStringContentFromRawFile(context, dtdRessourceId));     // dtd read
                    contentToCheck.append("\n");

                    // get content from assessment xml file
                    String xmlFileAssessments = App.getStringContentFromFile(xmlFileToCheck);

                    // append xml file to check to content to check
                    BufferedReader bufferedReader = new BufferedReader(new StringReader(xmlFileAssessments));
                    String line = "";
                    while((line = bufferedReader.readLine()) != null) {
                        if(!line.contains("xml version")) {
                            contentToCheck.append(line + "\n");
                        }
                    }

                    // safe to check file
                    File xmlFileWithDtd = new File(((Activity) this.getContext()).getFilesDir() + "/dtdcheck.xml");
                    if(xmlFileWithDtd.exists()) {
                        xmlFileWithDtd.delete();
                        xmlFileWithDtd.createNewFile();
                    }

                    // write content to new file
                    PrintWriter printWriter = new PrintWriter(xmlFileWithDtd);
                    printWriter.print(contentToCheck.toString());
                    printWriter.flush();
                    printWriter.close();

                    // validation
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance("mf.org.apache.xerces.jaxp.DocumentBuilderFactoryImpl", null);
                    factory.setValidating(true);
                    factory.setNamespaceAware(true);
                    DocumentBuilder parser = factory.newDocumentBuilder();
                    parser.setErrorHandler(new Validator.MyErrorHandler(this));
                    Document document = parser.parse(xmlFileWithDtd);

                    // after check, delete test file
                    if(xmlFileWithDtd.exists()) {
                        xmlFileWithDtd.delete();
                    }
                } else {
                    Log.e("xml_validation", "DTD ressource not found.");
                    this.setValidationSuccessful(false);
                }
            } else {
                Log.e("xml_validation", "File not found.");
                this.setValidationSuccessful(false);
            }
        } catch (Exception e) {
            Log.e("xml_validation", e.getMessage());
            this.setValidationSuccessful(false);
        }
    }

    public void addWarning(String warning) {
        this.validationWarnings.add(warning);
    }

    public void addError(String error) {
        this.validationErrors.add(error);
    }

    public void addFatalError(String fatalError) {
        this.validationFatalsErrors.add(fatalError);
    }
    // endregion

    // region getters & setters
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getFilePathToValidate() {
        return filePathToValidate;
    }

    public void setFilePathToValidate(String filePathToValidate) {
        this.filePathToValidate = filePathToValidate;
    }

    public String getDtdRessourceName() {
        return dtdRessourceName;
    }

    public void setDtdRessourceName(String dtdRessourceName) {
        this.dtdRessourceName = dtdRessourceName;
    }

    // private
    private void setValidationSuccessful(boolean validationSuccessful) {
        this.validationSuccessful = validationSuccessful;
    }

    public boolean isValidationSuccessful() {
        return validationSuccessful;
    }

    public List<String> getValidationWarnings() {
        return validationWarnings;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public List<String> getValidationFatalsErrors() {
        return validationFatalsErrors;
    }

    // endregion

    // region nested error handler
    private static class MyErrorHandler implements org.xml.sax.ErrorHandler {
        private Validator validatorObject = null;

        public MyErrorHandler(Validator validatorObject) {
            this.validatorObject = validatorObject;
        }

        @Override
        public void warning(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
            String warning = "Warning: " + exception.getMessage() + " Line: " + exception.getLineNumber();
            validatorObject.addWarning(warning);
            Log.w("sax", warning);
        }

        @Override
        public void error(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
            String error = "Error: " + exception.getMessage() + " Line: " + exception.getLineNumber();
            validatorObject.addError(error);
            this.validatorObject.setValidationSuccessful(false);
            Log.e("sax", error);
        }

        @Override
        public void fatalError(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
            String fatalError = "Fatal error: " + exception.getMessage() + " Line: " + exception.getLineNumber();
            validatorObject.addFatalError(fatalError);
            this.validatorObject.setValidationSuccessful(false);
            Log.e("sax", fatalError);
        }
    }
    // endregion
}
