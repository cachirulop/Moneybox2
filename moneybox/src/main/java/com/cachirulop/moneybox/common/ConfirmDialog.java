/*******************************************************************************
 *   Copyright (c) 2012 David Magro Martin.
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the GNU Public License v3.0
 *   which accompanies this distribution, and is available at
 *   http://www.gnu.org/licenses/gpl.html
 *   
 * Contributors:
 *       David Magro Martin
 *******************************************************************************/

package com.cachirulop.moneybox.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

/**
 * helper for Confirm-Dialog creation
 */
public abstract class ConfirmDialog
        extends DialogFragment
        implements View.OnClickListener
{
    private int _titleId;
    private int _messageId;

    public ConfirmDialog (int titleId,
                          int messageId)
    {
        super ();

        _titleId = titleId;
        _messageId = messageId;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState)
    {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder (getActivity ());
        builder.setTitle (_titleId);
        builder.setMessage (_messageId);

        builder.setPositiveButton (android.R.string.ok,
                                   new DialogInterface.OnClickListener ()
                                   {
                                       public void onClick (DialogInterface dialog,
                                                            int which)
                                       {
                                           // Do nothing here because we
                                           // override this button later to
                                           // change the close behaviour.
                                           // However, we still need this
                                           // because on older versions of
                                           // Android unless we
                                           // pass a handler the button doesn't
                                           // get instantiated
                                       }
                                   });

        builder.setNegativeButton (android.R.string.cancel,
                                   null);

        return builder.create ();
    }

    @Override
    public void onStart ()
    {
        super.onStart ();

        AlertDialog dialog;

        dialog = (AlertDialog) getDialog ();
        if (dialog != null) {
            Button positive;

            positive = (Button) dialog.getButton (Dialog.BUTTON_POSITIVE);

            positive.setOnClickListener (this);
        }
    }

    public void onClick (View v)
    {
        onOkClicked ();
        ((AlertDialog) getDialog ()).dismiss ();
    }

    /**
     * called when "ok" pressed.
     * 
     * @param input
     */
    abstract public void onOkClicked ();
}