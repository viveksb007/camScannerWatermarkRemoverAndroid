package com.scanner.watermarkremover;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SRC = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.pdf";
    private static final String DEST = Environment.getExternalStorageDirectory().getAbsolutePath() + "/result.pdf";


    private static final int REQUEST_DOC = 1024;
    private static final int REQUEST_WRITE_PERMISSION = 100;
    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private ArrayList<String> srcFilePaths = new ArrayList<>();
    private boolean hasWritePermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnModifyPdf = findViewById(R.id.btn_modify_pdf);
        btnModifyPdf.setOnClickListener(this);
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_rationale), REQUEST_WRITE_PERMISSION, perms);
        } else {
            hasWritePermission = true;
        }
    }

    private void modifyPDF(String src, String dest) throws IOException, DocumentException {
        /*Ref - https://developers.itextpdf.com/examples/stamping-content-existing-pdfs-itext5/changing-page-sizes-existing-pdfs*/
        PdfReader reader = new PdfReader(src);
        int n = reader.getNumberOfPages();
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        PdfContentByte over;
        PdfDictionary pageDict;
        PdfArray mediaBox;
        float llx, lly, ury, urx;
        for (int i = 1; i <= n; i++) {
            pageDict = reader.getPageN(i);
            mediaBox = pageDict.getAsArray(PdfName.MEDIABOX);
            llx = mediaBox.getAsNumber(0).floatValue();
            lly = mediaBox.getAsNumber(1).floatValue();
            urx = mediaBox.getAsNumber(2).floatValue();
            ury = mediaBox.getAsNumber(3).floatValue();
            over = stamper.getOverContent(i);
            over.saveState();
            over.setColorFill(new GrayColor(1.0f));
            over.rectangle(llx, lly, urx, 20);
            over.fill();
            over.restoreState();
        }
        stamper.close();
        reader.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        hasWritePermission = true;
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Permission Denied. Please give all permissions for app to work properly.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (!hasWritePermission) {
            Toast.makeText(this, "Please provide required permissions.", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.btn_modify_pdf:
                try {
                    modifyPDF(SRC, DEST);
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.fab_add_file:
                FilePickerBuilder.getInstance()
                        .setActivityTheme(R.style.LibAppTheme)
                        .pickFile(this, REQUEST_DOC);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_DOC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> filePaths = new ArrayList<>(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    srcFilePaths.addAll(filePaths);
                    Log.v(TAG, srcFilePaths.toString());
                    // broadcast srcFilePath change to ListView Adapter
                }
                break;
        }
    }
}
