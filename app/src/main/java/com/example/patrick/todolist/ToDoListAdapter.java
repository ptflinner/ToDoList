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
    private String category;
    private String TAG = "todolistadapter";

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item, parent, false);
        ItemHolder holder = new ItemHolder(view,category);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(holder, position);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public interface ItemClickListener {
        void onItemClick(int pos, String description, String duedate,Integer completion,String category, long id);
    }

    public ToDoListAdapter(Cursor cursor, String category,ItemClickListener listener) {
        this.cursor = cursor;
        this.listener = listener;
        this.category=category;
    }

    public void swapCursor(Cursor newCursor){
        if (cursor != null) cursor.close();
        cursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView descr;
        private TextView due;
        private CheckBox checkBox;
        private String duedate;
        private String description;
        private String category;
        private Integer itemCompleted=0;
        long id;


        ItemHolder(View view,String category) {
            super(view);
            descr = (TextView) view.findViewById(R.id.description);
            due = (TextView) view.findViewById(R.id.dueDate);
            checkBox=(CheckBox) view.findViewById(R.id.checkbox);
            this.category=category;
            view.setOnClickListener(this);
        }


        public void bind(ItemHolder holder, int pos) {
            cursor.moveToPosition(pos);
            id = cursor.getLong(cursor.getColumnIndex(Contract.TABLE_TODO._ID));
            Log.d(TAG, "deleting id: " + id);
            Log.d(TAG,"Category: "+category);


            if(category=="Default"){
                category=cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY));
                duedate = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE));
                description = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION));
                itemCompleted=cursor.getInt(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION));

                if(itemCompleted==1){
                    checkBox.setChecked(true);
                }else{
                    checkBox.setChecked(false);
                }

                descr.setText(description);
                due.setText(duedate);
                holder.itemView.setTag(id);
            }
            else{
                String dbCategory=cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY));;
                if(category==dbCategory){
                    duedate = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE));
                    description = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION));
                    itemCompleted=cursor.getInt(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_COMPLETION));

                    if(itemCompleted==1){
                        checkBox.setChecked(true);
                    }else{
                        checkBox.setChecked(false);
                    }

                    descr.setText(description);
                    due.setText(duedate);
                    holder.itemView.setTag(id);
                }
                else{
                    Log.e(TAG,"Failed to find correct category");
                }
            }
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            listener.onItemClick(pos, description, duedate,itemCompleted,category, id);
        }

        public void completionCheck(View v){
            CheckBox checkBox=(CheckBox)v;
            if(checkBox.isChecked()){
                itemCompleted=1;
            }
        }
    }

}
