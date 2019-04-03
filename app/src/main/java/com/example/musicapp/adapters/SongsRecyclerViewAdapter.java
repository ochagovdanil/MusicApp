package com.example.musicapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.musicapp.R;
import com.example.musicapp.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongsRecyclerViewAdapter
        extends RecyclerView.Adapter<SongsRecyclerViewAdapter.SongsViewHolder> {

    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private List<Song> mList = new ArrayList<>();

    public SongsRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(
                R.layout.item_song,
                viewGroup,
                false);
        return new SongsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongsViewHolder songsViewHolder, int i) {
        songsViewHolder.mImage.setImageBitmap(mList.get(i).getImage());
        songsViewHolder.mTextTitle.setText(mList.get(i).getTitle());
        songsViewHolder.mTextArtist.setText(mList.get(i).getArtist());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addSong(Song song) {
        mList.add(song);
    }

    public Song getSong(int index) {
        return mList.get(index);
    }

    public void deleteSong(int index) {
        mList.remove(index);
        notifyDataSetChanged();
    }

    class SongsViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextTitle, mTextArtist;
        private ImageView mImage;
        private RelativeLayout mLayout;

        SongsViewHolder(@NonNull View itemView) {
            super(itemView);

            mLayout = itemView.findViewById(R.id.item_song_layout);
            mTextTitle = itemView.findViewById(R.id.text_item_song_title);
            mTextArtist = itemView.findViewById(R.id.text_item_song_artist);
            mImage = itemView.findViewById(R.id.image_button_item_song);

            mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClickListener(mList.get(
                                getAdapterPosition()),
                                getAdapterPosition());
                    }
                }
            });
        }

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClickListener(Song song, int position);
    }

}
