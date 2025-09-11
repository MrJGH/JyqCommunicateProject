package com.tencent.qcloud.tuikit.tuichat.classicui.media;

/**
 *   录音管理
 * @author zzj
 */

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AudioManager {
	private MediaRecorder mRecorder;
	private String mDir;
	private String mCurrentFilePathString;
	private static AudioManager mInstance;
	private boolean isPrepared;

	public interface AudioStateListener {
		void wellPrepared();
	}
	public AudioStateListener mListener;

	public void setOnAudioStateListener(AudioStateListener listener) {
		mListener = listener;
	}
	private AudioManager(String dir) {
		mDir = dir;
	}
	public static AudioManager getInstance(String dir) {
		if (mInstance == null) {
			synchronized (AudioManager.class) {
				if (mInstance == null) {
					mInstance = new AudioManager(dir);
				}

			}
		}
		return mInstance;

	}
	public void prepareAudio() {
		System.out.println("00");
		isPrepared = false;
		File dir = new File(mDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String fileName = generalFileName();
		File file = new File(dir, fileName);
		mCurrentFilePathString = file.getAbsolutePath();
		mRecorder = new MediaRecorder();
		System.out.println("mRecorder" + mRecorder);
		mRecorder.setOutputFile(file.getAbsolutePath());
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			mRecorder.prepare();
			mRecorder.start();
			isPrepared = true;
			if (mListener != null) {
				mListener.wellPrepared();
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String generalFileName() {
		return UUID.randomUUID().toString() + ".amr";
	}

	public int getVoiceLevel(int maxLevel) {
		if (isPrepared && mRecorder != null) {
			System.out.println(mRecorder);
			return maxLevel * mRecorder.getMaxAmplitude() / 32768 + 1;
		}
		return 1;
	}

	public void release() {
		if(mRecorder != null) {
			try {
				mRecorder.stop();
			}catch (Exception e){
			}
			try {
				mRecorder.release();
			}catch (Exception e){
			}
			mRecorder = null;
		}
	}

	public void cancel() {
		release();
		if (mCurrentFilePathString != null) {
			File file = new File(mCurrentFilePathString);
			file.delete();
			mCurrentFilePathString = null;
		}
	}
	public String getCurrentFilePath() {
		return mCurrentFilePathString;
	}

}
