package com.shan.volumecontrol;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;

public class FrameLayoutActivity extends Activity 
implements ActionBar.OnNavigationListener
{
    private static final String     TAG = FrameLayoutActivity.class.getSimpleName();
    private static final String     CURR_FRAGMENT_INDEX = "selected_navigation_item";
    private static final String[]   m_TitleArr = {"Control Volume", "View Volume"};
    private int                     m_CurrFragmentIndex;
    
    @Override
    protected void onCreate(Bundle save_instance_state)
    {
        Log.d(TAG, "onCreate");        
        super.onCreate(save_instance_state);
        setContentView(R.layout.control_volume_framework);
        
        // Set up action bar to show a list of two fragments
        final ActionBar action_bar = getActionBar();
        action_bar.setDisplayShowTitleEnabled(false);
        action_bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        action_bar.setListNavigationCallbacks
        (
            new ArrayAdapter<String>
            (
                action_bar.getThemedContext(), 
                android.R.layout.simple_list_item_1, 
                android.R.id.text1, 
                m_TitleArr
            ), 
            this
        );       
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        return true;
    }

    
    @Override
    public void onRestoreInstanceState(Bundle save_instance)
    {
        Log.d(TAG, "onRestoreInstance");
        if (save_instance.containsKey(CURR_FRAGMENT_INDEX))
        {
            m_CurrFragmentIndex = 
                    save_instance.getInt(CURR_FRAGMENT_INDEX); 
            
            getActionBar().setSelectedNavigationItem(m_CurrFragmentIndex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle out_state)
    {
        Log.d(TAG, "onSaveInstance");        
        out_state.putInt
        (
            CURR_FRAGMENT_INDEX, 
            m_CurrFragmentIndex
        );
    }    

    @Override
    protected void onStart()
    {
        Log.d(TAG, "onStart");        
        SharedPreferences pref = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        m_CurrFragmentIndex = pref.getInt(CURR_FRAGMENT_INDEX, m_CurrFragmentIndex);
        getActionBar().setSelectedNavigationItem(m_CurrFragmentIndex);

        
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop");
        SharedPreferences.Editor pref_ed = 
                getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
                
        pref_ed.putInt(CURR_FRAGMENT_INDEX, m_CurrFragmentIndex);
        pref_ed.commit();

        super.onStop();
    }

    
    
    
    
    @Override
    public boolean onNavigationItemSelected(int pos, long id)
    {
        if (0 == pos || 1 == pos)
        {
            Fragment fragment = null;
            
            // VolumeControl view.
            if (0 == pos)
            {
                fragment    = new SetVolumeFragment();
            }
            // View Volume
            else
            {
                fragment = new ViewVolumeFragment();
            }

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.control_vol_framework, fragment);
            transaction.commit();
            m_CurrFragmentIndex = pos;
            
            return true;
        }
        else
        {
            throw new IndexOutOfBoundsException("NavigationBar does not have index " + Integer.toString(pos));
        }
    }

}
