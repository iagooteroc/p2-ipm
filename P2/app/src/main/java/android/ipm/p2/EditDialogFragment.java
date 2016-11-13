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

//Diálogo de de Edit
public class EditDialogFragment extends DialogFragment {

    private String text="Default";
    private int selected;

    public void setText(String text){
        this.text = text;
    }

    public void setSelected(int selected){
        this.selected = selected;
    }

    public interface EditDialogListener {
        void onEditPositiveClick(String value, int selected);
    }
    //Comunicador deEditDialogFragment con MainActivity
    EditDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (EditDialogListener) getTargetFragment();
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
        final View vie = inflater.inflate(R.layout.edit_dialog_fragment, null);
        EditText editText = (EditText) vie.findViewById(R.id.edit_name);
        editText.setText(text);
        builder.setView(vie)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog editDialog = (Dialog) dialog;
                        EditText editText = (EditText) editDialog.findViewById(R.id.edit_name);
                        String value = editText.getText().toString();
                        mListener.onEditPositiveClick(value, selected);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }


}