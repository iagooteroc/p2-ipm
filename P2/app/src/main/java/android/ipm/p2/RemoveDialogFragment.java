package android.ipm.p2;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class RemoveDialogFragment extends DialogFragment {

    private SparseBooleanArray selected;

    public void setSelected(SparseBooleanArray selected){
        this.selected = selected.clone();
    }

    public interface RemoveDialogListener {
        void onRemovePositiveClick(SparseBooleanArray selected);
    }
    //Comunicador deRemoveDialogFragment con MainActivity
    RemoveDialogFragment.RemoveDialogListener mListener;


    //Lo que hace cuando se añade la vista
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (RemoveDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RemoveDialogListener");
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
        final View vie = inflater.inflate(R.layout.remove_dialog_fragment, null);
        TextView editText = (TextView) vie.findViewById(R.id.remove_text);

        int n = 0;
        int n_selected = selected.size();
        // Cuenta el número de elementos seleccionados para mostrar el correspondiente mensaje de aviso
        for (int i = 0; i < n_selected; i++) {
            if (selected.valueAt(i)){
                n++;
            }
        }
        if (n > 1) {
            editText.setText(getString(R.string.delete_warning_plu1) + " " +  n + " " + getString(R.string.delete_warning_plu2));
            //builder.setMessage(getString(R.string.delete_warning_plu1) + " " +  n + " " + getString(R.string.delete_warning_plu2));
        } else {
            editText.setText(R.string.delete_warning_sing);
            //builder.setMessage(R.string.delete_warning_sing);
        }

        builder.setView(vie)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onRemovePositiveClick(selected);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RemoveDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
