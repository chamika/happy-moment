package com.chamika.happymoment.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.chamika.happymoment.R;
import com.chamika.happymoment.adapter.CommentsAdapter;
import com.chamika.happymoment.adapter.LikesAdapter;
import com.chamika.happymoment.model.FBComment;
import com.chamika.happymoment.model.FBPerson;
import com.chamika.happymoment.model.FBPhoto;
import com.chamika.happymoment.utils.CommonUtils;
import com.chamika.happymoment.utils.FBUtil;
import com.facebook.Response;
import com.survivingwithandroid.endlessadapter.EndlessListView;
import com.survivingwithandroid.endlessadapter.EndlessListView.EndlessListener;

public class ContactActivity extends ActionBarActivity {

	public static final String INTENT_EXTRA_PHOTO = "photo";

	private FBPhoto photo;

	private String commentsAfterId = "";
	private String likesAfterId = "";

	private EndlessListView listViewComments;
	private EndlessListView listViewLikes;

	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int screenWidth = (int) (metrics.widthPixels);

		getSupportActionBar().hide();

		activity = this;

		setContentView(R.layout.activity_contact);

		getWindow().setLayout(screenWidth, LayoutParams.MATCH_PARENT);

		boolean hasExtras = handleExtras(getIntent().getExtras());
		if (hasExtras) {
			initUI();
		} else {
			Toast.makeText(this, "No photo found", Toast.LENGTH_LONG).show();
			finish();
		}

	}

	private void initUI() {
		setupTabs();

		listViewComments = (EndlessListView) findViewById(R.id.contact_list_comments);
		listViewLikes = (EndlessListView) findViewById(R.id.contact_list_likes);

		EndlessListView.EndlessListener commentsListener = new EndlessListener() {

			@Override
			public void loadData() {
				loadCommentAsync();
			}
		};

		EndlessListView.EndlessListener likesListener = new EndlessListener() {

			@Override
			public void loadData() {
				loadLikesAsync();
			}
		};

		listViewComments.setListener(commentsListener);
		listViewComments.setLoadingView(R.layout.list_loading_row);

		listViewLikes.setListener(likesListener);
		listViewLikes.setLoadingView(R.layout.list_loading_row);

		listViewComments.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FBComment item = (FBComment) listViewComments.getAdapter().getItem(position);
				CommonUtils.startUrlApp(activity, FBUtil.getProfileLink(item.getId()));
			}
		});

		listViewLikes.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FBPerson item = (FBPerson) listViewLikes.getAdapter().getItem(position);
				CommonUtils.startUrlApp(activity, FBUtil.getProfileLink(item.getId()));
			}
		});

		loadCommentAsync();
		loadLikesAsync();
	}

	private boolean handleExtras(Bundle bundle) {
		boolean result = true;
		if (bundle == null) {
			return false;
		}
		if (bundle.containsKey(INTENT_EXTRA_PHOTO)) {
			photo = (FBPhoto) bundle.getSerializable(INTENT_EXTRA_PHOTO);
		} else {
			result = false;
		}

		return result;
	}

	private void setupTabs() {

		LayoutInflater inflater = LayoutInflater.from(this);

		TabHost tabHost = (TabHost) findViewById(R.id.contact_tabhost);
		TextView tab1Title = (TextView) inflater.inflate(R.layout.paybill_tab_heads, null, false);
		TextView tab2Title = (TextView) inflater.inflate(R.layout.paybill_tab_heads, null, false);

		tabHost.setup();

		TabSpec spec1 = tabHost.newTabSpec("TAB 1");
		spec1.setContent(R.id.contact_list_comments);
		spec1.setIndicator(tab1Title);

		TabSpec spec2 = tabHost.newTabSpec("TAB 2");
		spec2.setContent(R.id.contact_list_likes);
		spec2.setIndicator(tab2Title);

		tab1Title.setText("Comments " + getCountString(photo.getComments()));
		tab2Title.setText("Likes " + getCountString(photo.getLikes()));

		tabHost.getTabWidget().setDividerDrawable(null);

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
	}

	private void loadCommentAsync() {
		AsyncTask<String, Void, ArrayList<FBComment>> task = new AsyncTask<String, Void, ArrayList<FBComment>>() {

			@Override
			protected ArrayList<FBComment> doInBackground(String... params) {
				if (params.length == 1) {
					return loadComments(params[0], commentsAfterId);
				}

				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<FBComment> result) {
				super.onPostExecute(result);
				updateCommentsList(result);
			}

		};

		task.execute(photo.getPhotoId());
	}

	private void loadLikesAsync() {
		AsyncTask<String, Void, ArrayList<FBPerson>> task = new AsyncTask<String, Void, ArrayList<FBPerson>>() {

			@Override
			protected ArrayList<FBPerson> doInBackground(String... params) {
				if (params.length == 1) {
					return loadLikes(params[0], likesAfterId);
				}

				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<FBPerson> result) {
				super.onPostExecute(result);
				updateLikesList(result);
			}

		};

		task.execute(photo.getPhotoId());
	}

	private void updateCommentsList(ArrayList<FBComment> result) {
		if (result == null) {
			listViewComments.setLoadingFinished(true);
		} else {
			ListAdapter listAdapter = listViewComments.getAdapter();
			if (listAdapter == null) {
				listAdapter = new CommentsAdapter(this, result);
				listViewComments.setAdapter((CommentsAdapter) listAdapter);
			} else {
				listViewComments.addNewData(result);
			}
		}
	}

	private void updateLikesList(ArrayList<FBPerson> result) {
		if (result == null) {
			listViewLikes.setLoadingFinished(true);
		} else {
			ListAdapter listAdapter = listViewLikes.getAdapter();
			if (listAdapter == null) {
				listAdapter = new LikesAdapter(this, result);
				listViewLikes.setAdapter((LikesAdapter) listAdapter);
			} else {

				listViewLikes.addNewData(result);
			}
		}
	}

	private ArrayList<FBComment> loadComments(String photoId, String afterId) {

		Map<String, String> params = FBUtil.getPhotoCommentParams();

		if (afterId == null) {
			return null;
		} else {
			params.put(FBUtil.PARAM_AFTER, afterId);
		}
		final Response response = FBUtil.getRequest(FBUtil.getPhotoCommentUrl(photoId), params);
		ArrayList<FBComment> comments = handleCommentsRespnose(response);

		commentsAfterId = FBUtil.getNext(response);

		return comments;
	}

	private ArrayList<FBPerson> loadLikes(String photoId, String afterId) {

		Map<String, String> params = new HashMap<String, String>();
		if (afterId == null) {
			return null;
		} else {
			params.put(FBUtil.PARAM_AFTER, afterId);
		}
		final Response response = FBUtil.getRequest(FBUtil.getPhotoLikesUrl(photoId), params);
		ArrayList<FBPerson> comments = handleLikesRespnose(response);

		likesAfterId = FBUtil.getNext(response);

		return comments;
	}

	private ArrayList<FBComment> handleCommentsRespnose(Response response) {
		try {
			final JSONObject json = response.getGraphObject().getInnerJSONObject();
			final JSONArray dataJson = json.getJSONArray("data");

			final ArrayList<FBComment> comments = new ArrayList<FBComment>();

			for (int i = 0; i < dataJson.length(); i++) {
				final JSONObject photoJson = dataJson.getJSONObject(i);
				final FBComment comment = FBUtil.convertCommentJson(photoJson);

				if (comment != null) {
					comments.add(comment);
				}

			}

			return comments;

		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private ArrayList<FBPerson> handleLikesRespnose(Response response) {
		try {
			final JSONObject json = response.getGraphObject().getInnerJSONObject();
			final JSONArray dataJson = json.getJSONArray("data");

			final ArrayList<FBPerson> likes = new ArrayList<FBPerson>();

			for (int i = 0; i < dataJson.length(); i++) {
				final JSONObject photoJson = dataJson.getJSONObject(i);
				final FBPerson comment = FBUtil.convertLikeJson(photoJson);

				if (comment != null) {
					likes.add(comment);
				}

			}

			return likes;

		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private String getCountString(int count) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(count);
		sb.append(")");

		return sb.toString();
	}
}
