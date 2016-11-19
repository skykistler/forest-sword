package com.teamlucidic.hax;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {
	public static boolean running = false;
	public static boolean success = false;

	public static boolean unzip(String zipPath, String toPath, boolean deleteZipWhenDone) {
		if (running) {
			System.out.println("Already running unzipper");
			return false;
		}
		success = false;
		new Thread(new UnzipRunnable(zipPath, toPath, deleteZipWhenDone)).start();
		return success;
	}
}

class UnzipRunnable implements Runnable {
	public File zip;
	public File destination;
	public boolean deleteWhenDone;

	public UnzipRunnable(String zipPath, String toPath, boolean deleteZipWhenDone) {
		zip = new File(Main.m.resourcePack + zipPath);
		destination = new File(Main.m.resourcePack + toPath);
		destination.mkdirs();
		deleteWhenDone = deleteZipWhenDone;
	}

	public void run() {
		ZipUtils.running = true;
		if (!zip.exists() || !destination.isDirectory()) {
			ZipUtils.success = false;
			ZipUtils.running = false;
			if (!zip.exists())
				Main.m.error("Zip does not exist!");
			if (!destination.isDirectory())
				Main.m.error("Destination is not a directory or does not exist!");
			return;
		}
		try {
			ZipFile zfile = new ZipFile(zip);
			Enumeration<? extends ZipEntry> entries = zfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File file = new File(destination, entry.getName());
				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					file.getParentFile().mkdirs();
					InputStream in = zfile.getInputStream(entry);
					try {
						OutputStream out = new FileOutputStream(file);
						try {
							byte[] buffer = new byte[1024];
							while (true) {
								int readCount = in.read(buffer);
								if (readCount < 0) {
									break;
								}
								out.write(buffer, 0, readCount);
							}
						} finally {
							out.close();
						}
					} finally {
						in.close();
					}
				}
			}
			zfile.close();
			ZipUtils.success = true;

			if (deleteWhenDone)
				zip.delete();
		} catch (Exception e) {
			Main.m.error("Could not unzip " + zip.getAbsolutePath());
			e.printStackTrace();
		}

		ZipUtils.running = false;
	}
}