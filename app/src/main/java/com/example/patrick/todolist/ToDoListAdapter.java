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

    private ItemClickListener listener;
    private ArrayList<ToDoItem> toDoList;
    private String TAG = "todolistadapter";

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item, parent, false);

        ItemHolder holder = new ItemHolder(view);

        Log.d(TAG,"CREATION ENDS?");
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(holder, position);
    }

    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    public interface ItemClickListener {
        void onItemClick(int pos, String description, String duedate,Integer completion,String category, long id);
    }

    public ToDoListAdapter(ArrayList<ToDoItem> toDoList,ItemClickListener listener) {

        this.listener = listener;
        this.toDoList=toDoList;
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView descr;
        private TextView due;
        private CheckBox checkBox;
        private String duedate;
        private String description;
        private String category;
        private Integer itemCompleted;

        long id;


        ItemHolder(View view) {
            super(view);
            descr = (TextView) view.findViewById(R.id.description);
            due = (TextView) view.findViewById(R.id.dueDate);
            checkBox=(CheckBox) view.findViewById(R.id.checkbox);
            view.setOnClickListener(this);
            Log.d(TAG,"CREATION: ");
        }


        public void bind(ItemHolder holder, int pos) {

            Log.d(TAG,"Category: "+category);

            duedate = toDoList.get(pos).getDueDate();
            description = toDoList.get(pos).getDescription();
            itemCompleted=toDoList.get(pos).getCompleted();
            category=toDoList.get(pos).getCategory();
            id=toDoList.get(pos).getId();

            if(itemCompleted==1){
                checkBox.setChecked(true);
            }else{
                checkBox.setChecked(false);
            }

            descr.setText(description);
            due.setText(duedate);
            holder.itemView.setTag(id);

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
