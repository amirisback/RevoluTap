package com.frogobox.rythmtap.model;
import com.frogobox.rythmtap.util.Tools;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Iterator;

public class DataParser implements Iterator<DataNote>  {
	
	public DataFile df;
	public int notesDataIndex;
	private int notesIndex;

	
	public DataParser(String filename)
	throws DataParserException, FileNotFoundException {
		if (!Tools.isStepfile(filename)) {
			throw new DataParserException(
					"Tools.getString(R.string.DataParser_unsupported)");
		}
		df = new DataFile(filename);
		notesDataIndex = 0;
		notesIndex = 0;
		
		if (Tools.isSMFile(filename)) {
			DataParserSM.parse(df);
		} else if (Tools.isDWIFile(filename)) {
			DataParserDWI.parse(df);
		}
	}
	
	//public DataFile getDataFile() {
		//return df;
	//}
	
	public DataNotesData getNotesData() {
		return df.notesData.get(notesDataIndex);
	}
	
	public void setNotesDataIndex(int i) {
		notesDataIndex = i;
	}
	
	public void loadNotes(boolean jumps, boolean holds, boolean randomize)
	throws DataParserException {
		if (Tools.isSMFile(df.getFilename())) {
			DataParserSM.parseNotesData(
					df, df.notesData.get(notesDataIndex),
					jumps, holds, randomize
					);
		} else if (Tools.isDWIFile(df.getFilename())) {
			DataParserDWI.parseNotesData(
					df, df.notesData.get(notesDataIndex),
					jumps, holds, randomize
					);
		} else {
			throw new DataParserException(
					"Tools.getString(R.string.DataParser_unsupported)");
		}
		Collections.sort(df.notesData.get(notesDataIndex).notes);
		
	}
	
	public boolean hasNext() {
		try {
			return (notesIndex < df.notesData.get(notesDataIndex).notes.size());
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}
	
	public DataNote peek() {
		if (hasNext()) {
			try {
				return df.notesData.get(notesDataIndex).notes.get(notesIndex);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public DataNote next() {
		DataNote n = peek();
		if (n != null) {
			notesIndex++;
		}
		return n;
	}
	
	public void remove() {
		throw new UnsupportedOperationException("ZE METHOD, ZIT DOES NOSSING!");
	}

}
