package com.example.musicapp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.example.musicapp.R;

public class DeleteSongDialogFragment extends DialogFragment {

    private OnPositiveButtonClickListener mOnPositiveButtonClickListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), 4); // dark theme

        builder.setCancelable(false)
                .setTitle(R.string.delete_song_dialog)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnPositiveButtonClickListener != null) {
                            mOnPositiveButtonClickListener.setOnPositiveButtonClickListener();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public void setOnPositiveButtonClickListener(
            OnPositiveButtonClickListener onPositiveButtonClickListener) {
        mOnPositiveButtonClickListener = onPositiveButtonClickListener;
    }

    public interface OnPositiveButtonClickListener {
        void setOnPositiveButtonClickListener();
    }

}
