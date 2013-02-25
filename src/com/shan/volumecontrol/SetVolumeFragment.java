package com.shan.volumecontrol;

import com.shan.volumecontrol.R;

import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class SetVolumeFragment 
extends     Fragment 
implements  OnSeekBarChangeListener, 
            OnClickListener
{
    private static final String TAG = SetVolumeFragment.class.getSimpleName(); 
    private static final String VOL_CTRL_SERVICE = VolumeControlService.class.getName();
    
    public static final String TAG_EXT = TAG;
    public static final String HEADSET_VOL = "HeadSet_Volume";
    public static final String SPEAKER_VOL = "Speaker_Volume";
    public static final String IS_SET_ALL_STREAM = "Is_Set_Ringer";
    
    private SeekBar m_HeadSetSeek, m_SpeakerSeek;
    private Switch  m_Switch;
    private int     m_HeadSetVol, m_SpeakerVol;
    private boolean m_IsOn; 
    private boolean m_IsSetAllStream; 
    private int     m_MenuTextId;
    private Activity m_AttachActivity = null;
    private VolumeManager m_VolMgr = null;
    
    
    @Override
    public void onAttach(Activity activity)
    {
        Log.d(TAG, "onAttach");
        m_AttachActivity = activity;
        m_VolMgr = new VolumeManager(activity);
        
        super.onAttach(activity);
    }

    

    @Override
    public void onCreate(Bundle bundle)
    {
        Log.d(TAG, "onCreate");
        restoreInstance(getActivity(), bundle);
        // workaround for menu text - opPrepareOptionMenu is called first 
        // before onActivityCreated(), hence we need to set the menu id here
        // first
        m_MenuTextId = (m_IsSetAllStream) 
                        ? 
                        R.string.menu_set_all_stream : R.string.menu_set_media_stream;
        setHasOptionsMenu(true);
        super. onCreate(bundle);
    }


    @Override
    public View onCreateView
    (
        LayoutInflater  inflater, 
        ViewGroup       container,
        Bundle          saved_instance
    )
    {
        Log.d(TAG, "onCreateView");
        View view = 
                inflater.inflate(R.layout.activity_set_volume, container, false);
        
        m_Switch        = (Switch)view.findViewById(R.id.switch_on);
        m_HeadSetSeek   = (SeekBar)view.findViewById(R.id.headset_control);
        m_SpeakerSeek   = (SeekBar)view.findViewById(R.id.speaker_control);
                       
        m_HeadSetSeek.setOnSeekBarChangeListener(this);
        m_SpeakerSeek.setOnSeekBarChangeListener(this);
        m_Switch.setOnClickListener(this);
              
        return view;
    }

    
    @Override
    public void onActivityCreated(Bundle bundle)
    {
        Log.d(TAG, "onActivityCreated");
        int max_vol = m_VolMgr.getMaxStreamVol(); 
        
        m_HeadSetSeek.setMax(max_vol);
        m_HeadSetSeek.setProgress(m_HeadSetVol);
        m_SpeakerSeek.setMax(max_vol);
        m_SpeakerSeek.setProgress(m_SpeakerVol);
           
        super.onActivityCreated(bundle);
    }
      
    
    @Override
    public void onStart()
    {
        Log.d(TAG, "onStart");
        
        // activity may have bben nulled on onStop() call
        if (null == m_AttachActivity)
            m_AttachActivity = getActivity();
        
        setVolumeControlService(isServiceRunning());
        setStreamControl(m_IsSetAllStream);
        
        super.onStart();
    }

        
    @Override
    public void onStop()
    {
        Log.d(TAG, "onStop");
        
        SharedPreferences.Editor pref_ed = 
                this.getActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
                
        pref_ed.putBoolean(IS_SET_ALL_STREAM, m_IsSetAllStream);
        pref_ed.putInt(HEADSET_VOL, m_HeadSetVol);
        pref_ed.putInt(SPEAKER_VOL, m_SpeakerVol);
        pref_ed.commit();

        m_AttachActivity = null;
        
        super.onStop();
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater menu_inflater)
    {
        menu_inflater.inflate(R.menu.activity_set_volume, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
        case R.id.menu_set_ringer:
            //set the opposite boolean value as we want the mew value 
            // after the press, not before it
            setStreamControl(!item.isChecked());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onPrepareOptionMenu");
        
        MenuItem item =  menu.findItem(R.id.menu_set_ringer); 
        item.setTitle(m_MenuTextId);
        item.setChecked(m_IsSetAllStream);
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle out_state)
    {
        Log.d(TAG, "Saving instance");
        out_state.putInt(HEADSET_VOL, m_HeadSetVol);
        out_state.putInt(SPEAKER_VOL, m_SpeakerVol);
        out_state.putBoolean(IS_SET_ALL_STREAM, m_IsSetAllStream);
        super.onSaveInstanceState(out_state);
    }

    private void restoreInstance(Activity activity, Bundle bundle)
    {
        AudioManager audio_mgr = 
                (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
        
        int default_music_vol = 
                audio_mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        
        m_IsSetAllStream = false;
        m_HeadSetVol = default_music_vol;
        m_SpeakerVol = default_music_vol;
        
        if (null != bundle)
        {
            m_IsSetAllStream = bundle.getBoolean(IS_SET_ALL_STREAM, m_IsSetAllStream);
            m_HeadSetVol = bundle.getInt(HEADSET_VOL, m_HeadSetVol);
            m_SpeakerVol = bundle.getInt(SPEAKER_VOL, m_SpeakerVol);
        }
        else
        {
            SharedPreferences pref = activity.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            m_IsSetAllStream = pref.getBoolean(IS_SET_ALL_STREAM, false);
            m_HeadSetVol = pref.getInt(HEADSET_VOL, m_HeadSetVol);
            m_SpeakerVol = pref.getInt(SPEAKER_VOL, m_SpeakerVol);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seek_bar, int progress,
            boolean from_user)
    {
        if (from_user)
        {
            switch (seek_bar.getId())
            {
            case R.id.headset_control:
                m_HeadSetVol = progress;
                break;
            case R.id.speaker_control:
                m_SpeakerVol = progress;
                break;
            };
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
        //Do nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        updateVolumes();
    }
    
    private boolean isServiceRunning()
    {
        ActivityManager mgr = 
                (ActivityManager)m_AttachActivity.getSystemService(Context.ACTIVITY_SERVICE);
               
        for (RunningServiceInfo service : mgr.getRunningServices(Integer.MAX_VALUE))
        {
            if (VOL_CTRL_SERVICE.equals(service.service.getClassName()))
                return true;
        }
        return false;
    }
    
    private void setVolumeControlService(boolean is_on)
    {
        m_IsOn = is_on;
        m_Switch.setChecked(m_IsOn);
        setControlOnOff(m_IsOn);
        
        if (m_IsOn)
        {
            updateVolumes();
        }
        else
        {
            Log.d(TAG, "stopping volvum service from actiivty");
            Intent intent = new Intent(m_AttachActivity, VolumeControlService.class);
            m_AttachActivity.stopService(intent);
        }
    }
    
    private void setControlOnOff(boolean is_on)
    {
        // This MUST be a view group
        ViewGroup vg = (ViewGroup)getView().findViewById(R.id.activity_layout);
        vg.setEnabled(is_on);
        for (int i = 0; i < vg.getChildCount(); ++i)
        {
            vg.getChildAt(i).setEnabled(is_on);
        }
    }
    
    private void setStreamControl(boolean is_all_stream)
    {
        // if the user changed the status, update the menu title to 
        // reflect the current status
        
        TextView all_text = (TextView)getView().findViewById(R.id.word_all);
        TextView media_text = (TextView)getView().findViewById(R.id.word_media);
        
        m_IsSetAllStream = is_all_stream;
        Log.d(TAG, "IsEtAllStream is" + Boolean.toString(m_IsSetAllStream));
        if (m_IsSetAllStream)
        {
            all_text.setTextColor(getResources().getColor((R.color.text_selected)));
            media_text.setTextColor(getResources().getColor((R.color.text_unselected)));
            m_MenuTextId = R.string.menu_set_all_stream;
        }
        else
        {
            all_text.setTextColor(getResources().getColor((R.color.text_unselected)));
            media_text.setTextColor(getResources().getColor((R.color.text_selected)));
            m_MenuTextId = R.string.menu_set_media_stream;
        }
        
        updateVolumes();
        m_AttachActivity.invalidateOptionsMenu();

        
    }
    
    private void updateVolumes()
    {
        Intent intent = 
                new Intent(m_AttachActivity, VolumeControlService.class);
        
        intent.putExtra(HEADSET_VOL, m_HeadSetVol);
        intent.putExtra(SPEAKER_VOL, m_SpeakerVol);
        intent.putExtra(IS_SET_ALL_STREAM, m_IsSetAllStream);
        m_AttachActivity.startService(intent);
         
    }
    
    
    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.switch_on)
            setVolumeControlService(m_Switch.isChecked());
    }
}
