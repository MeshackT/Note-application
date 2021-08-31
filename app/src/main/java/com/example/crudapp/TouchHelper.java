package com.example.crudapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class TouchHelper extends ItemTouchHelper.SimpleCallback {


    private MyAdapter adapter;
    private ShowActivity activity;

    public TouchHelper(MyAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {

            new AlertDialog.Builder(viewHolder.itemView.getContext())
                    .setMessage("Are you sure that you want to update?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = viewHolder.getAdapterPosition();
                            adapter.updateData(position);
                            adapter.notifyDataSetChanged();

                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
            }).create().show();
        } else if (direction == ItemTouchHelper.RIGHT) {
            new AlertDialog.Builder(viewHolder.itemView.getContext())
                    .setMessage("Do you want to delete?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = viewHolder.getAdapterPosition();
                            adapter.deleteData(position);
                            adapter.notifyItemRemoved(position);
                            adapter.notifyItemChanged(viewHolder.getAdapterPosition());


                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
            }).create().show();
        } else if (direction == ItemTouchHelper.ACTION_STATE_IDLE) {
            adapter.speak2(position);
            adapter.notifyItemChanged(viewHolder.getAdapterPosition());

        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeRightBackgroundColor(Color.rgb(151, 70, 202))
                .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24)
                .addSwipeLeftBackgroundColor(Color.rgb(227, 64, 213))
                .addSwipeLeftActionIcon(R.drawable.ic_baseline_edit_24)
                .create()
                .decorate();

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);


    }
}
