/*
 * Copyright (C) 2010-2011 Felix Bechstein
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import de.ub0r.android.lib.Log;

/**
 * Act on incoming Calls.
 * 
 * @author flx
 */
public final class Receiver extends BroadcastReceiver {
	/** Tag for log output. */
	private static final String TAG = "bc";

	/** Preference's name to switch back. */
	private static final String PREFS_NORMAL_RING = "normal_mode_ring";
	/** Preference's name to switch back. */
	private static final String PREFS_NORMAL_VIBRATE = "normal_mode_vibrate";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReceive(final Context context, final Intent intent) {
		Log.init(context.getString(R.string.app_name));
		Log.d(TAG, "onReceive()");
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		final String state = intent
				.getStringExtra(TelephonyManager.EXTRA_STATE);
		if (state == null
				|| !state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			Log.d(TAG, "status not 'ringing'");
			final int mr = p.getInt(PREFS_NORMAL_RING, -1);
			final int mv = p.getInt(PREFS_NORMAL_VIBRATE, -1);
			if (mr >= 0 && mv >= 0) {
				final AudioManager amgr = (AudioManager) context
						.getSystemService(Context.AUDIO_SERVICE);
				Editor e = p.edit();
				e.remove(PREFS_NORMAL_RING);
				e.remove(PREFS_NORMAL_VIBRATE);
				e.commit();
				Log.d(TAG, "switch back vibrate mode: " + mv);
				amgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, mv);
				Log.d(TAG, "switch back ring mode: " + mr);
				amgr.setRingerMode(mr);
			}
			Log.d(TAG, "status not 'ringing' -> exit");
			return;
		}
		String remote = intent
				.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		if (remote == null) {
			Log.d(TAG, "no remote number -> exit");
			return;
		}
		Log.d(TAG, "remote number: " + remote);
		final Pattern pattern = Pattern.compile("[+\\- /[0-9]]+");
		final Matcher matcher = pattern.matcher(remote);
		if (matcher.find()) {
			remote = matcher.group();
			Log.d(TAG, "remote number: " + remote);
		}
		final int mode = p.getInt(Preferences.PREFS_MODE, 0);
		Log.d(TAG, "mode: " + mode);
		int mr;
		int mv;
		if (mode >= 0 && mode < Preferences.RING_MODES.length) {
			mr = Preferences.RING_MODES[mode];
			mv = Preferences.VIBRATE_MODES[mode];
		} else {
			return;
		}
		if (mr < 0 || mv < 0) {
			return;
		}
		Log.d(TAG, "ringMode: " + mr);
		Log.d(TAG, "vibrateMode: " + mv);
		final String s = p.getString(Preferences.PREFS_DATA, "");
		if (s == null || s.length() <= 2 * Preferences.SEP.length()) {
			return;
		}
		final String[] data = s.split(Preferences.SEP);
		boolean match = false;
		for (String d : data) {
			Log.d(TAG, "match number against: " + d);
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
				if (PhoneNumberUtils.compare(remote, d)) {
					match = true;
					break;
				}
			}
		}
		if (!match) {
			Log.d(TAG, "no match -> exit");
			return;
		}
		// set to ring mode
		final AudioManager amgr = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		int i = amgr.getRingerMode();
		amgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, mv);
		amgr.setRingerMode(mr);

		final Editor e = p.edit();
		Log.d(TAG, "save current state (ring): " + i);
		e.putInt(PREFS_NORMAL_RING, i);
		i = amgr.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
		Log.d(TAG, "save current state (vibrate): " + i);
		e.putInt(PREFS_NORMAL_VIBRATE, i);
		e.commit();
	}
}
