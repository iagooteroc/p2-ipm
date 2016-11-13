package android.ipm.p2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

//Diálogo de de Add
public class AddDialogFragment extends DialogFragment {
    public interface AddDialogListener {
         void onAddPositiveClick(String value);
    }
    AddDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (AddDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddDialogListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("DISMISS", true);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if ((savedInstanceState != null) && (savedInstanceState.containsKey("DISMISS"))
                && (savedInstanceState.getBoolean("DISMISS")))
            this.dismissAllowingStateLoss();
    }

    //Lo que hace cuando se crea el diálogo(Cuando se abre la ventanita)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View vie = inflater.inflate(R.layout.add_dialog_fragment, null);
        builder.setView(vie)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog addDialog = (Dialog) dialog;
                        EditText editText = (EditText) addDialog.findViewById(R.id.add_name);
                        String value = editText.getText().toString();
                        mListener.onAddPositiveClick(value);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }


}
