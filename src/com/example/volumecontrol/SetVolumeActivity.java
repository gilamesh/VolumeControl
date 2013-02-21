package com.example.volumecontrol;

import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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

public class SetVolumeActivity extends Activity implements OnSeekBarChangeListener, OnClickListener
{
    private static final String TAG = SetVolumeActivity.class.getSimpleName(); 
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

    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_volume);

        restoreInstance(savedInstanceState); 
        
        m_Switch        = (Switch)findViewById(R.id.switch_on);
        m_HeadSetSeek   = (SeekBar)findViewById(R.id.headset_control);
        m_SpeakerSeek   = (SeekBar)findViewById(R.id.speaker_control);
                       
        m_HeadSetSeek.setOnSeekBarChangeListener(this);
        m_SpeakerSeek.setOnSeekBarChangeListener(this);
        m_Switch.setOnClickListener(this);
              
        
        AudioManager audio_mgr = 
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        
        int max_vol = audio_mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC); 
        
        m_HeadSetSeek.setMax(max_vol);
        m_HeadSetSeek.setProgress(m_HeadSetVol);
        m_SpeakerSeek.setMax(max_vol);
        m_SpeakerSeek.setProgress(m_SpeakerVol);
        
    }    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onreateMenuOption");
        MenuInflater menu_inflater = getMenuInflater();
        menu_inflater.inflate(R.menu.activity_set_volume, menu);
        return true;
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
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onPrepareOptionMenu");
        
        MenuItem item =  menu.findItem(R.id.menu_set_ringer); 
        item.setTitle(m_MenuTextId);
        item.setChecked(m_IsSetAllStream);
        
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart()
    {
        Log.d(TAG, "onStart");
        setVolumeControlService(isServiceRunning());
        setStreamControl(m_IsSetAllStream);
        
        super.onStart();
    }

        
    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop");
        
        SharedPreferences.Editor pref_ed = 
                getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
                
        pref_ed.putBoolean(IS_SET_ALL_STREAM, m_IsSetAllStream);
        pref_ed.putInt(HEADSET_VOL, m_HeadSetVol);
        pref_ed.putInt(SPEAKER_VOL, m_SpeakerVol);
        pref_ed.commit();

        super.onStop();
    }

    
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle out_state)
    {
        Log.d(TAG, "Saving instance");
        out_state.putInt(HEADSET_VOL, m_HeadSetVol);
        out_state.putInt(SPEAKER_VOL, m_SpeakerVol);
        out_state.putBoolean(IS_SET_ALL_STREAM, m_IsSetAllStream);
        super.onSaveInstanceState(out_state);
    }

    private void restoreInstance(Bundle bundle)
    {
        AudioManager audio_mgr = 
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        
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
            SharedPreferences pref = getSharedPreferences(TAG, Context.MODE_PRIVATE);
            m_IsSetAllStream = pref.getBoolean(IS_SET_ALL_STREAM, false);
            m_HeadSetVol = pref.getInt(HEADSET_VOL, m_HeadSetVol);
            m_SpeakerVol = pref.getInt(SPEAKER_VOL, m_SpeakerVol);
        }
    
    
    
    
    
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser)
    {
        if (fromUser)
        {
            switch (seekBar.getId())
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
                (ActivityManager)getSystemService(ACTIVITY_SERVICE);
               
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
        setActivityOnOff(m_IsOn);
        
        if (m_IsOn)
        {
            updateVolumes();
        }
        else
        {
            Log.d(TAG, "stopping volvum service from actiivty");
            Intent intent = new Intent(this, VolumeControlService.class);
            stopService(intent);
        }
    }
    
    private void setActivityOnOff(boolean is_on)
    {
        // This MUST be a view group
        ViewGroup vg = (ViewGroup)findViewById(R.id.activity_layout);
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
        
        TextView all_text = (TextView)findViewById(R.id.word_all);
        TextView media_text = (TextView)findViewById(R.id.word_media);
        
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
        invalidateOptionsMenu();

        
    }
    
    private void updateVolumes()
    {
       Intent intent = new Intent(this, VolumeControlService.class);
        
        intent.putExtra(HEADSET_VOL, m_HeadSetVol);
        intent.putExtra(SPEAKER_VOL, m_SpeakerVol);
        intent.putExtra(IS_SET_ALL_STREAM, m_IsSetAllStream);
        startService(intent);
         
    }
    
    
    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.switch_on)
            setVolumeControlService(m_Switch.isChecked());        
    }
 }
