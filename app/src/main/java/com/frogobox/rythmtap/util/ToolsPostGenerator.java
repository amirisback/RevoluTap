package com.frogobox.rythmtap.util;

import com.frogobox.rythmtap.model.DataFile;
import com.frogobox.rythmtap.model.DataParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ToolsPostGenerator {

	private static final String extractPath = "output/extract/";
	private static final String outputPath = "output/post/";
	private static final String websiteURL = "http://beatsportable.com/static/songs/";
	private static final String downloadImage = "http://beatsportable.com/static/images/download.png";
	private static final String supportThread = "http://beatsportable.com/forum/viewtopic.php?t=18";

	private static final String songPack = "Otaku's Dream 5th Anime Mix -PAD Sector-";
	private static final String bannerPath = websiteURL + songPack + "/banners/";
	private static final String downloadPath = websiteURL + songPack + "/packs/";
	private static final String listPath = "Z:\\_Packs\\Songs-zipped\\Songs\\Otaku's Dream 5th Anime Mix -PAD Sector-\\packs";
	private static final String releaseThread = "http://beatsportable.com/forum/viewtopic.php?f=31&t=450";
	
	public static void main(String[] args) {
		try {
			File outputDir = new File(outputPath);
			outputDir.mkdirs();
			File extractDir = new File(extractPath);
			extractDir.mkdirs();
			
			File dir = new File(listPath);
			for (File file : Objects.requireNonNull(dir.listFiles())) {
				String filename = file.getPath();
				if (Tools.isStepfilePack(filename)) {
					String stepfile = unzip(filename);
					parseStepfile(filename, stepfile);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getFilename(String s) {
		String f = s;
		if (f.contains("/")) {
			f = f.substring(f.lastIndexOf("/") + 1);
		}
		if (f.contains("\\")) {
			f = f.substring(f.lastIndexOf("\\") + 1);
		}
		return f;
	}
	
	private static String getBasename(String s) {
		String b = getFilename(s);
		if (b.contains(".")) {
			b = b.substring(0, b.lastIndexOf("."));
		}
		return b;
	}
	
	private static String cleanup(String s) {
		return s.replace("[", "%5B").replace("]", "%5D").replace(" ", "%20");
	}

	private static void parseStepfile(String file, String stepfile) {
		try {
			DataParser dp = new DataParser(stepfile);
			DataFile df = dp.df;
			String basename = getBasename(file);
			
			String title = df.getTitle();
			if (df.getTitleTranslit().length() > 2) {
				title = title + " (" + df.getTitleTranslit() + ")";
			}
			String artist = df.getArtist();
			if (df.getArtistTranslit().length() > 2) {
				artist = artist + " (" + df.getArtistTranslit() + ")";
			}
			
			System.out.println("[*]" + basename);
			String post =
				"[img]" + cleanup(bannerPath) + cleanup(basename) + ".png" + "[/img]" + "\n" +
    			"[size=100][font=Comic Sans MS]" + "\n" +
    			"[b]Song:[/b] " + title + "\n" +
    			"[b]Artist:[/b] " + artist + "\n" +
    			"[b]Credit:[/b] " + df.getCredit() + "\n" +
    			"[b]BPM Range: [/b] " + df.getBPMRange() + "\n" +
    			"[b]Difficulty:[/b] " + df.getNotesDataDifficulties() + "\n" +
    			"[b]Song Pack:[/b] " + "[url=" + releaseThread + "]" + songPack + "[/url]" + "\n" +
    			"[/font][/size]" + "\n" +
    			"[url=" + cleanup(downloadPath) + cleanup(getFilename(file)) + "][img]" + downloadImage + "[/img][/url]" + "\n" +
    			"Download/install prob? See [url=" + supportThread + "]here[/url]!" + "\n" +
    			""
				;
			String outputFile = outputPath + basename + ".txt";
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8));
			out.write(post);
			out.close();
			
			File bg = df.getBanner();
			if (bg != null) {
				bg.renameTo(new File(outputPath + basename + ".png"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Extraction code from http://java.sun.com/developer/technicalArticles/Programming/compression/
	private static void extractFile(
			String path, ZipFile zipfile, BufferedInputStream is, BufferedOutputStream dest, ZipEntry entry)
		throws IOException
	{
		is = new BufferedInputStream(zipfile.getInputStream(entry));
		int count;
		byte[] data = new byte[Tools.BUFFER];
		FileOutputStream fos = new FileOutputStream(path);
		dest = new BufferedOutputStream(fos, Tools.BUFFER);
		while ((count = is.read(data, 0, Tools.BUFFER)) != -1) {
			dest.write(data, 0, count);
		}
		dest.flush();
		dest.close();
		is.close();
	}
	
	private static String unzip(String file) throws IOException {
		String stepfile = "";
		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		ZipEntry entry;
		ZipFile zipfile;
		Enumeration<? extends ZipEntry> e;
		
		zipfile = new ZipFile(file);
		e = zipfile.entries();
		while (e.hasMoreElements()) {
			entry = e.nextElement();
			if (!entry.isDirectory()) {
				if (Tools.isSMFile(entry.getName())) {
					stepfile = extractPath + getFilename(entry.getName());
				}
				extractFile(extractPath + getFilename(entry.getName()), zipfile, is, dest, entry);
			}
		}
		
		return stepfile;
	}
	
}
