package com.fsgtech.detectme;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jomari on 10/20/2017.
 */

public class ArrayAdapterDisplay extends ArrayAdapter {

    private Context context;
    private ArrayList<String> title =  new ArrayList<>();
    ArrayList<String> desc =  new ArrayList<>();

    public ArrayAdapterDisplay(Context context, ArrayList<String> title, ArrayList<String> desc) {
        super(context, R.layout.disp_layout, title);
        this.context = context;
        this.title = title;
        this.desc = desc;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View subLL;

        subLL = layoutInflater.inflate(R.layout.disp_layout, parent, false);

        TextView titles = subLL.findViewById(R.id.textView);
        titles.setText(title.get(position));

        TextView descs = subLL.findViewById(R.id.textView2);
        descs.setText(desc.get(position));


        return subLL;
    }
}


