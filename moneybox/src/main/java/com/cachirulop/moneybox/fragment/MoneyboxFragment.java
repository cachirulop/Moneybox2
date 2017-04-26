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

package com.cachirulop.moneybox.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cachirulop.moneybox.R;
import com.cachirulop.moneybox.activity.IMoneyboxListener;
import com.cachirulop.moneybox.activity.MainActivity;
import com.cachirulop.moneybox.entity.CurrencyValueDef;
import com.cachirulop.moneybox.entity.Movement;
import com.cachirulop.moneybox.manager.CurrencyManager;
import com.cachirulop.moneybox.manager.MovementsManager;
import com.cachirulop.moneybox.manager.SoundsManager;
import com.cachirulop.moneybox.manager.VibratorManager;

public class MoneyboxFragment
        extends Fragment
{
    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState)
    {
        return inflater.inflate (R.layout.moneybox_tab,
                                 container,
                                 false);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState)
    {
        initActivity ();
        updateTotal ();

        registerLayoutListener ();

        super.onActivityCreated (savedInstanceState);
    }

    /**
     * Register the event OnGlobalLayoutListener to fill the moneybox when the
     * layout is created.
     */
    private void registerLayoutListener ()
    {
        final View v;
        final ViewTreeObserver vto;
        Activity parent;

        parent = getActivity ();

        v = parent.findViewById (R.id.moneyDropLayout);
        vto = v.getViewTreeObserver ();
        if (vto.isAlive ()) {
            vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener ()
            {
                public void onGlobalLayout ()
                {
                    initMoneybox ();

                    v.getViewTreeObserver ().removeGlobalOnLayoutListener (this);
                }
            });
        }
    }

    /**
     * Initialize the activity creating the list of buttons with the coins and
     * bills.
     */
    private void initActivity ()
    {
        String currencyName;

        currencyName = getResources ().getString (R.string.currency_name);

        CurrencyManager.initCurrencyDefList (currencyName);
        addButtons ();
    }

    /**
     * Launched when a coin or a bill is clicked to be inserted in the moneybox.
     * 
     * Drop the clicked element inside the moneybox and updates the total
     * amount.
     * 
     * @param v
     *            View that launch the event.
     */
    protected void onMoneyClicked (View v)
    {
        CurrencyValueDef value;

        value = (CurrencyValueDef) v.getTag ();
        if (value != null) {
            MainActivity parent;
            Movement result;

            parent = (MainActivity) getActivity ();

            result = MovementsManager.insertMovement (parent.getCurrentMoneybox (),
                                                      value.getAmount ());
            dropMoney (v,
                       result);

            updateTotal ();
            refreshMovements ();
        }
    }

    /**
     * Move an image of the money like it was dropping inside the money box
     * 
     * @param src
     *            Image of the money to drop
     * @param m
     *            Movement with the value of the money to drop
     */

    private void dropMoney (View src,
                            Movement m)
    {
        HorizontalScrollView scroll;

        scroll = (HorizontalScrollView) getActivity ().findViewById (R.id.scrollButtonsView);

        dropMoney (src.getLeft () - scroll.getScrollX (),
                   src.getRight (),
                   m);
    }

    /**
     * Drop money from the top of the layout to the bottom simulating that a
     * coin or bill is inserted in the moneybox.
     * 
     * @param leftMargin
     *            Left side of the coin/bill
     * @param width
     *            Width of the image to slide down
     * @param m
     *            Movement with the value of the money to drop
     */
    protected void dropMoney (int leftMargin,
                              int width,
                              Movement m)
    {
        ImageView money;
        AnimationSet moneyDrop;
        RelativeLayout layout;
        RelativeLayout.LayoutParams lpParams;
        Rect r;
        Activity parent;
        CurrencyValueDef curr;

        parent = getActivity ();

        curr = CurrencyManager.getCurrencyDef (Math.abs (m.getAmount ()));
        r = curr.getDrawable ().getBounds ();

        money = new ImageView (parent);
        money.setVisibility (View.INVISIBLE);
        money.setImageDrawable (curr.getDrawable ().getConstantState ().newDrawable ());
        money.setTag (curr);
        money.setId ((int) m.getIdMovement ());

        layout = findLayout ();

        lpParams = new RelativeLayout.LayoutParams (r.width (),
                                                    r.height ());
        lpParams.leftMargin = leftMargin;
        lpParams.rightMargin = layout.getWidth () - (leftMargin + width);
        lpParams.topMargin = 0;
        lpParams.bottomMargin = r.height ();

        layout.addView (money,
                        lpParams);

        moneyDrop = createDropAnimation (money,
                                         layout,
                                         curr);
        money.setVisibility (View.VISIBLE);

        SoundsManager.playMoneySound (curr.getType ());
        VibratorManager.vibrateMoneyDrop (curr.getType ());

        money.startAnimation (moneyDrop);
    }

    /**
     * Create dynamically an android animation for a coin or a bill droping into
     * the moneybox.
     * 
     * @param img
     *            ImageView to receive the animation
     * @param layout
     *            Layout that paint the image
     * @param curr
     *            Currency value of the image
     * @return Set of animations to apply to the image
     */
    private AnimationSet createDropAnimation (ImageView img,
                                              View layout,
                                              CurrencyValueDef curr)
    {
        AnimationSet result;

        result = new AnimationSet (false);
        result.setFillAfter (true);

        // Fade in
        AlphaAnimation fadeIn;

        fadeIn = new AlphaAnimation (0.0f,
                                     1.0f);
        fadeIn.setDuration (300);
        result.addAnimation (fadeIn);

        // drop
        TranslateAnimation drop;
        int bottom;

        bottom = Math.abs (layout.getHeight () - img.getLayoutParams ().height);
        drop = new TranslateAnimation (1.0f,
                                       1.0f,
                                       1.0f,
                                       bottom);
        drop.setStartOffset (300);
        drop.setDuration (1500);

        if (curr.getType () == CurrencyValueDef.MoneyType.COIN) {
            drop.setInterpolator (new BounceInterpolator ());
        }
        else {
            // drop.setInterpolator(new DecelerateInterpolator(0.7f));
            drop.setInterpolator (new AnticipateOvershootInterpolator ());
        }

        result.addAnimation (drop);

        return result;
    }

    /**
     * Create dynamically an android animation for a coin or a bill getting from
     * the moneybox.
     * 
     * @param img
     *            ImageView to receive the animation
     * @param layout
     *            Layout that paint the image
     * @return Set of animations to apply to the image
     */
    private AnimationSet createGetAnimation (ImageView img,
                                             View layout)
    {
        AnimationSet result;

        result = new AnimationSet (false);
        result.setFillAfter (true);

        // get
        TranslateAnimation get;
        int bottom;

        bottom = Math.abs (layout.getHeight () - img.getLayoutParams ().height);
        get = new TranslateAnimation (1.0f,
                                      1.0f,
                                      bottom,
                                      1.0f);
        get.setDuration (1500);
        result.addAnimation (get);

        // Fade out
        AlphaAnimation fadeOut;

        fadeOut = new AlphaAnimation (1.0f,
                                      0.0f);
        fadeOut.setDuration (300);
        fadeOut.setStartOffset (1500);

        result.addAnimation (fadeOut);

        return result;
    }

    /**
     * Create dynamically an android animation for a coin or a bill deleted from
     * the moneybox.
     * 
     * @param img
     *            ImageView to receive the animation
     * @param layout
     *            Layout that paint the image
     * @return Set of animations to apply to the image
     */
    private AnimationSet createDeleteAnimation (ImageView img,
                                                View layout)
    {
        AnimationSet result;

        result = new AnimationSet (false);
        result.setFillAfter (true);

        // Fade out
        AlphaAnimation fadeOut;

        fadeOut = new AlphaAnimation (1.0f,
                                      0.0f);
        fadeOut.setStartOffset (300);
        fadeOut.setDuration (300);

        result.addAnimation (fadeOut);

        return result;
    }

    /**
     * Update the total amount using the main tab activity.
     */
    private void updateTotal ()
    {
        ((IMoneyboxListener) getActivity ()).onUpdateTotal ();
    }

    /**
     * Send a request to the activity to refresh the movement list
     */
    private void refreshMovements ()
    {
        ((IMoneyboxListener) getActivity ()).onUpdateMovements ();
    }

    /**
     * Set a value to the total field using the main tab activity.
     * 
     * @param val
     *            Value to be painted in the total.
     */
    protected void setTotal (double val)
    {
        ((IMoneyboxListener) getActivity ()).onSetTotal (val);
    }

    /**
     * Add the currency buttons dynamically from the money_defs.xml file
     */
    private void addButtons ()
    {
        ArrayList<CurrencyValueDef> currencies;
        LinearLayout buttons;
        Activity parent;

        parent = getActivity ();

        buttons = (LinearLayout) parent.findViewById (R.id.moneyButtonsLayout);

        currencies = CurrencyManager.getCurrencyDefList ();

        View.OnClickListener listener;

        listener = new View.OnClickListener ()
        {
            public void onClick (View v)
            {
                onMoneyClicked (v);
            }
        };

        for (CurrencyValueDef c : currencies) {
            ImageView v;

            v = new ImageView (parent);
            v.setOnClickListener (listener);
            v.setImageDrawable (c.getDrawable ());
            v.setLongClickable (true);
            v.setTag (c);

            buttons.addView (v);
        }
    }

    /**
     * Fill the moneybox with all the movements dropping coins randomly
     */
    public void fillMoneybox ()
    {
        RelativeLayout layout;
        int maxWidth;
        ArrayList<Movement> lstMoney;
        Random rndLeft;
        double total;
        int i;
        MainActivity parent;

        parent = (MainActivity) getActivity ();

        layout = findLayout ();
        maxWidth = layout.getWidth ();
        if (maxWidth == 0) {
            // The layout is not initialized
            return;
        }

        total = 0.0;
        i = 0;

        rndLeft = new Random ();
        lstMoney = MovementsManager.getActiveMovements (parent.getCurrentMoneybox ());
        for (Movement m : lstMoney) {
            Rect r;
            CurrencyValueDef curr;
            int left;

            curr = CurrencyManager.getCurrencyDef (Math.abs (m.getAmount ()));
            if (curr != null) {
                r = curr.getDrawable ().getBounds ();

                left = rndLeft.nextInt (maxWidth - r.width ());

                total += m.getAmount ();

                MoneyTimerTask task;

                task = new MoneyTimerTask (this,
                                           m,
                                           left,
                                           r.width (),
                                           total);

                layout.postDelayed (task,
                                    400 * i);
            }

            i++;
        }
    }

    /**
     * Initialize the activity filling the window with the coins and bills that
     * are inside the moneybox. Also center the coins and bills list to show the
     * middle item.
     */
    public void initMoneybox ()
    {
        fillMoneybox ();

        HorizontalScrollView scroll;
        int offsetX;
        List<CurrencyValueDef> currList;
        int elemWidth;

        currList = CurrencyManager.getCurrencyDefList ();
        scroll = (HorizontalScrollView) getActivity ().findViewById (R.id.scrollButtonsView);

        elemWidth = currList.get (0).getDrawable ().getBounds ().right;
        offsetX = ((currList.size () * elemWidth) / 2) - (elemWidth / 2);
        scroll.scrollTo (offsetX,
                         0);
    }

    /**
     * Remove the coins and bills inside the moneybox and refill it.
     */
    public void refresh ()
    {
        findLayout ().removeAllViews ();
        fillMoneybox ();
    }

    /**
     * Get a money from the moneybox making an animation.
     * 
     * @param m
     *            Movement to be got
     */
    public void getMovement (Movement m)
    {
        ImageView money;

        money = findMovementImage (m);
        if (money != null) {
            AnimationSet moneyGet;
            moneyGet = createGetAnimation (money,
                                           findLayout ());

            money.startAnimation (moneyGet);
        }
    }

    /**
     * Delete a movement from the moneybox making an animation.
     * 
     * @param m
     *            Movement to be deleted.
     */
    public void deleteMovement (Movement m)
    {
        ImageView money;

        money = findMovementImage (m);
        if (money != null) {
            AnimationSet moneyDelete;

            moneyDelete = createDeleteAnimation (money,
                                                 findLayout ());

            money.startAnimation (moneyDelete);
        }
    }

    /**
     * Drop again a movement in the moneybox making an animation.
     * 
     * @param m
     *            Movement to be dropped.
     */
    public void dropAgainMovement (Movement m)
    {
        ImageView v;
        CurrencyValueDef c;
        
        // TODO: This doesn't work!!!!!!
        
        c = CurrencyManager.getCurrencyDef (Math.abs (m.getAmount ()));

        v = new ImageView (getActivity ());
        v.setLeft (findLayout ().getWidth () / 2);
        v.setTop (0);
        v.setImageDrawable (c.getDrawable ());
        v.setTag (c);
        
        dropMoney (v, m);
    }

    /**
     * Finds the image of a specific movement in the images created when the
     * movement is inserted.
     * 
     * @param m
     *            Movement to find
     * 
     * @return The ImageView that represents the movement in the moneybox or
     *         null if the movement doesn't exist.
     */
    private ImageView findMovementImage (Movement m)
    {
        return (ImageView) findLayout ().findViewById ((int) m.getIdMovement ());
    }

    /**
     * Find the main Layout of the fragment
     * 
     * @return The main layout of the fragment
     */
    private RelativeLayout findLayout ()
    {
        return (RelativeLayout) getActivity ().findViewById (R.id.moneyDropLayout);
    }
}

/**
 * Class that implements a task that drop the coin or bill inside the moneybox
 * in an independent thread.
 * 
 * @author dmagro
 */
final class MoneyTimerTask
        implements Runnable
{
    MoneyboxFragment _parent;
    Movement         _movement;
    int              _left;
    int              _width;
    double           _total;

    /**
     * Creates new object with then necessary values to launch the coin or the
     * bill inside the moneybox.
     * 
     * @param parent
     *            Activity to drop the money
     * @param currency
     *            Currency to be dropped
     * @param left
     *            Left coordinate of the money inside the layout
     * @param width
     *            With of the image that paint the money
     * @param total
     *            Total to be painted in the total layout.
     */
    public MoneyTimerTask (MoneyboxFragment parent,
                           Movement movement,
                           int left,
                           int width,
                           double total)
    {
        _parent = parent;
        _movement = movement;
        _left = left;
        _width = width;
        _total = total;
    }

    /**
     * Drop the money in the moneybox and update the total amount.
     */
    public void run ()
    {
        if (_parent.getActivity () != null) {
            _parent.getActivity ()
                   .runOnUiThread (new Runnable ()
                   {
                       public void run ()
                       {
                           _parent.dropMoney (_left, _width, _movement);
                           _parent.setTotal (_total);
                       }
                   });
        }
    }

}
