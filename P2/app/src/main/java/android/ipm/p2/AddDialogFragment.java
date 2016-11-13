package android.ipm.p2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

//Diálogo de de Add
public class AddDialogFragment extends DialogFragment {
    public interface AddDialogListener {
         void onAddPositiveClick(String value);
    }
    //Comunicador de AddDialogFragment con MainActivity
    AddDialogListener mListener;

    //Lo que hace cuando se añade la vista
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AddDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AddDialogListener");
        }
    }

    //Lo que hace cuando se crea el diálogo(Cuando se abre la ventanita)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View vie = inflater.inflate(R.layout.add_dialog_fragment, null);
        builder.setView(vie)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) vie.findViewById(R.id.add_name);
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
