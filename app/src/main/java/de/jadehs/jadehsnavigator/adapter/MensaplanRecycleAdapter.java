package de.jadehs.jadehsnavigator.adapter;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.model.MensaplanMeal;

public class MensaplanRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = "MensaplanRecycleAdapter";
    private ArrayList<MensaplanMeal> data;

    public MensaplanRecycleAdapter(ArrayList<MensaplanMeal> data) {
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mensaplan_item, parent, false);
        TextView txtDescription = (TextView) view.findViewById(R.id.mealDescription);
        TextView txtPrice = (TextView) view.findViewById(R.id.mealPrice);
        TextView txtAdditives = (TextView) view.findViewById(R.id.mealAdditives);

        ViewHolder holder = new ViewHolder(view);

        holder.mAdditives = txtAdditives;
        holder.mDescription = txtDescription;
        holder.mPrice = txtPrice;

        return holder;
    }


    public void add(MensaplanMeal mensaplanMeal,int position) {
        position = position == -1 ? getItemCount()  : position;
        data.add(position, mensaplanMeal);
        notifyItemInserted(position);
    }

    public void remove(int position){
        if (position < getItemCount()  ) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder myHolder = (ViewHolder) holder;
            myHolder.mDescription.setText(data.get(position).getDescription());
            myHolder.mPrice.setText(data.get(position).getPrice());
            if(data.get(position).getAdditives().isEmpty()) {
                myHolder.mAdditives.setHeight(0);
            } else {
                myHolder.mAdditives.setText(data.get(position).getAdditives());
            }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mDescription;
        public TextView mPrice;
        public TextView mAdditives;
        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

    }


}
