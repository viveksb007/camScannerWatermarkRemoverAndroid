package com.scanner.watermarkremover;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class ListPDFsActivity extends AppCompatActivity {

    private static final String TAG = ListPDFsActivity.class.getSimpleName();
    private static final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/documents/WatermarkRemover";
    ListView lvPdfFiles;
    private ArrayList<String> filePaths = new ArrayList<>();
    private PdfFileAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pdfs);
        lvPdfFiles = findViewById(R.id.lv_files);
        adapter = new PdfFileAdapter(filePaths, this);
        lvPdfFiles.setAdapter(adapter);
        lvPdfFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = new File(filePaths.get(position));
                Intent viewPdfIntent = new Intent(Intent.ACTION_VIEW);
                viewPdfIntent.setDataAndType(Uri.fromFile(file), "application/pdf");
                viewPdfIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(viewPdfIntent);
            }
        });
        updateLV();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateLV() {
        File fileDir = new File(BASE_PATH);
        if (fileDir.isDirectory()) {
            File[] files = fileDir.listFiles();
            if (files.length > 0) {
                for (File file : files) {
                    filePaths.add(file.getAbsolutePath());
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

}
