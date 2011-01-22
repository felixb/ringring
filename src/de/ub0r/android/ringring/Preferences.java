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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.ub0r.android.lib.Log;

/**
 * Set up RingRing.
 * 
 * @author flx
 */
public class Preferences extends ListActivity implements OnClickListener,
		OnItemClickListener {

	/** Dialog: about. */
	private static final int DIALOG_ABOUT = 0;

	/** Preference's name: data. */
	public static final String PREFS_DATA = "data";
	/** Preference's name: mode. */
	public static final String PREFS_MODE = "mode";
	/** Available modes. */
	public static final int[] ringModes = new int[] {
			AudioManager.RINGER_MODE_NORMAL, AudioManager.RINGER_MODE_NORMAL,
			AudioManager.RINGER_MODE_VIBRATE, AudioManager.RINGER_MODE_SILENT,
			-1 };
	/** Available modes. */
	public static final int[] vibrateModes = new int[] {
			AudioManager.VIBRATE_SETTING_ON, AudioManager.VIBRATE_SETTING_OFF,
			AudioManager.VIBRATE_SETTING_ON, AudioManager.VIBRATE_SETTING_OFF,
			-1 };
	/** Array of String Resources for modes. */
	private static final Integer[] resModes = new Integer[] {
			R.string.ringvibrate, R.string.ring, R.string.vibrate,
			R.string.silent, R.string.disable };
	/** Array of String Resources for modes. */
	private static final String[] strModes = new String[resModes.length];
	/** Preference's separator. */
	public static final String SEP = "~#~";

	/** Objects of this list. */
	final ArrayList<String> objects = new ArrayList<String>();
	/** Adapter representing the objects. */
	ArrayAdapter<String> adapter = null;

	/** {@link Spinner} holding modes. */
	private Spinner spModes = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.init(this.getString(R.string.app_name));
		this.setContentView(R.layout.list_ok_add);
		this.adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, this.objects);

		this.setListAdapter(this.adapter);
		this.getListView().setOnItemClickListener(this);

		this.findViewById(R.id.add).setOnClickListener(this);
		this.findViewById(R.id.ok).setOnClickListener(this);
		final int l = resModes.length;
		for (int i = 0; i < l; i++) {
			strModes[i] = this.getString(resModes[i]);
		}

		this.spModes = (Spinner) this.findViewById(R.id.mode);
		this.spModes.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, strModes));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_about: // start about dialog
			this.showDialog(DIALOG_ABOUT);
			return true;
		case R.id.item_more:
			try {
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://search?q=pub:\"Felix Bechstein\"")));
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this, "missing market application",
						Toast.LENGTH_LONG).show();
			}
			return true;
		default:
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final Dialog onCreateDialog(final int id) {
		Dialog d;
		switch (id) {
		case DIALOG_ABOUT:
			d = new Dialog(this);
			d.setContentView(R.layout.about);
			d.setTitle(this.getString(R.string.about_) + " v"
					+ this.getString(R.string.app_version));
			return d;
		default:
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onResume() {
		super.onResume();
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
		final int mode = p.getInt(PREFS_MODE, 0);
		if (mode >= 0 && mode < vibrateModes.length) {
			this.spModes.setSelection(mode);
		} else {
			this.spModes.setSelection(0);
		}
		final String s = p.getString(PREFS_DATA, "");
		this.objects.clear();
		if (s != null && s.length() > 0) {
			final String[] data = s.split(SEP);
			for (String d : data) {
				if (d == null || d.trim().length() == 0) {
					continue;
				}
				this.objects.add(d.trim());
			}
		}
		this.adapter.notifyDataSetChanged();

		if (this.objects.isEmpty()) {
			this.findViewById(R.id.add_hint).setVisibility(View.VISIBLE);
		} else {
			this.findViewById(R.id.add_hint).setVisibility(View.GONE);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPause() {
		super.onPause();
		final Editor e = PreferenceManager.getDefaultSharedPreferences(this)
				.edit();
		StringBuilder sb = new StringBuilder();
		sb.append(SEP);
		for (String d : this.objects) {
			sb.append(d);
			sb.append(SEP);
		}
		sb.append(SEP);
		e.putString(PREFS_DATA, sb.toString());
		e.putInt(PREFS_MODE, this.spModes.getSelectedItemPosition());
		e.commit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.ok:
			this.finish();
			break;
		case R.id.add:
			this.addEdit(-1);
			break;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		final AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setCancelable(true);
		b.setItems(R.array.edit_delete, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				switch (which) {
				case 0:
					Preferences.this.addEdit(position);
					break;
				case 1:
					Preferences.this.objects.remove(position);
					Preferences.this.adapter.notifyDataSetChanged();
					if (Preferences.this.objects.isEmpty()) {
						Preferences.this.findViewById(R.id.add_hint)
								.setVisibility(View.VISIBLE);
					}
					break;
				default:
					break;
				}
			}
		});
		b.show();
	}

	/**
	 * Add or edit an item.
	 * 
	 * @param pos
	 *            position in adapter to edit
	 */
	private void addEdit(final int pos) {
		final AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle(R.string.add_number);
		b.setCancelable(true);
		final EditText et = new EditText(this);
		if (pos >= 0) {
			et.setText(this.objects.get(pos));
		}
		b.setView(et);
		b.setNegativeButton(android.R.string.cancel, null);
		b.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						final String number = et.getText().toString();
						if (number == null || number.length() == 0) {
							return;
						}
						if (pos < 0) {
							Preferences.this.objects.add(number);
						} else {
							Preferences.this.objects.set(pos, number);
						}
						Preferences.this.adapter.notifyDataSetChanged();
						Preferences.this.findViewById(R.id.add_hint)
								.setVisibility(View.GONE);
					}
				});
		b.show();
	}
}