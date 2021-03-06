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

public class CommentsAdapter extends ArrayAdapter<FBComment> {
	private static final int resource = R.layout.comments_row;

	private LayoutInflater inflater;

	public CommentsAdapter(Context context, ArrayList<FBComment> initialList) {
		super(context, resource, initialList);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = inflater.inflate(resource, parent, false);
		}

		FBComment item = getItem(position);
		if (item != null) {
			TextView textTitle = (TextView) convertView.findViewById(R.id.title);
			TextView textSubtitle = (TextView) convertView.findViewById(R.id.subtitle);

			if (item.getName() != null) {
				textTitle.setText(item.getName());
			} else {
				textTitle.setText("");
			}

			if (item.getMessage() != null) {
				textSubtitle.setText(item.getMessage());
			} else {
				textSubtitle.setText("");
			}

		}

		return convertView;
	}
}
