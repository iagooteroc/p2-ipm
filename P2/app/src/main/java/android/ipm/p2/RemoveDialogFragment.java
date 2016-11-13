package android.ipm.p2;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;

public class RemoveDialogFragment extends DialogFragment {

    private SparseBooleanArray selected;
    private boolean firstTime = true;

    public void setSelected(SparseBooleanArray selected) {
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

        int n = 0;
        if (selected != null) {
            int n_selected = selected.size();
            // Cuenta el número de elementos seleccionados para mostrar el correspondiente mensaje de aviso
            for (int i = 0; i < n_selected; i++) {
                if (selected.valueAt(i)) {
                    n++;
                }
            }
        }
        if (n > 1) {
            builder.setMessage(getString(R.string.delete_warning_plu1) + " " + n + " " + getString(R.string.delete_warning_plu2));
        } else {
            builder.setMessage(R.string.delete_warning_sing);
        }

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
