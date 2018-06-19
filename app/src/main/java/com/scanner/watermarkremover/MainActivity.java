package com.scanner.watermarkremover;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SRC = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.pdf";
    private static final String DEST = Environment.getExternalStorageDirectory().getAbsolutePath() + "/result.pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnModifyPdf = findViewById(R.id.btn_modify_pdf);
        btnModifyPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    modifyPDF();
                } catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void modifyPDF() throws IOException, DocumentException {
        /*Ref - https://developers.itextpdf.com/examples/stamping-content-existing-pdfs-itext5/changing-page-sizes-existing-pdfs*/
        PdfReader reader = new PdfReader(SRC);
        int n = reader.getNumberOfPages();
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(DEST));
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
}
