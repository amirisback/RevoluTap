package com.github.budsterblue.revolutap.widget;

import android.annotation.SuppressLint;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GUIListenersMulti extends GUIListeners {

	public GUIListenersMulti(GUIHandler handler) {
		super(handler);
	}

	// TODO: redo this unreadable input system
	public OnTouchListener getOnTouchListener() {
		return new OnTouchListener() {
			private final SparseIntArray finger2pitch = new SparseIntArray();
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent e) {
				if (!v.hasFocus()) v.requestFocus();
				if (autoPlay || h.done || h.score.gameOver) return false;
				int pitch;
				
				// Normal multi-touch
				int action = e.getActionMasked();
				int index = e.getActionIndex();
				switch (action) {
					case MotionEvent.ACTION_DOWN:
						index = 0;
						//fallthru
					case MotionEvent.ACTION_POINTER_DOWN:
						pitch = h.onTouch_Down(e.getX(index), e.getY(index));
						if (pitch > 0) finger2pitch.put(index, pitch);
						return pitch > 0;
					case MotionEvent.ACTION_POINTER_UP:
						h.onTouch_Up(e.getX(index), e.getY(index));
						if (finger2pitch.indexOfKey(index) > -1) { //finger2pitch.containsKey(actionpid)
							return h.onTouch_Up(finger2pitch.get(index));
						} else {
							return h.onTouch_Up(0xF);
						}
					case MotionEvent.ACTION_UP:
						h.onTouch_Up(e.getX(index), e.getY(index));
						return h.onTouch_Up(0xF);
					default:
						return false;
				}
			}
		};
	}	
}
