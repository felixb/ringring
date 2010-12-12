/*
 * Copyright (C) 2010 Felix Bechstein
 * 
 * This file is part of RingRing.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.ringring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

/**
 * Act on incoming Calls.
 * 
 * @author flx
 */
public class Receiver extends BroadcastReceiver {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReceive(final Context context, final Intent intent) {
		final String state = intent
				.getStringExtra(TelephonyManager.EXTRA_STATE);
		if (state == null
				|| !state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			return;
		}
		final String remote = intent
				.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		if (remote == null) {
			return;
		}
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (!p.getBoolean(Preferences.PREFS_ENABLE, true)) {
			return;
		}
		final String s = p.getString(Preferences.PREFS_DATA, "");
		if (s == null || s.length() <= 2 * Preferences.SEP.length()) {
			return;
		}
		final String[] data = s.split(Preferences.SEP);
		boolean match = false;
		for (String d : data) {
			if (d == null) {
				continue;
			}
			d = d.trim();
			if (d.length() == 0) {
				continue;
			}

			if (d.startsWith("%") && d.length() > 1) {
				if (d.endsWith("%") && d.length() > 2) {
					if (remote.contains(d.substring(1, d.length() - 1))) {
						match = true;
						break;
					}
				} else {
					if (remote.endsWith(d.substring(1))) {
						match = true;
						break;
					}
				}
			} else if (d.endsWith("%") && d.length() > 1) {
				if (remote.startsWith(d.substring(0, d.length() - 1))) {
					match = true;
					break;
				}
			} else {
				if (remote.equals(d)) {
					match = true;
					break;
				}
			}
		}
		if (!match) {
			return;
		}
		// set to ring mode
		final AudioManager amgr = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		amgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}
}
