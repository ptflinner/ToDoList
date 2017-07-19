package com.example.patrick.todolist;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.example.patrick.todolist.data.Contract;
import com.example.patrick.todolist.data.DBHelper;
import com.example.patrick.todolist.data.ToDoItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AddToDoFragment.OnDialogCloseListener, UpdateToDoFragment.OnUpdateDialogCloseListener{

    private RecyclerView rv;
    private FloatingActionButton button;
    private DBHelper helper;
    private Cursor cursor;
    private SQLiteDatabase db;
    ToDoListAdapter adapter;
    private String selectedCategory="Default";
    private final String TAG = "mainactivity";
//    private ArrayList<ToDoItem> toDoList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "oncreate called in main activity");
        button = (FloatingActionButton) findViewById(R.id.addToDo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                AddToDoFragment frag = new AddToDoFragment();
                frag.show(fm, "addtodofragment");
            }
        });

        rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (db != null) db.close();
        if (cursor != null) cursor.close();
    }

    @Override
    protected void onStart() {
        super.onStart();

        createAdapter(selectedCategory);
    }

    private void createAdapter(String category){
        helper = new DBHelper(this);
        db = helper.getWritableDatabase();

        if(selectedCategory.toUpperCase().equals("DEFAULT")){
            cursor = getAllItems(db);
        }
        else{
            cursor =getCategoryItems(db);
        }


        adapter = new ToDoListAdapter(cursor, category,new ToDoListAdapter.ItemClickListener() {

            @Override
            public void onItemClick(int pos, String description, String duedate, Integer completion,String category,long id) {
                Log.d(TAG, "item click id: " + id);
                String[] dateInfo = duedate.split("-");
                int year = Integer.parseInt(dateInfo[0].replaceAll("\\s",""));
                int month = Integer.parseInt(dateInfo[1].replaceAll("\\s",""));
                int day = Integer.parseInt(dateInfo[2].replaceAll("\\s",""));

                FragmentManager fm = getSupportFragmentManager();

                UpdateToDoFragment frag = UpdateToDoFragment.newInstance(year, month, day, description,completion,category, id);
                frag.show(fm, "updatetodofragment");
            }
        });

        rv.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                long id = (long) viewHolder.itemView.getTag();
                Log.d(TAG, "passing id: " + id);
                removeToDo(db, id);
                adapter.swapCursor(getAllItems(db));
            }
        }).attachToRecyclerView(rv);
    }
    @Override
    public void closeDialog(int year, int month, int day, String description,Integer completion,String category) {
        addToDo(db, description, formatDate(year, month, day),completion,category);
        cursor = getAllItems(db);
        adapter.swapCursor(cursor);
        createAdapter(selectedCategory);
    }

    public String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }



    private Cursor getCategoryItems(SQLiteDatabase db){
        String args[]=new String[]{Contract.TABLE_TODO._ID,
                Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION,
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE,
                Contract.TABLE_TODO.COLUMN_NAME_CATEGORY,
                Contract.TABLE_TODO.COLUMN_NAME_COMPLETION};

        return db.query(
                Contract.TABLE_TODO.TABLE_NAME,
                args,
                Contract.TABLE_TODO.COLUMN_NAME_CATEGORY+"=?",
                new String[]{selectedCategory},
                null,
                null,
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE
        );
    }
    private Cursor getAllItems(SQLiteDatabase db) {
        return db.query(
                Contract.TABLE_TODO.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE
        );
    }

    private long addToDo(SQLiteDatabase db, String description, String duedate,Integer completion,String category) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION,completion);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY,category);
        return db.insert(Contract.TABLE_TODO.TABLE_NAME, null, cv);
    }

    private boolean removeToDo(SQLiteDatabase db, long id) {
        Log.d(TAG, "deleting id: " + id);
        return db.delete(Contract.TABLE_TODO.TABLE_NAME, Contract.TABLE_TODO._ID + "=" + id, null) > 0;
    }


    private int updateToDo(SQLiteDatabase db, int year, int month, int day, String description,Integer completion,String category, long id){

        String duedate = formatDate(year, month, day);

        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION,completion);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY,category);

        return db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
    }

    @Override
    public void closeUpdateDialog(int year, int month, int day, String description, Integer completion,String category,long id) {
        updateToDo(db, year, month, day, description,completion,category, id);
        adapter.swapCursor(getAllItems(db));
        createAdapter(selectedCategory);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.categories,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId=item.getItemId();
        if(itemThatWasClickedId==R.id.default_category){
            selectedCategory="Default";
            createAdapter(selectedCategory);
            return true;
        }
        if(itemThatWasClickedId==R.id.shopping_category){
            selectedCategory="Shopping";
            createAdapter(selectedCategory);
            return true;
        }
        if(itemThatWasClickedId==R.id.business_category){
            selectedCategory="Business";
            createAdapter(selectedCategory);
            return true;
        }
        if(itemThatWasClickedId==R.id.school_category){
            selectedCategory="School";
            createAdapter(selectedCategory);
            return true;
        }
        if(itemThatWasClickedId==R.id.personal_category){
            selectedCategory="Personal";
            createAdapter(selectedCategory);
            return true;
        }
        return onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public void createList(){

    }
}
