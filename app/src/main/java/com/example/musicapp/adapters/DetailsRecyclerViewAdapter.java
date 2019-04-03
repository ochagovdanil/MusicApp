package com.example.musicapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.musicapp.R;
import com.example.musicapp.models.Detail;

import java.util.ArrayList;
import java.util.List;

public class DetailsRecyclerViewAdapter
        extends RecyclerView.Adapter<DetailsRecyclerViewAdapter.DetailsViewHolder> {

    private Context mContext;
    private List<Detail> mListDetails = new ArrayList<>();

    public DetailsRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public DetailsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(
                R.layout.item_detail,
                viewGroup,
                false);
        return new DetailsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsViewHolder detailsViewHolder, int i) {
        detailsViewHolder.mHeadline.setText(mListDetails.get(i).getHeadLine());
        detailsViewHolder.mDescription.setText(mListDetails.get(i).getDescription());
    }

    @Override
    public int getItemCount() {
        return mListDetails.size();
    }

    public void addDetail(Detail detail) {
        mListDetails.add(detail);
    }

    class DetailsViewHolder extends RecyclerView.ViewHolder {

        private TextView mHeadline, mDescription;

        DetailsViewHolder(@NonNull View itemView) {
            super(itemView);

            mHeadline = itemView.findViewById(R.id.item_detail_headline);
            mDescription = itemView.findViewById(R.id.item_detail_description);
        }

    }

}
