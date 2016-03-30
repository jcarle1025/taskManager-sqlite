package com.example.jcarle1025.taskmanagerdb2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.Serializable;
import java.util.ArrayList;

public class ManagerAdapter extends ArrayAdapter implements Serializable {

    private final Context context;
    private final ArrayList<ToDoList> lists;

    public ManagerAdapter(Context context, ArrayList<ToDoList> lists) {
        super(context, R.layout.task_info, lists);
        this.context = context;
        this.lists = lists;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inf.inflate(R.layout.task_info, parent, false);
        final CheckBox c = (CheckBox) rowView.findViewById(R.id.checkbox);

        c.setText(lists.get(position).getName());

        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c.isChecked())
                    lists.get(position).setSelected(true);
                else if (!c.isChecked())
                    lists.get(position).setSelected(false);
            }
        });
        return rowView;
    }
}
