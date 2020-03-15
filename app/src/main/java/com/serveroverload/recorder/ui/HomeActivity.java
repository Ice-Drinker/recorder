package com.serveroverload.recorder.ui;

import java.util.ArrayList;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import cn.com.gxrb.master.audio.R;


public class HomeActivity extends FragmentActivity {

	MediaPlayer mMediaPlayer;

	private ArrayList<String> recordings = new ArrayList<String>();

	public int RecordingNumber;

	/**
	 * @return the mMediaPlayer
	 */
	public MediaPlayer getmMediaPlayer() {
		return mMediaPlayer;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);

		mMediaPlayer = new MediaPlayer();

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(R.id.container, new RecordAudioFragment());
		fragmentTransaction.addToBackStack("RecordAudioFragment");
		fragmentTransaction.commit();
		
		AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer = null;
		}

	}

	public ArrayList<String> getRecordings() {
		return recordings;
	}

	public void setRecordings(ArrayList<String> recordings) {
		this.recordings = recordings;
	}

}
