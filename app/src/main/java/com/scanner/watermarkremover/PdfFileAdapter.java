package com.scanner.watermarkremover;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PdfFileAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<String> filePaths;
    private Context context;

    public PdfFileAdapter(ArrayList<String> filePaths, Context context) {
        this.filePaths = filePaths;
        this.context = context;
    }

    @Override
    public int getCount() {
        return filePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return filePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null && inflater != null) {
            convertView = inflater.inflate(R.layout.list_item, null);
        }
        assert convertView != null;
        TextView pdfName = convertView.findViewById(R.id.tv_pdf_name);
        String path = filePaths.get(position);
        String filename = path.substring(path.lastIndexOf('/') + 1);
        pdfName.setText(filename);
        return convertView;
    }
}
