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
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Set up RingRing.
 * 
 * @author flx
 */
public class Preferences extends ListActivity implements OnClickListener,
		OnItemClickListener {

	/** Preference's name: data. */
	public static final String PREFS_DATA = "data";
	/** Preference's name: enable. */
	public static final String PREFS_ENABLE = "enable";
	/** Preference's separator. */
	public static final String SEP = "~#~";

	/** Objects of this list. */
	final ArrayList<String> objects = new ArrayList<String>();
	/** Adapter representing the objects. */
	ArrayAdapter<String> adapter = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.list_ok_add);
		this.adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, this.objects);

		this.setListAdapter(this.adapter);
		this.getListView().setOnItemClickListener(this);

		this.findViewById(R.id.add).setOnClickListener(this);
		this.findViewById(R.id.ok).setOnClickListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onResume() {
		super.onResume();
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
		((CheckBox) this.findViewById(R.id.enable)).setChecked(p.getBoolean(
				PREFS_ENABLE, true));
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
		e.putBoolean(PREFS_ENABLE, ((CheckBox) this.findViewById(R.id.enable))
				.isChecked());
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