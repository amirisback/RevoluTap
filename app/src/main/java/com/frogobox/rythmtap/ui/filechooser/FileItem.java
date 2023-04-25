package com.frogobox.rythmtap.ui.filechooser;


import java.io.File;

public class FileItem implements Comparable<FileItem>{

	private final String name, path;
	private final boolean isDir;
	private final File f;
	
	public FileItem(String name, String path, boolean isDir, File f) {
		this.name = name;
		this.path = path;
		this.isDir = isDir;
		this.f = f;
	}
	
	public int compareTo(FileItem another) {
		if (this.name != null) {
			return this.name.toLowerCase().compareTo(another.getName().toLowerCase());
		} else {
			throw new IllegalArgumentException();
		}
	}
	public String getName() {
		return this.name;
	}
	public String getPath() {
		return this.path;
	}
	public boolean isDirectory() {
		return this.isDir;
	}
	public File getFile() {
		return this.f;
	}

}
