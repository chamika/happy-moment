package com.chamika.happymoment.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chamika.happymoment.R;
import com.chamika.happymoment.R.id;
import com.chamika.happymoment.R.layout;
import com.chamika.happymoment.model.FBComment;
import com.chamika.happymoment.model.FBPerson;

public class LikesAdapter extends ArrayAdapter<FBPerson> {
	private static final int resource = R.layout.likes_row;

	private LayoutInflater inflater;

	public LikesAdapter(Context context, ArrayList<FBPerson> initialList) {
		super(context, resource, initialList);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = inflater.inflate(resource, parent, false);
		}

		FBPerson item = getItem(position);
		if (item != null) {
			TextView textTitle = (TextView) convertView.findViewById(R.id.title);

			if (item.getName() != null) {
				textTitle.setText(item.getName());
			} else {
				textTitle.setText("");
			}

		}

		return convertView;
	}
}
