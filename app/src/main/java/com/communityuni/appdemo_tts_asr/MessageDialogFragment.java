package com.communityuni.appdemo_tts_asr;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

public class MessageDialogFragment extends AppCompatDialogFragment {
    public interface Listener{
        void onMessageDialogDismissed();

    }

    private static final String ARG_MESSAGE = "message";
    public static MessageDialogFragment newInstance(String message){
        MessageDialogFragment fragment= new MessageDialogFragment();
        Bundle args= new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;

    }
    public Dialog onCreateDialog(Bundle savedInstanceState){
        return new AlertDialog.Builder(getContext())
                .setMessage(getArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Listener) getActivity()).onMessageDialogDismissed();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        ((Listener) getActivity()).onMessageDialogDismissed();
                    }
                })
                .create();

    }


}
