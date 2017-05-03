package Comprehensive;

// https://gist.github.com/dhavaln/7c7e3a95442a1a3e6af3

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import Components.Assessment;
import Components.RelatedGroup;
import Components.Support;
import it.liehr.mls_app.R;

public class DownloadZipPackage extends AsyncTask<String, String, String> {
    private String mUrl = "";
    private String mTargetDownloadedZipFile = "";   // downloaded zip file, where to store
    private String mContentFirstTarget = "";        // after download, where should files to be extracted? temporary
    private String mContentSecondTarget = "";       //
    private TextView mResponseTextView = null;
    private Context mContext = null;
    private static String responseTextViewText = "";

    public DownloadZipPackage(String url, String targetZipFile, String contentFirstTarget, Context context, TextView responseTextView) {
        if(url != "" && targetZipFile != "" && contentFirstTarget != "" && context != null && responseTextView != null) {
            mUrl = url;
            mResponseTextView = responseTextView;
            mContext = context;
            mTargetDownloadedZipFile = targetZipFile;
            mContentFirstTarget = contentFirstTarget;
        } else {
            throw new IllegalArgumentException("Url empty.");
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... xUrl) {
        // first, download package
        if(this.downloadFile()) {
            // zip file (assessments package) sucessfully downloaded
            // verify downloaded file
            if(this.verifyDownloadedFile()) {
                // file successfully verified
                // extract package
                if(this.extractPackage()) {
                    // extracting ok, verify extracted files
                    // info
                    //this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_package_extracted) + "\n");

                    // verify extracted files
                    if(this.verifyExtractedFiles()) {
                        // verification ok, start import
                        // info
                        //this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_verify_extracted_files) + ": Ok\n");

                        // merge directories
                        if(mergeDirectories()) {
                            // import into database
                            if(import_into_database()) {
                                Import_Xml2Obj import_xml2Obj = new Import_Xml2Obj(mContentFirstTarget);
                                import_xml2Obj.startImportProgress();

                                if(import_xml2Obj.getLastImportSuccessful()) {
                                    // save to db
                                    Import_Obj2Db import_obj2Db = new Import_Obj2Db(this.mContext, mUrl);

                                    List<Assessment> importAssessments = import_xml2Obj.getImportedAssessments();
                                    List<Support> supports = import_xml2Obj.getImportedSupport();
                                    List<RelatedGroup> related = import_xml2Obj.getImportedRelatedGroups();
                                    // database
                                    DatabaseHelper helper = new DatabaseHelper(mContext);

                                    // import objects to db
                                    if(import_obj2Db.importAssessments(importAssessments, helper)) {
                                        // sucessfull
                                        this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_import_assessments_successfull) + "\n");

                                        // try to import related and supports
                                        if(import_obj2Db.importRelated(related, helper)) {
                                            this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_import_related_successfull) + "\n");
                                        } else {
                                            this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_import_related_not_successfull) + "\n");
                                        }

                                        if(import_obj2Db.importSupports(supports, helper)) {
                                            this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_import_supports_successfull) + "\n");
                                        } else {
                                            this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_import_supports_not_successfull) + "\n");
                                        }
                                    } else {
                                        // error
                                        this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_import_assessments_not_successfull) + "\n");
                                    }

                                } else {
                                    // error
                                    this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_import_assessments_not_successfull) + "\n");
                                }
                            } else {

                            }
                        }
                    } else {
                        // error info
                        this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_verify_extracted_files) + ": " + mContext.getString(R.string.activity_config_import_message_verify_extracted_files_error) + "\n");
                    }
                }
            }
        } else {
            // error info
            this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_error_downloading_package));
        }

        // delete temp directories and files
        File directoryExtract = new File(((Activity) this.mContext).getFilesDir() + App.relativeExtractDataDirectory);
        File directoryTmp = new File(((Activity) this.mContext).getFilesDir() + "/tmp/");

        try {
            App.deleteRecursive(directoryExtract);
        } catch (Exception e) {
            Log.e("Error", "DownloadZipPackage doInBackground(): " + e.getMessage());
        }

        try {
            App.deleteRecursive(directoryTmp);
        } catch (Exception e) {
            Log.e("Error", "DownloadZipPackage doInBackground(): " + e.getMessage());
        }

        return null;
    }


    /**
     * Method for downloading the zip assessments package file
     * @return True, if download was sucessfull
     **/
    private boolean downloadFile() {
        try {
            // set connection to zip file
            URL url = new URL(mUrl);

            // check connection
            if(App.checkConnection(url)) {
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(2000);
                connection.connect();

                // info for user
                this.setResponseTextViewText(mContext.getString(R.string.activity_config_import_message_package_start_download) + "\n");

                // prepare file & set input & outout
                InputStream input = new BufferedInputStream(url.openStream());
                File zipFile = new File(mTargetDownloadedZipFile);
                zipFile.getParentFile().mkdirs();   // create parent directories
                zipFile.createNewFile();    // create file
                FileOutputStream output = new FileOutputStream(zipFile);

                // read data from inputstream, write to outputstream
                byte data[] = new byte[1024];
                long total = 0;
                int count = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }

                // close streams
                output.flush();
                output.close();
                input.close();

                // info for user
                this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_package_downloaded) + "\n");
            } else {
                // file not found
                this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_file_not_found) + "\n");
                return false;
            }
        } catch (java.io.IOException ioe) {
            Log.e("Error", "Download progress (io): " + ioe.getMessage());
            return false;
        } catch (Exception e) {
            Log.e("Error", "Download progress: " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Method for verifying the downloaded zip assessments package file.
     * @return False, if an error occurred or files does not exist or file is empty
     */
    private Boolean verifyDownloadedFile() {
        try {
            File fileToCheck = new File(mTargetDownloadedZipFile);

            if(fileToCheck.exists()) {
                if(fileToCheck.length() > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        // if file does not exist, return false
        return false;
    }

    /**
     * Method for extracting the downloaded zip package
     *
     * @return True, if the complete extraction process was sucessfull
     */
    private Boolean extractPackage() {
        // info to user
        //this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_verify_downloaded_file_ok) + "\n");
        //this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_package_size) + ": " + (new File(mTargetDownloadedZipFile).length() / 1000 / 1000) + " MBytes\n");    // not divide: 1024
        //this.appendTextToResponseTextView(mContext.getString(R.string.activity_config_import_message_extract_package) + "\n");

        // start extracting
        try {
            InputStream is = new FileInputStream(mTargetDownloadedZipFile);
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze = null;

            // iterate each zip entry (an entry is a file or a directory)
            while((ze = zis.getNextEntry()) != null) {
                // check if entry is a directory
                if(ze.isDirectory()) {
                    // zip entry is directory
                    // maybe the directory must be created first
                    File f = new File(mContentFirstTarget + ze.getName());
                    //Log.i("Info", ze.getName());
                    if(!f.isDirectory()) {
                        f.mkdirs();
                    }
                } else {
                    // zip entry is a file
                    String newFilePath = mContentFirstTarget + ze.getName();
                    FileOutputStream fos = new FileOutputStream(newFilePath);  // overwrite existing file
                    //Log.i("Info", ze.getName());
                    byte[] buffer = new byte[8192];
                    int len;
                    while((len = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }

                    // close zip file output stream
                    fos.close();
                }
            }

            zis.close();
            is.close();

            return true;
        } catch (Exception e) {
            // return false;
        }

        // return false, if end of extracting process not reached
        return false;
    }

    /**
     * Method for verifying the extracted files.
     * At least assessments_standard.xml and assessments_extended.xml must be in the package.
     * A validation against a dtd is part of the method.
     * @return True if, assessments_standard.xml and assessments_extended.xml are part of the package. The dtd validation check must be successfull.
     */
    private Boolean verifyExtractedFiles() {
        // assessments_standard.xml
        // assessments_extended.xml
        // assessment_ids_related.xml
        // assessment_support.xml

        Validator validatorStandard = new Validator(this.mContext, mContentFirstTarget + "/assessments_standard.xml", "dtd_standard");
        Validator validatorExtended = new Validator(this.mContext, mContentFirstTarget + "/assessments_extended.xml", "dtd_extended");
        validatorStandard.validate();
        validatorExtended.validate();

        if(validatorStandard.isValidationSuccessful() && validatorExtended.isValidationSuccessful()) {
            return true;
        } else {
            // log errors
            for(String error:validatorStandard.getValidationWarnings()) {
                Log.w("xml_validation_std", "warning: " + error);
            }

            for(String error:validatorStandard.getValidationErrors()) {
                Log.e("xml_validation_std", "error: " + error);
            }

            for(String error:validatorStandard.getValidationFatalsErrors()) {
                Log.e("xml_validation_std", "fatal error: " + error);
            }

            for(String error: validatorExtended.getValidationWarnings()) {
                Log.w("xml_validation_ext", "warning: " + error);
            }

            for(String error:validatorExtended.getValidationErrors()) {
                Log.e("xml_validation_ext", "error: " + error);
            }

            for(String error:validatorExtended.getValidationFatalsErrors()) {
                Log.e("xml_validation_ext", "fatal error: " + error);
            }
        }

        return false;
    }

    private Boolean mergeDirectories() {
        // merge with working directory
        String directoryExtractPath = ((Activity) this.mContext).getFilesDir() + App.relativeExtractDataDirectory;
        String directoryWorkPath = ((Activity) this.mContext).getFilesDir() + App.relativeWorkingDataDirectory;

        try {
            File extractDirectory = new File(directoryExtractPath);
            File workingDirectory = new File(directoryWorkPath);

            this.copyDirectory(extractDirectory, workingDirectory);

            /*

            // check if working directory exists
            if(workingDirectory.isDirectory() && workingDirectory.exists()) {
                // exists, merge
            } else {
                // not existing, direct copy, extract -> working
                this.copyDirectory(extractDirectory, workingDirectory);

            }
            */
        } catch (Exception e) {
            Log.e("Error", "DownloadZipPackage mergeDirectories(): " + e.getMessage());
            return false;
        }
        
        return true;
    }

    public void copyDirectory(File sourceDirectory , File targetDirectory) throws IOException {
        if (sourceDirectory.isDirectory()) {
            if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
                throw new IOException("Directory cannot be created: " + targetDirectory.getAbsolutePath());
            }

            String[] children = sourceDirectory.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceDirectory, children[i]), new File(targetDirectory, children[i]));
            }
        } else {
            // target directory exists?
            File directory = targetDirectory.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceDirectory);
            OutputStream out = new FileOutputStream(targetDirectory);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    private Boolean import_into_database() {
        return true;
    }

    private String getResponseTextViewText() {
        return DownloadZipPackage.responseTextViewText;
    }

    private void setResponseTextViewText(final String text) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
            DownloadZipPackage.responseTextViewText = text;
            mResponseTextView.setText(text);
            }
        });
    }

    private void appendTextToResponseTextView(String text) {
        // sleep, give main thread some time
        try {
            Thread.sleep(100);
        } catch (Exception ex) {

        }
        this.setResponseTextViewText(DownloadZipPackage.responseTextViewText + text);
    }
}