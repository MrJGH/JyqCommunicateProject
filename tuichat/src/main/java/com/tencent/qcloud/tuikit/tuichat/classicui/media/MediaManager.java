package com.tencent.qcloud.tuikit.tuichat.classicui.media;

/**
 * @author zzj
 */

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;


import java.io.IOException;

public class MediaManager {
	private static boolean isPause;
	private static MediaPlayer mMediaPlayer;


	public static void playSound(String filePath,
								 OnAudioListener onCompletionListener) {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {

					mMediaPlayer.reset();
					onCompletionListener.onCompletion(mp);
					return false;
				}
			});
		} else {
			mMediaPlayer.reset();
		}
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnCompletionListener(onCompletionListener);
		try {


			mMediaPlayer.setDataSource(filePath);
			mMediaPlayer.prepare();
			onCompletionListener.audioTotalTime(mMediaPlayer.getDuration() / 1000f);
		} catch (IllegalArgumentException e) {
			onCompletionListener.onCompletion(mMediaPlayer);
			e.printStackTrace();
		} catch (SecurityException e) {
			onCompletionListener.onCompletion(mMediaPlayer);
			e.printStackTrace();
		} catch (IllegalStateException e) {
			onCompletionListener.onCompletion(mMediaPlayer);
			e.printStackTrace();
		} catch (IOException e) {
			onCompletionListener.onCompletion(mMediaPlayer);
			e.printStackTrace();
		}

		mMediaPlayer.start();
	}

	public static void playRawSound(Context context, int raw,
									OnAudioListener onCompletionListener) {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {

					mMediaPlayer.reset();
					if(onCompletionListener!=null)
					onCompletionListener.onCompletion(mp);
					return false;
				}
			});
		} else {
			mMediaPlayer.reset();
		}
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnCompletionListener(onCompletionListener);
		try {
			AssetFileDescriptor file = context.getResources().openRawResourceFd(raw);
			mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
			mMediaPlayer.prepare();

		} catch (IllegalArgumentException e) {
			if(onCompletionListener!=null)
			onCompletionListener.onCompletion(mMediaPlayer);
			e.printStackTrace();
		} catch (SecurityException e) {
			if(onCompletionListener!=null)
			onCompletionListener.onCompletion(mMediaPlayer);
			e.printStackTrace();
		} catch (IllegalStateException e) {
			if(onCompletionListener!=null)
			onCompletionListener.onCompletion(mMediaPlayer);
			e.printStackTrace();
		} catch (IOException e) {
			if(onCompletionListener!=null)
			onCompletionListener.onCompletion(mMediaPlayer);
			e.printStackTrace();
		}

		mMediaPlayer.start();
	}

	public static float getAudioDuring(String filePath) {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					mMediaPlayer.reset();
					return false;
				}
			});
		}
		mMediaPlayer.reset();
		try {
			mMediaPlayer.setDataSource(filePath);
			mMediaPlayer.prepare();
			return mMediaPlayer.getDuration() / 1000f;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void pause() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			isPause = true;
		}
	}

	public static void seekToEnd() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.seekTo(mMediaPlayer.getDuration() -1);

		}
	}

	public static void resume() {
		if (mMediaPlayer != null && isPause) {
			mMediaPlayer.start();
			isPause = false;
		}
	}

	public static void release() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	public static class OnAudioListener implements OnCompletionListener{

		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {

		}

		public void audioTotalTime(float time){

		}
	}
}
