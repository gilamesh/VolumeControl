package com.shan.volumecontrol;

import com.shan.volumecontrol.R;

import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    
    private SeekBar     m_HeadSetSeek, m_SpeakerSeek;
    private Switch      m_Switch;
    private ImageView   m_MediaIcon, m_PhoneIcon, m_AlertIcon, m_AlarmIcon;
    private int         m_HeadSetVol, m_SpeakerVol;
    private int         m_OnColour, m_OffColour;
    private boolean     m_IsOn; 
    private boolean     m_IsSetAllStream; 
    private int         m_MenuTextId;
    private VolumeManager m_VolMgr = null;
    

    @Override
    public void onCreate(Bundle bundle)
    {
        restoreInstance(getActivity(), bundle);
        // workaround for menu text - opPrepareOptionMenu is called first 
        // before onActivityCreated(), hence we need to set the menu id here
        // first
        m_MenuTextId = (m_IsSetAllStream) 
                        ? 
                        R.string.menu_set_all_stream 
                        : 
                        R.string.menu_set_media_stream;
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

        m_MediaIcon = (ImageView)view.findViewById(R.id.media_icon);
        m_PhoneIcon = (ImageView)view.findViewById(R.id.phone_icon);
        m_AlarmIcon = (ImageView)view.findViewById(R.id.alarm_icon);
        m_AlertIcon = (ImageView)view.findViewById(R.id.alert_icon);
        
        m_OnColour = getResources().getColor(R.color.holo_light_blue);
        m_OffColour = getResources().getColor(R.color.light_grey);
        
        
        return view;
    }

    
    @Override
    public void onActivityCreated(Bundle bundle)
    {
        m_VolMgr = new VolumeManager(getActivity());
        
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

        setServiceOnOff(isServiceRunning());
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
        {
            //set the opposite boolean value as we want the mew value 
            // after the press, not before it
            setStreamControl(!item.isChecked());
            if (m_IsOn)
                updateVolumes();
            return true;
        }
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
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
                (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
               
        for (RunningServiceInfo service : mgr.getRunningServices(Integer.MAX_VALUE))
        {
            if (VOL_CTRL_SERVICE.equals(service.service.getClassName()))
                return true;
        }
        return false;
    }
    
    /*
     * turn on and off volume control along with the appropriate change in 
     * UI 
     * */
    private void setServiceOnOff(boolean is_on)
    {
        Log.d(TAG, "setVolumeControlService");
        
        m_IsOn = is_on;
        m_Switch.setChecked(m_IsOn);
        enableDisableView(m_IsOn);
        setStreamControl(m_IsSetAllStream);
        
        if (m_IsOn)
        {
            Log.d(TAG, "Set Service On");
            updateVolumes();
        }
        else
        {
            Log.d(TAG, "Set Service Off");
            Intent intent = new Intent(getActivity(), VolumeControlService.class);
            getActivity().stopService(intent);
        }
    }
    
    private void enableDisableView(boolean is_on)
    {
        // This MUST be a view group
        ViewGroup vg = (ViewGroup)getView().findViewById(R.id.activity_layout);
        vg.setEnabled(is_on);
        for (int i = 0; i < vg.getChildCount(); ++i)
        {
            vg.getChildAt(i).setEnabled(is_on);
        }
        
        if (!is_on)
        {
            m_MediaIcon.setColorFilter(m_OffColour, Mode.MULTIPLY);
            m_PhoneIcon.setColorFilter(m_OffColour, Mode.MULTIPLY);
            m_AlarmIcon.setColorFilter(m_OffColour, Mode.MULTIPLY);
            m_AlertIcon.setColorFilter(m_OffColour, Mode.MULTIPLY);
            m_MenuTextId = R.string.menu_set_media_stream;
        }
    }
    
    private void setStreamControl(boolean is_all_stream)
    {
        Log.d(TAG, "setStreamControl");
        
        m_IsSetAllStream = is_all_stream;
        
        m_MenuTextId = (m_IsSetAllStream) 
                        ?
                        R.string.menu_set_all_stream
                        :
                        R.string.menu_set_media_stream;    
        
        if (m_IsOn)
        {
            if (m_IsSetAllStream)
            {
                m_MediaIcon.setColorFilter(m_OnColour, Mode.MULTIPLY);
                m_PhoneIcon.setColorFilter(m_OnColour, Mode.MULTIPLY);
                m_AlarmIcon.setColorFilter(m_OnColour, Mode.MULTIPLY);
                m_AlertIcon.setColorFilter(m_OnColour, Mode.MULTIPLY);
            }
            else
            {
                m_MediaIcon.setColorFilter(m_OnColour, Mode.MULTIPLY);
                m_PhoneIcon.setColorFilter(m_OffColour, Mode.MULTIPLY);
                m_AlarmIcon.setColorFilter(m_OffColour, Mode.MULTIPLY);
                m_AlertIcon.setColorFilter(m_OffColour, Mode.MULTIPLY);
            }
        }
        
        getActivity().invalidateOptionsMenu();
    }
    
    private void updateVolumes()
    {
        Log.d(TAG, "updateVolume");

        assert(m_IsOn);

        Intent intent = 
                new Intent(getActivity(), VolumeControlService.class);
        
        intent.putExtra(HEADSET_VOL, m_HeadSetVol);
        intent.putExtra(SPEAKER_VOL, m_SpeakerVol);
        intent.putExtra(IS_SET_ALL_STREAM, m_IsSetAllStream);
        
        getActivity().startService(intent);
    }
    
    
    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.switch_on)
            setServiceOnOff(m_Switch.isChecked());
    }
}
