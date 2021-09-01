package com.example.crudapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements Filterable {

//    public  TextView title, desc;
//    public  Button speak;
//    public  ImageView edit_card, delete_card,share_card;
//    public  LinearLayout mlayout;
//    public  CardView cardView;


    private final ShowActivity activity;

    private final List<Model> mList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MyAdapter(ShowActivity activity, List<Model> mList) {
        this.activity = activity;
        this.mList = mList;
    }

    public void updateData(int position) {
        Model item = mList.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("uId", item.getId());
        bundle.putString("uTitle", item.getTitle());
        bundle.putString("uDesc", item.getDesc());
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);


    }

    public void deleteData(int position) {
        Model item = mList.get(position);
        db.collection("Documents").document(item.getId()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               if (task.isSuccessful()) {
                                                   notifyRemoved(position);
                                                   Toast.makeText(activity,
                                                           item.getId() + " is delected", Toast.LENGTH_SHORT).show();

                                               } else {
                                                   Toast.makeText(activity,
                                                           "ERROR!\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                               }
                                           }

                                       }
                );
    }

    private void notifyRemoved(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
        activity.showData();

    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.title.setText(mList.get(position).getTitle());
        holder.desc.setText(mList.get(position).getDesc());

        holder.speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(position);
            }
        });
        holder.speak.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak2(position);
                return false;
            }
        });
        holder.edit_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int position = holder.getAdapterPosition();
                                updateData(position);
                                notifyDataSetChanged();

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                }).create().show();
            }
        });
        holder.delete_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int position = holder.getAdapterPosition();
                                deleteData(position);
                                notifyDataSetChanged();

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                }).create().show();
            }
        });

        //type of option to sent to
        holder.share_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = mList.get(position).getDesc();
                String shareSub = mList.get(position).getTitle();
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                activity.startActivity(Intent.createChooser(sharingIntent, "Share using"));
            }
        });

        //card Expand
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Toast.makeText(activity, "clicked card title: " + holder.title, Toast.LENGTH_SHORT).show();
                int view = ((holder.desc.getVisibility()) == View.GONE) ? View.VISIBLE : View.GONE;
                TransitionManager.beginDelayedTransition(holder.mlayout, new AutoTransition());
                holder.desc.setVisibility(view);


            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public Filter getFilter() {

        return filter;
    }

    Filter filter = new Filter() {
        //runs on background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<Model> filterList = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                filterList.addAll(mList);

            } else {
                for (Model title : mList) {
                    if (title.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filterList.add(title);
                    }

                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filterList;
            return filterResults;
        }

        //runs on UI thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            mList.clear();

            mList.addAll((Collection<? extends Model>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc;
        Button speak;
        ImageView edit_card, delete_card, share_card;
        LinearLayout mlayout;
        CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title_text);
            desc = itemView.findViewById(R.id.desc);
            speak = itemView.findViewById(R.id.speak);
            edit_card = itemView.findViewById(R.id.edit_card);
            delete_card = itemView.findViewById(R.id.delete_card);
            share_card = itemView.findViewById(R.id.share_card);
            mlayout = itemView.findViewById(R.id.layout);
            cardView = itemView.findViewById(R.id.cardview);

        }
    }

    /////////////////////////play button///////////////////////////////////////
    public void speak(int position) {
        Model item = mList.get(position);
        activity.mTTS.speak(item.getTitle(), TextToSpeech.QUEUE_FLUSH, null);

    }

    public void speak2(int position) {
        Model item = mList.get(position);
        activity.mTTS.speak(item.getDesc(), TextToSpeech.QUEUE_FLUSH, null);

    }
    /////////////////////////play ends///////////////////////////////////////


}
