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

public class VolumeControlActivity extends Activity 
implements ActionBar.OnNavigationListener
{
    private static final String     TAG = VolumeControlActivity.class.getSimpleName();
    private static final String     CURR_FRAGMENT_INDEX = "selected_navigation_item";
    private static final int        CTRL_VOL_PAGE = 0;
    private static final int        VIEW_VOL_PAGE = 1;
    private int                     m_CurrFragmentIndex;
    
    @Override
    protected void onCreate(Bundle save_instance_state)
    {
        Log.d(TAG, "onCreate");        
        super.onCreate(save_instance_state);
        setContentView(R.layout.control_volume_activity);
        
        
        String[] title_arr = 
                        {
                            getString(R.string.vol_ctrl), 
                            getString(R.string.vol_view)
                        }; 
        
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
                title_arr
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
        Fragment f = null;
        
        switch (pos)
        {
        case CTRL_VOL_PAGE:
            f = new SetVolumeFragment();
            break;
        case VIEW_VOL_PAGE:
            f = new ViewVolumeFragment();
            break;
        default:
            assert(true);
            return false;
        };
        
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.control_vol_framework, f);
        transaction.commit();
        m_CurrFragmentIndex = pos;
        
        return true;
    }

}
