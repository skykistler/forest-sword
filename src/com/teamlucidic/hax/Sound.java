package com.teamlucidic.hax;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

import com.jcraft.oggdecoder.OggData;
import com.jcraft.oggdecoder.OggDecoder;

public class Sound {
	/* The goal of this class is to act in a similar manner to the Texture class.
	 * That's a little difficult because handling sounds with OpenAL is, while similar
	 * to OpenGL textures, not identical and so it gets a bit awk at times.
	 * The static part of this class handles all the sounds, the Sound object
	 * holds sound data and plays it.
	 * */

	public static HashMap<Integer, Sound> soundMap = new HashMap<Integer, Sound>();
	public static ArrayList<Sound> paused = new ArrayList<Sound>();

	public static int masterVolume = 100;
	public static float[] listenerPos = new float[3];
	public static float[] direction = new float[6];

	public String name;
	public int bufferId = -1;
	public int backgroundSourceId = -1;
	public boolean isLooping;
	public boolean isPaused;
	public ArrayList<Integer> usedSources = new ArrayList<Integer>();

	public Sound(String path) {
		name = path;
		String fullpath = Main.m.resourcePack + "audio/" + path;
		bufferId = AL10.alGenBuffers();
		int format = 0;
		ByteBuffer data = null;
		int freq = 0;

		WaveData wd = null;
		OggData od = null;
		try {
			if (path.endsWith(".wav")) {
				wd = WaveData.create(new BufferedInputStream(new FileInputStream(fullpath)));
				format = wd.format;
				data = wd.data;
				freq = wd.samplerate;
			} else if (path.endsWith(".ogg")) {
				OggDecoder oggdec = new OggDecoder();
				InputStream is = new BufferedInputStream(new FileInputStream(fullpath));
				od = oggdec.getData(is);
				format = od.channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16;
				data = od.data;
				freq = od.rate;
			} else {
				Main.m.error("Could not load filetype " + path.substring(path.lastIndexOf(".") + 1, path.length()) + " for file " + path);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Main.m.error("Could not load sound " + path);
		}

		AL10.alBufferData(bufferId, format, data, freq);

		if (wd != null)
			wd.dispose();

		soundMap.put(path.toLowerCase().hashCode(), this);
	}

	public void update() {
		if (isLooping && !isPaused && !isPlaying(backgroundSourceId)) {
			AL10.alDeleteSources(backgroundSourceId);
			backgroundSourceId = -1;
			playInBackground(true);
		}
		for (int s = 0; s < usedSources.size(); s++) {
			Integer i = usedSources.get(s);
			if (i != backgroundSourceId && !isPlaying(i))
				AL10.alDeleteSources(i);
		}

	}

	public void play(int source) {
		AL10.alSourcePlay(source);
		if (source == backgroundSourceId)
			isPaused = false;
	}

	public void pause(int source) {
		AL10.alSourcePause(source);
		if (source == backgroundSourceId)
			isPaused = true;
	}

	public void stop(int source) {
		AL10.alSourceStop(source);
		if (source == backgroundSourceId)
			isPaused = false;
	}

	public boolean isPlaying(int source) {
		if (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING)
			return true;
		else
			return false;
	}

	public int playAtPosition(double x, double y, double z) {
		return playAtNewSource(x, y, z, 1, 1, 0, 0, 0);
	}

	public int playAtPositionGain(double x, double y, double z, float gain) {
		return playAtNewSource(x, y, z, 1, gain, 0, 0, 0);
	}

	public int playAtNewSource(double x, double y, double z, float pitch, float gain, float vx, float vy, float vz) {
		int source = AL10.alGenSources();
		AL10.alSourcei(source, AL10.AL_BUFFER, bufferId);
		AL10.alSource3f(source, AL10.AL_POSITION, (float) x, (float) y, (float) z);
		AL10.alSourcef(source, AL10.AL_PITCH, pitch);
		AL10.alSourcef(source, AL10.AL_GAIN, gain);
		AL10.alSource3f(source, AL10.AL_VELOCITY, vx, vy, vz);
		AL10.alSourcePlay(source);
		usedSources.add(source);
		return source;
	}

	public void playInBackground(boolean loop) {
		if (isPaused && backgroundSourceId > 0) {
			play(backgroundSourceId);
		} else
			backgroundSourceId = playAtNewSource(listenerPos[0], listenerPos[1], listenerPos[2], 1, 1, 0, 0, 0);
		isLooping = loop;
		isPaused = false;
	}

	public void pauseBackground() {
		pause(backgroundSourceId);
		isPaused = true;
	}

	public void stopBackground() {
		stop(backgroundSourceId);
		backgroundSourceId = -1;
		isPaused = false;
		isLooping = false;
	}

	public boolean isBackgroundPlaying() {
		return isPlaying(backgroundSourceId);
	}

	public void delete() {
		if (bufferId > 0)
			AL10.alDeleteBuffers(bufferId);
		for (Integer i : usedSources)
			AL10.alDeleteSources(i);
		usedSources.clear();
	}

	public static void init() {
		try {
			AL.create();
			loadSoundsInFolder(Main.m.resourcePack + "audio/");
		} catch (LWJGLException e) {
			e.printStackTrace();
			Main.m.error("Could not create sound library, sounds will be disabled");
			masterVolume = 0;
		}
		System.out.println("Successfully loaded " + soundMap.values().size() + " sounds.");
	}

	public static void loadSoundsInFolder(String path) {
		File folder = new File(path);
		File[] files = folder.listFiles();
		if (files != null)
			for (File file : files)
				if (file.isDirectory())
					loadSoundsInFolder(file.getAbsolutePath());
				else
					new Sound(file.getAbsolutePath().substring((Main.m.resourcePack + "audio/").length()).replace("\\", "/"));
	}

	public static Sound getSound(String name) {
		if (!soundMap.containsKey(name.toLowerCase().hashCode())) {
			System.out.println("Loading: " + name);
			return new Sound(name);
		}
		return soundMap.get(name.toLowerCase().hashCode());
	}

	public static void updateAll() {
		if (Main.m.camera != null)
			AL10.alListener3f(AL10.AL_POSITION, listenerPos[0] = (float) Main.m.camera.posX, listenerPos[1] = (float) (Main.m.camera.posY + Main.m.camera.currentHeight), listenerPos[2] = (float) Main.m.camera.posZ);
		else
			AL10.alListener3f(AL10.AL_POSITION, listenerPos[0] = 0, listenerPos[1] = 0, listenerPos[2] = 0);
		//		direction[0] = (float) Math.sin()
		//		AL10.alListener(pname, value)
		for (Sound s : soundMap.values())
			s.update();
	}

	public static void pauseAllBackgroundSounds() {
		for (Sound s : soundMap.values())
			if (s.isBackgroundPlaying()) {
				s.pauseBackground();
				paused.add(s);
			}
	}

	public static void resumePausedBackgroundSounds() {
		for (Sound s : paused)
			if (s.isPaused)
				s.playInBackground(s.isLooping);
		paused.clear();
	}

	public static void stopAllBackgroundSounds() {
		for (Sound s : soundMap.values())
			s.stopBackground();
	}

	public static void cleanup() {
		for (Sound sound : soundMap.values())
			sound.delete();
		AL.destroy();
	}

}