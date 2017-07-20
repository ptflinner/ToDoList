package com.example.patrick.todolist;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.patrick.todolist.data.Contract;
import com.example.patrick.todolist.data.ToDoItem;

import java.util.ArrayList;

import static android.R.attr.category;

/**
 * Created by mark on 7/4/17.
 */

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ItemHolder> {

    private Cursor cursor;
    private ItemClickListener listener;
    private String TAG = "todolistadapter";

    //Creates the viewholders that will hold the various pieces of data
    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item, parent, false);
        ItemHolder holder = new ItemHolder(view);

        return holder;
    }

    //Binds the information to pieces in the viewholder
    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(holder, position);
    }

    //Returns how many items that the recyclerview holds
    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    //Interface that is used in the main activity for when various things are clicked
    public interface ItemClickListener {
        void onItemClick(int pos, String description, String duedate,Integer completion,String category, long id);
        void checkBoxUsed(String description, String dueDate,Integer completion,String category, long id);
    }

    //Creates the Adapter object
    public ToDoListAdapter(Cursor cursor, ItemClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
    }

    //Closes the current cursor and sets it to the head of the new database
    public void swapCursor(Cursor newCursor){
        if (cursor != null) cursor.close();
        cursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    //Viewholder/Itemholder class
    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView descr;
        private TextView due;
        private CheckBox checkBox;
        private String duedate;
        private String description;
        private String category;
        private Integer itemCompleted=0;

        long id;

        //Creates the itemholder object
        //Initializes everything
        ItemHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.description);
            due = (TextView) view.findViewById(R.id.dueDate);
            checkBox=(CheckBox) view.findViewById(R.id.checkbox);
            view.setOnClickListener(this);
            Log.d(TAG,"CREATION: ");
        }

        //Was used to test where things were created for learning experience
        //Binding happens in next function
        public void bind(ItemHolder holder, int pos) {

            cursor.moveToPosition(pos);

            id = cursor.getLong(cursor.getColumnIndex(Contract.TABLE_TODO._ID));
            Log.d(TAG, "deleting id: " + id);
            bindItems(holder);
        }

        //Binds the data to the specific pieces in the itemholder
        //Sets a listener for the checkbox
        private void bindItems(ItemHolder holder){
            duedate = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE));
            description = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION));
            itemCompleted=cursor.getInt(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION));
            category=cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY));

            //Sets a listener to the checkbox
            //Calls the interface function when it is checked or unchecked
            checkBox.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if(checkBox.isChecked()){
                        itemCompleted=1;
                    }
                    else
                        itemCompleted=0;
                    listener.checkBoxUsed(description,duedate,itemCompleted,category,id);
                }
            });

            if(itemCompleted==1){
                checkBox.setChecked(true);
            }else{
                checkBox.setChecked(false);
            }

            descr.setText(description);
            due.setText(duedate);
            holder.itemView.setTag(id);

        }

        //Returns info when the itemholder is clicked
        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            listener.onItemClick(pos, description, duedate,itemCompleted,category, id);
        }

    }

}
