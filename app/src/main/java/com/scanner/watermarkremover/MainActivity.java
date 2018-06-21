package com.scanner.watermarkremover;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SRC = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.pdf";
    private static final String DEST = Environment.getExternalStorageDirectory().getAbsolutePath() + "/result.pdf";
    private static final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/documents/WatermarkRemover";

    boolean doubleBackToExitPressedOnce = false;
    private static final int REQUEST_DOC = 1024;
    private static final int REQUEST_WRITE_PERMISSION = 100;
    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private ArrayList<String> srcFilePaths = new ArrayList<>();
    private ListView fileListView;
    private PdfFileAdapter adapter;
    private boolean hasWritePermission = false;
    private Context context;
    private AlertDialog alertDialog;

    private final Object lock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        fileListView = findViewById(R.id.lv_files);
        adapter = new PdfFileAdapter(srcFilePaths, this);
        fileListView.setAdapter(adapter);
        fileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new MaterialDialog.Builder(context)
                        .title("Remove Pdf")
                        .content("Do you want to remove this PDF from list?")
                        .negativeText("No")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .positiveText("Yes")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                srcFilePaths.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .build()
                        .show();
                return true;
            }
        });
        Button btnModifyPdf = findViewById(R.id.btn_modify_pdf);
        btnModifyPdf.setOnClickListener(this);
        FloatingActionButton fabAddFile = findViewById(R.id.fab_add_file);
        fabAddFile.setOnClickListener(this);
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
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_back_again), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
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
                File dir = new File(BASE_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (srcFilePaths.size() == 0)
                    Toast.makeText(this, "No PDF selected.", Toast.LENGTH_SHORT).show();
                else {
                    //Toast.makeText(this, "Processing " + srcFilePaths.size() + " PDFs", Toast.LENGTH_SHORT).show();
                    // Process PDFs
                    alertDialog = new SpotsDialog.Builder()
                            .setContext(context)
                            .setMessage("Processing PDFs")
                            .setCancelable(false)
                            .build();
                    alertDialog.show();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            for (int i = 0; i < srcFilePaths.size(); i++) {
                                String path = srcFilePaths.get(i);
                                new ModifyPdf(path, BASE_PATH + "/" + path.substring(path.lastIndexOf('/') + 1)).execute();
                                synchronized (lock) {
                                    try {
                                        lock.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    alertDialog.dismiss();
                                    new MaterialDialog.Builder(context)
                                            .title("Files processed")
                                            .content(getString(R.string.processed_prompt))
                                            .positiveText("Yes")
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    srcFilePaths.clear();
                                                    adapter.notifyDataSetChanged();
                                                    Intent viewPdfsIntent = new Intent(MainActivity.this, ListPDFsActivity.class);
                                                    startActivity(viewPdfsIntent);
                                                }
                                            })
                                            .negativeText("No")
                                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    srcFilePaths.clear();
                                                    adapter.notifyDataSetChanged();
                                                    dialog.dismiss();
                                                }
                                            })
                                            .build()
                                            .show();
                                }
                            });
                        }
                    };
                    thread.start();
                }
                break;
            case R.id.fab_add_file:
                FilePickerBuilder.getInstance()
                        .setActivityTheme(R.style.LibAppTheme)
                        .pickFile(this, REQUEST_DOC);
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ModifyPdf extends AsyncTask<Void, Void, Void> {

        private String src;
        private String dest;

        ModifyPdf(String src, String dest) {
            this.src = src;
            this.dest = dest;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                modifyPDF(src, dest);
            } catch (IOException | DocumentException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_DOC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> filePaths = new ArrayList<>(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    for (String path : filePaths) {
                        if (!srcFilePaths.contains(path) && path.endsWith(".pdf"))
                            srcFilePaths.add(path);
                    }
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_rate_app:
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, uri);
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(marketIntent);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                break;
            case R.id.menu_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this app to remove watermark from CamScanner Docs : " + "https://play.google.com/store/apps/details?id=" + getPackageName());
                shareIntent.setType("text/plain");
                startActivity(shareIntent);
                break;
            case R.id.menu_feedback:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "vbplayapps@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Watermark Remover App");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Send Feedback.."));
                break;
            case R.id.menu_instructions:
                new MaterialDialog.Builder(context)
                        .title("Instructions")
                        .customView(R.layout.instruction_dialog, true)
                        .positiveText("Okay")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .build()
                        .show();
                break;
            case R.id.menu_view_pdfs:
                Intent viewPdfsIntent = new Intent(MainActivity.this, ListPDFsActivity.class);
                startActivity(viewPdfsIntent);
                break;
        }
        return true;
    }

}
