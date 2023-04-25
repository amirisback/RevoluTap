package com.frogobox.rythmtap.widget;

import androidx.annotation.NonNull;

import com.frogobox.rythmtap.model.DataNote;
import com.frogobox.rythmtap.model.DataParser;
import com.frogobox.rythmtap.util.Tools;
import com.frogobox.rythmtap.util.ToolsArrayQueue;

import java.util.Iterator;

public class GUIFallingObjectSpace implements Iterable<GUIFallingObject> {
	/* A place to put timed falling objects into columns.*/
	
	private final ToolsArrayQueue<GUIFallingObject>[] available = emptyObjArray();
	private final ToolsArrayQueue<GUIFallingObject>[] missed = emptyObjArray();
	private final ToolsArrayQueue<GUIFallingObject>[] done = emptyObjArray(); // To avoid calls to GC; extra overhead but avoids lag spikes
	
	private final DataParser dp;
	
	/* If the object space has been changed since we last called fetchAll, this is null.
	   Otherwise, it is the argument of the most recent fetchAll() call. */
	private GUIFallingObject[] _last_fetchAll_arr = new GUIFallingObject[0];
	
	public static ToolsArrayQueue<GUIFallingObject>[] emptyObjArray() {
		@SuppressWarnings("unchecked")
		ToolsArrayQueue<GUIFallingObject>[] arr = new ToolsArrayQueue[Tools.PITCHES];
		for (int i = 0; i < Tools.PITCHES; i++) 
			arr[i] = new ToolsArrayQueue<>();
		return arr;
	}
	
	public GUIFallingObjectSpace(DataParser dataparser) {
		dp = dataparser;
	}
	
	// Remove all GUIFallingObjects from all ToolsArrayQueues so GC can collect them
	public void clearArrays() {
		for (int i = 0; i < Tools.PITCHES; i++) {
			while (!available[i].isEmpty()) available[i].remove();
			while (!missed[i].isEmpty()) missed[i].remove();
			while (!done[i].isEmpty()) done[i].remove();
		}
	}
	
	//holds whose HOLD_END has not yet appeared
	private final GUIFallingHold [] holds_to_create = new GUIFallingHold[Tools.PITCHES];
	public void update(int onScreenTime, int offScreenTime, GUIScore score) {
		/* Update the viewing area.
		 * All objects with time<offScreenTime are removed, and objects with time<=onScreenTime are added.
		 * Objects will be added in time order.
		 * 
		 * Updates the score's arrow/hold/etc counts.
		 * */
		
		for (int pitch = 0; pitch < Tools.PITCHES; pitch++) {
			//technically we only should need to look thru the missed ones
			//for (Queue<GUIFallingObject>[] columns: new Queue[][] {available, missed}) {
			
				//while ((!falling.isEmpty()) && (falling.get(0).data().missed())) {
				//Queue<GUIFallingObject> falling = columns[pitch];
				GUIFallingObject o;
				while ((o = available[pitch].peek()) != null && (o.end_time < offScreenTime)) { // off-screen
					done[pitch].add(available[pitch].remove());
					_last_fetchAll_arr = null;
				}
				while ((o = missed[pitch].peek()) != null && (o.end_time < offScreenTime)) { // off-screen
					done[pitch].add(missed[pitch].remove());
					_last_fetchAll_arr = null;
				}
				
			//}
		}

		while (dp.hasNext() && dp.peek().time <= onScreenTime) {
				DataNote currentNote = dp.next();
				int pitch = currentNote.column;

				GUIFallingObject o = null;
				switch(currentNote.noteType) {
					case TAP_NOTE:
						o = new GUIFallingArrow(currentNote);
						break;
					case HOLD_START:
						GUIFallingHold h = new GUIFallingHold(currentNote);
						holds_to_create[pitch] = h;
						o = h;
						score.holdCount++;
						break;
					case HOLD_END:
						if (holds_to_create[pitch] != null) {
							holds_to_create[pitch].end_time = currentNote.time;
							holds_to_create[pitch] = null;
						}
						break;
					default:
						break;
				}
				
				if (o != null) {
					available[pitch].add(o);
					_last_fetchAll_arr = null;
					score.noteCount++;
				}
			}

	}
	
	public GUIFallingObject peekColumn(int pitch) {
		/* return the earliest available (i.e. non-missed) note in a column */
		
		if (available[pitch].isEmpty()) return null;
		else return available[pitch].peek();
	}
	
	public void popColumn(int pitch) {
		/* return and remove the earliest available (i.e. non-missed) note in a column */
		if (available[pitch].isEmpty()) return;
		
		_last_fetchAll_arr = null;
		GUIFallingObject o = available[pitch].remove();
		done[pitch].add(o);
	}
	
	public void missColumn(int pitch) {
		/* return the earliest available (i.e. non-missed) note in a column,
		 * and set its missed status to true */
		if (available[pitch].isEmpty()) return;
		
		_last_fetchAll_arr = null;
		GUIFallingObject o = available[pitch].remove();
		o.missed = true;
		missed[pitch].add(o);
	}
	
	public boolean isDone() {
		//true if screen is empty and no more notes are coming
		return !dp.hasNext() && (size() == 0);
	}

	private GUIFallingObject[] tempArrayEnsureCapacity(int cap, GUIFallingObject[] arr) {
		/* Returns a array of size at least cap. The array returned may be arr.
		 * There is no guarantee as to what elements are in the array. */
		if ((arr == null) || (arr.length < cap)) {
			//Log.i("FallingObjectSpace", "new size: " + (cap*2));
			return new GUIFallingObject[cap*2];
		} else {
			return arr;
		}
	}
	
	private final ToolsArrayQueue<GUIFallingObject> _iter_Q = new ToolsArrayQueue<>();

	@NonNull
	public Iterator<GUIFallingObject> iterator() {
		/* Iterate through the falling objects, in no particular order. */
		_iter_Q.clear();
		for (int i = 0; i < Tools.PITCHES; i++) 
			_iter_Q.addQueue(missed[i]);
		for (int i = 0; i < Tools.PITCHES; i++) 
			_iter_Q.addQueue(available[i]);
		return _iter_Q.iterator();
	}
	
	/* Put the contents of this object space into an array (possibly the provided array) and return the array.
	 * The array can be modified only if you do not intend to call fetchAll again on the same array. 
	 *   (Calling fetchAll multiple times on the same array has no effect
	 *   if this object space is not modified in the interim.)
	 * */
	public GUIFallingObject[] fetchAll(GUIFallingObject[] arr) {
		if (arr == _last_fetchAll_arr) return arr;
		_last_fetchAll_arr = arr;
		
		arr = tempArrayEnsureCapacity(size() + 1, arr);
		int idx = 0;
		for (int i = 0; i < Tools.PITCHES; i++) 
			idx += missed[i].arraycopy(arr, idx);
		for (int i = 0; i < Tools.PITCHES; i++) 
			idx += available[i].arraycopy(arr, idx);
		arr[idx++] = null;
		return arr;
	}
	
	public int size() {
		int x = 0;
		for (int i = 0; i < Tools.PITCHES; i++) {
			x += available[i].size();
			x += missed[i].size();
		}
		return x;
	}
	
}
