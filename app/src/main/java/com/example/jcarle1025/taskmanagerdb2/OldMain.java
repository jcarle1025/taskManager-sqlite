package com.example.jcarle1025.taskmanagerdb2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OldMain extends AppCompatActivity implements View.OnClickListener, Serializable {
    public ArrayList<Task> taskList = new ArrayList<Task>();
    public String newTitle, newDetail;
    public int index, count = 1;
    public boolean changed = false;

    Button add, delete, edit, show, clear, done;
    ListView myListView;
    MyAdapter myTaskAdapter;
    String tableName, dbName;
    EditText newName, listName;
    SQLiteDatabase db;
    Intent parent;
    Bundle myBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_main);

        myListView = (ListView) findViewById(R.id.listView);

        add = (Button) findViewById(R.id.addButton);
        delete = (Button) findViewById(R.id.deleteButton);
        edit = (Button) findViewById(R.id.editDetail);
        show = (Button) findViewById(R.id.showDetail);
        clear = (Button) findViewById(R.id.clearAllButton);
        done = (Button) findViewById(R.id.doneButton);

        newName = (EditText) findViewById(R.id.addName);
        listName = (EditText) findViewById(R.id.listName);

        parent = getIntent();
        myBundle = parent.getExtras();

        tableName = myBundle.getString("t");
        listName.setText(tableName);
        if(tableName.contains(" "))
            tableName = tableName.replace(' ','_');

        dbName = myBundle.getString("d");

        db = openOrCreateDatabase(dbName, 0, null);
        createTable(tableName);
        updateTaskList();

    }

    private void updateTaskList() {
        taskList.clear();
        Cursor c = db.rawQuery("select * from " + tableName + " order by date", null);
        int titleId = c.getColumnIndex("title");
        int descriptionId = c.getColumnIndex("description");
        int dateId = c.getColumnIndex("date");
        c.moveToFirst();

        while (c.isAfterLast() == false) {
            String title = c.getString(titleId);
            String desc = c.getString(descriptionId);
            String date = c.getString(dateId);

            taskList.add(new Task(count++, title, desc, date));
            c.moveToNext();
        }
    }

    private void createTable(String name) {
        Cursor c = db.rawQuery("SELECT name from sqlite_master WHERE type='table'", null);
        c.moveToFirst();

        int i = c.getColumnIndex("name");

        while (c.isAfterLast() == false) {
            String myName = c.getString(i);
            if (myName.equals(tableName))
                return;
            c.moveToNext();
        }
        db.beginTransaction();

        try {
            db.execSQL("create table if not exists " + tableName + " (" +
                    " recID integer PRIMARY KEY autoincrement," +
                    " title text," +
                    " description text," +
                    " date text );");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Toast.makeText(OldMain.this, "Failed to create " + tableName + " table", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        myTaskAdapter = new MyAdapter(this, taskList);
        myListView.setAdapter(myTaskAdapter);

        add.setOnClickListener(this);
        delete.setOnClickListener(this);
        edit.setOnClickListener(this);
        show.setOnClickListener(this);
        clear.setOnClickListener(this);
        done.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        clearChecks();
        db = openOrCreateDatabase(tableName, 0, null);
        createTable(tableName);
        updateTaskList();

        if (changed) {
            Task temp = taskList.get(index);
            Cursor c = db.rawQuery("select * from " + tableName + " where title='" + temp.getTitle() + "'",null);
            c.moveToFirst();
            int dateId = c.getColumnIndex("date");
            String date = c.getString(dateId);

            db.execSQL("DELETE FROM " + tableName + " where title='" + temp.getTitle() + "'");
            addToDB(new Task(count++, newTitle, newDetail, date));//make the date better
            changed = false;
        }
        myTaskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addButton:
                addTask();
                break;
            case R.id.deleteButton:
                deleteTasks();
                break;
            case R.id.editDetail:
                editTask();
                break;
            case R.id.showDetail:
                showDetail();
                break;
            case R.id.clearAllButton:
                clearDB();
                break;
            case R.id.doneButton:
                finishEdit();
                break;
        }
    }

    public void finishEdit(){
        myBundle.putString("newListName", listName.getText().toString());
        parent.putExtras(myBundle);
        setResult(Activity.RESULT_OK, parent);
        finish();
    }

    public void addToDB(Task t) {
        Toast.makeText(OldMain.this, "Adding "+t.getTitle()+"\n"+t.getDescription(), Toast.LENGTH_SHORT).show();
        db.beginTransaction();
        try {
            db.execSQL(" insert into " + tableName + "(title,description,date) " +
                    "values ( '" + t.getTitle() + "','" + t.getDescription() + "','" + t.getDate() + "');");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Toast.makeText(OldMain.this, "could not enter data", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
        updateTaskList();
    }

    public void clearDB() {
        try {
            if (taskList.size() > 0) {
                count = 1;
                taskList.clear();
                if(tableName.contains(" "))
                    tableName = tableName.replace(' ','_');
                db.execSQL("drop table " + tableName + "; ");
            }
        } catch (Exception e) {
            Toast.makeText(OldMain.this, "could not clear table", Toast.LENGTH_SHORT).show();
        }
        createTable(tableName);
        updateTaskList();
        myTaskAdapter.notifyDataSetChanged();
    }

    public void addTask() {
        String input = newName.getText().toString();

        if (!input.isEmpty()) {
            SimpleDateFormat simpledate = new SimpleDateFormat("dd MMMM 'at' HH:mm");
            String date = simpledate.format(new Date());

            addToDB(new Task(count++, input, "", date));
            newName.setText("");
            myTaskAdapter.notifyDataSetChanged();
        } else
            Toast.makeText(OldMain.this, "ENTER A TASK NAME", Toast.LENGTH_SHORT).show();
    }

    public void deleteTasks() {
        ArrayList<Task> rem = new ArrayList<Task>();
        Cursor c;
        for (Task t : taskList) {
            if (t.isSelected())
                try {
                    db.execSQL("DELETE FROM " + tableName + " where title='" + t.getTitle() + "'");
                }catch (Exception e){
//                    Toast.makeText(OldMain.this,"COULDNT DELETE THAT ONE",Toast.LENGTH_SHORT).show();
                }
        }
        updateTaskList();
        myTaskAdapter.notifyDataSetChanged();
    }

    public void editTask() {
        int sel = 0;

        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).isSelected()) {
                index = i;
                sel++;
            }
        }

        if (sel != 1) {
            Toast.makeText(OldMain.this, "CHOOSE EXACTLY ONE TASK!", Toast.LENGTH_SHORT).show();
            return;
        }

        Task t = taskList.get(index);

        Intent editActivity = new Intent(OldMain.this, EditActivity.class);
        Bundle myBundle = new Bundle();

        myBundle.putString("title", t.getTitle());
        myBundle.putString("detail", t.getDescription());
        editActivity.putExtras(myBundle);

        startActivityForResult(editActivity, 101);
    }

    public void showDetail() {
        ArrayList<Task> shows = new ArrayList<Task>();
        for (Task t : taskList) {
            if (t.isSelected()) {
                shows.add(t);
            }
        }

        if (shows.size() != 1) {
            Toast.makeText(OldMain.this, "CHOOSE EXACTLY ONE TASK!", Toast.LENGTH_SHORT).show();
            return;
        }

        Task t = shows.get(0);

        Intent showActivity = new Intent(OldMain.this, ShowActivity.class);
        Bundle myBundle = new Bundle();

        myBundle.putString("title", t.getTitle());
        myBundle.putString("detail", t.getDescription());
        myBundle.putString("date", t.getDate());
        showActivity.putExtras(myBundle);

        startActivityForResult(showActivity, 102);
    }

    public void clearChecks() {
        for (Task t : taskList)
            t.setSelected(false);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        super.onActivityResult(requestCode, resultCode, i);

        try {
            if (requestCode == 101 && resultCode == Activity.RESULT_OK) { //edited task
                Bundle data = i.getExtras();
                newTitle = data.getString("newT");
                newDetail = data.getString("newD");
                changed = true;
                clearChecks();
            } else if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
                clearChecks();
            }
        } catch (Exception e) {
            Log.i("ABC", "EXCEPTION IN EDITACTIVITY" + e.getMessage());
        }
    }
}
