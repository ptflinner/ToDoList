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

    //Creates the activity and initializes the variables and sets up the recycler view
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

    //When the app stops it closes connections to the db and to the cursor
    @Override
    protected void onStop() {
        super.onStop();
        if (db != null) db.close();
        if (cursor != null) cursor.close();
    }

    //When the app starts its lifecycle it creates the adapter
    @Override
    protected void onStart() {
        super.onStart();

        createAdapter(selectedCategory);
    }

    //The adapter is created and begins making and placing everything
    private void createAdapter(final String category){
        //Database is set up
        helper = new DBHelper(this);
        db = helper.getWritableDatabase();

        //Checks what categories need to be displayed
        //Default isn't an option to be placed in the database
        //So it must be checked for explicitly
        if(selectedCategory.toUpperCase().equals("DEFAULT")){
            cursor = getAllItems(db);
        }
        else{
            cursor =getCategoryItems(db);
        }

        //Adapter is made using the cursor that was set above.
        adapter = new ToDoListAdapter(cursor,new ToDoListAdapter.ItemClickListener() {

            //Interface in the todolist adapter is implemented here
            //on item click will trigger everything in the selected object to be passed to the update fragment
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

            //WIP. Supposed to update the database when checkbox is clicked
            @Override
            public void checkBoxUsed(String description, String dueDate, Integer completion, String category, long id) {
                updateToDo(dueDate,description,completion,category,id);
            }

        });

        rv.setAdapter(adapter);

        //Allows for items to be deleted by swiping
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

    //Closes the dialog for adding a dataset and then updates the UI elements
    @Override
    public void closeDialog(int year, int month, int day, String description,Integer completion,String category) {
        addToDo(db, description, formatDate(year, month, day),completion,category);
        cursor = getAllItems(db);
        adapter.swapCursor(cursor);
//        createAdapter(selectedCategory);
    }

    //formats the date to the proper style
    public String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }

    //Used to filter what categories are displayed
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

    //Gets all items in the database
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

    //Adds an item to the database
    private long addToDo(SQLiteDatabase db, String description, String duedate,Integer completion,String category) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION,completion);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY,category);
        return db.insert(Contract.TABLE_TODO.TABLE_NAME, null, cv);
    }

    //Removes an item from the database
    private boolean removeToDo(SQLiteDatabase db, long id) {
        Log.d(TAG, "deleting id: " + id);
        return db.delete(Contract.TABLE_TODO.TABLE_NAME, Contract.TABLE_TODO._ID + "=" + id, null) > 0;
    }

    //Updates an item in the database
    private int updateToDo(SQLiteDatabase db, int year, int month, int day, String description,Integer completion,String category, long id){

        String duedate = formatDate(year, month, day);

        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION,completion);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY,category);

        return db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
    }

    //WIP
    //Another way to update the Database
    private int updateToDo(String dueDate, String description,Integer completion,String category, long id){

        ContentValues cv = new ContentValues();
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, dueDate);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION,completion);
        cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY,category);

        return db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
    }

    //Closes the update dialog and sends the data to be updated
    //Then updates the UI
    @Override
    public void closeUpdateDialog(int year, int month, int day, String description, Integer completion,String category,long id) {
        updateToDo(db, year, month, day, description,completion,category, id);
        if(selectedCategory.toUpperCase().equals("DEFAULT")){
            cursor = getAllItems(db);
        }
        else{
            cursor = getCategoryItems(db);
        }
        adapter.swapCursor(cursor);
//        createAdapter(selectedCategory);
    }

    //Creates the menu in the top right
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.categories,menu);
        return true;
    }

    //Sets up what happens when an item is selected
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

    //WIP
    //Not working currently
    public void updateDatabase(String dueDate, String description,Integer completion,String category,long id){
        updateToDo(dueDate, description,completion,category, id);
        if(selectedCategory.toUpperCase().equals("DEFAULT")){
            cursor = getAllItems(db);
        }
        else{
            cursor = getCategoryItems(db);
        }
        adapter.swapCursor(cursor);
        createAdapter(selectedCategory);
    }
}
