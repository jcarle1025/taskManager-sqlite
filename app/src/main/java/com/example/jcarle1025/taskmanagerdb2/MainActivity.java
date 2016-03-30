package com.example.jcarle1025.taskmanagerdb2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Button add, remove, clear, edit;
    ListView myListView;
    ManagerAdapter listAdapter;
    ArrayList<ToDoList> allLists = new ArrayList<ToDoList>();
    int index;
    String listName, dbName = "metaTaskManager";
    boolean changed;
    SQLiteDatabase db;
    EditText addName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myListView = (ListView) findViewById(R.id.listView);
        add = (Button) findViewById(R.id.addButton);
        remove = (Button) findViewById(R.id.removeButton);
        clear = (Button) findViewById(R.id.clearAllButton);
        edit = (Button) findViewById(R.id.editButton);
        add.setOnClickListener(this);
        remove.setOnClickListener(this);
        edit.setOnClickListener(this);
        clear.setOnClickListener(this);
        addName = (EditText) findViewById(R.id.addName);

        listAdapter = new ManagerAdapter(this, allLists);

        db = openOrCreateDatabase(dbName, 0, null);
        populateAllLists();
        myListView.setAdapter(listAdapter);
    }

    public void populateAllLists(){
        allLists.clear();
        ArrayList<ToDoList> tempList = new ArrayList<ToDoList>();
        Cursor c = db.rawQuery("SELECT name from sqlite_master WHERE type='table'", null);
        c.moveToFirst();

        while (c.isAfterLast() == false) {
            String s = c.getString(0);
            if (s.contains("_"))
                s = s.replace('_', ' ');
            tempList.add(new ToDoList(s));
            c.moveToNext();
        }
        ArrayList<Integer> a = new ArrayList<>();
        for(ToDoList t : tempList) {
            if (t.getName().contains("metadata") || t.getName().contains("sqlite")) {
                allLists.remove(t);
            }
            else
                allLists.add(t);
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        db = openOrCreateDatabase(dbName, 0, null);
        populateAllLists();
        clearChecks();

        if (changed) {
            ToDoList temp = allLists.get(index);
            String s = temp.getName();
            if (s.contains(" "))
                s = s.replace(' ','_');
            if(listName.contains(" "))
                listName = listName.replace(' ','_');
            try {
                db.execSQL("ALTER TABLE " + s + " RENAME TO " + listName + "");
            }
            catch(Exception e){
                Toast.makeText(MainActivity.this, "COULDNT RENAME TABLE", Toast.LENGTH_SHORT).show();
            }
            changed = false;
        }
        populateAllLists();
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addButton:
                addList();
                break;
            case R.id.removeButton:
                removeLists();
                break;
            case R.id.editButton:
                editList();
                break;
            case R.id.clearAllButton:
                clearAllLists();
                break;
        }
    }

    public void removeLists(){
        ArrayList<String> namesToRem = new ArrayList<String>();
        for(ToDoList t : allLists){
            if (t.isSelected())
                namesToRem.add(t.getName());
        }

        for(String s : namesToRem){
            if(s.contains(" "))
                s = s.replace(' ','_');

            try {
                db.execSQL("drop table " + s);
            } catch (Exception e){
//                Toast.makeText(MainActivity.this, "Couldnt drop " + s, Toast.LENGTH_SHORT).show();
            }
        }
        populateAllLists();
        listAdapter.notifyDataSetChanged();
    }

    public void clearAllLists(){
        Cursor c = db.rawQuery("SELECT name from sqlite_master WHERE type='table'", null);
        c.moveToFirst();

        while (c.isAfterLast() == false) {
            String s = c.getString(0);
            if(s.contains(" "))
                s = s.replace(' ','_');
                try {
                    db.execSQL("drop table " + s);
//                    Toast.makeText(MainActivity.this, "dropped "+s,Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
//                    Toast.makeText(MainActivity.this, "Couldnt drop " + s, Toast.LENGTH_SHORT).show();
                }
            c.moveToNext();
        }
        populateAllLists();
        listAdapter.notifyDataSetChanged();
    }

    public void addList(){
        String nameAdded = addName.getText().toString();
        if (addName.length() == 0) {
            Toast.makeText(MainActivity.this,"ENTER A LIST NAME!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(nameAdded.contains(" "))
            nameAdded = nameAdded.replace(' ', '_');

        db.beginTransaction();

        try {
            db.execSQL("create table if not exists " + nameAdded + " (" +
                    " recID integer PRIMARY KEY autoincrement," +
                    " title text," +
                    " description text," +
                    " date text );");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Failed to create " + addName.getText().toString() + " table", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
        allLists.add(new ToDoList(addName.getText().toString()));
        addName.setText("");
        listAdapter.notifyDataSetChanged();
    }

    public void editList(){
        int sel = 0;

        for (int i = 0; i < allLists.size(); i++) {
            if (allLists.get(i).isSelected()) {
                index = i;
                sel++;
            }
        }

        if (sel != 1) {
            Toast.makeText(MainActivity.this, "CHOOSE EXACTLY ONE LIST!", Toast.LENGTH_SHORT).show(); return;
        }

        ToDoList list = allLists.get(index);

        Intent oldMain = new Intent(MainActivity.this,OldMain.class);
        Bundle data = new Bundle();
        data.putString("t", list.getName());
        data.putString("d", dbName);
        oldMain.putExtras(data);
        startActivityForResult(oldMain, 100);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        super.onActivityResult(requestCode, resultCode, i);

        try {
            if (requestCode == 100 && resultCode == Activity.RESULT_OK) { //edited list
                Bundle data = i.getExtras();
                listName = data.getString("newListName");
                if(listName.contains(" "))
                    listName = listName.replace(' ','_');
                changed = true;
                clearChecks();
            } else if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
                clearChecks();
            }
        } catch (Exception e) {
            Log.i("ABC", "EXCEPTION IN EDITACTIVITY" + e.getMessage());
        }
    }

    public void clearChecks(){
        for(ToDoList t: allLists){
            t.setSelected(false);
        }
    }
}