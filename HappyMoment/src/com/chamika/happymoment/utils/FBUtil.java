package com.chamika.happymoment.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.chamika.happymoment.model.FBAlbum;
import com.chamika.happymoment.model.FBComment;
import com.chamika.happymoment.model.FBPerson;
import com.chamika.happymoment.model.FBPhoto;
import com.chamika.happymoment.model.TaggedDownload;
import com.chamika.happymoment.model.User;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;

public class FBUtil {

	public static final String PARAM_FIELDS = "fields";
	public static final String PARAM_AFTER = "after";
	public static final String PARAM_SINCE = "since";
	public static final String PARAM_UNTIL = "until";
	public static final String TAGGED_PHOTOS_API_CALL = "/me/photos";
	public static final String ALBUMS_API_CALL = "me/albums";
	public static final String PHOTOS_FIELDS = "id,created_time,picture,source,link,place{name},likes.summary(true).limit(0),comments.summary(true).limit(0)";
	public static final String ALBUMS_FIELDS = "id,name,created_time,link,count,updated_time,likes.summary(true).limit(1),comments.summary(true).limit(1)";
	public static final String COMMENTS_FIELDS = "from,message";

	public static Response getRequest(String graphUrl, String fields) {
		Map<String, String> params = new HashMap<String, String>();
		if (fields != null) {
			params.put(PARAM_FIELDS, fields);
		}
		return getRequest(graphUrl, params);
	}

	public static Response getRequest(String graphUrl, Map<String, String> parameters) {
		// Request request = new Request(getSession(), graphUrl, null,
		// HttpMethod.GET, null);

		Bundle params = new Bundle();
		if (parameters != null) {
			Iterator it = parameters.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pairs = (Entry<String, String>) it.next();
				params.putString(pairs.getKey(), pairs.getValue());
				it.remove(); // avoids a ConcurrentModificationException
			}

		}
		Request request = new Request(getSession(), graphUrl, params, HttpMethod.GET, null);
		return Request.executeAndWait(request);
	}

	private static Session getSession() {
		return Session.getActiveSession();
	}

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);

	public static FBPhoto convertTaggedPhotoJson(JSONObject json) {
		if (json == null) {
			return null;
		}

		try {
			FBPhoto photo = new FBPhoto();
			photo.setPhotoId(json.getString("id"));
			photo.setImageUrl(json.getString("source"));
			photo.setThumbnailUrl(json.getString("picture"));
			photo.setLink(json.getString("link"));

			try {
				photo.setComments(json.getJSONObject("comments").getJSONObject("summary").getInt("total_count"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				photo.setLikes(json.getJSONObject("likes").getJSONObject("summary").getInt("total_count"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				photo.setCreatedTime(dateFormat.parse(json.getString("created_time")));
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (json.has("place")) {
				photo.setLocation(json.getJSONObject("place").getString("name"));
			}

			return photo;
		} catch (JSONException e) {
			e.printStackTrace();

		}

		return null;
	}

	public static FBAlbum convertAlbumJson(JSONObject json) {
		if (json == null) {
			return null;
		}

		try {
			FBAlbum album = new FBAlbum();
			album.setAlbumId(json.getString("id"));
			album.setName(json.getString("name"));
			album.setCount(json.getInt("count"));
			album.setLink(json.getString("link"));

			try {
				album.setComments(json.getJSONObject("comments").getJSONObject("summary").getInt("total_count"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				album.setLikes(json.getJSONObject("likes").getJSONObject("summary").getInt("total_count"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				album.setCreatedTime(dateFormat.parse(json.getString("created_time")));
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				album.setUpdatedTime(dateFormat.parse(json.getString("updated_time")));
			} catch (Exception e) {
				e.printStackTrace();
			}

			return album;
		} catch (JSONException e) {
			e.printStackTrace();

		}

		return null;
	}

	public static FBComment convertCommentJson(JSONObject json) {
		if (json == null) {
			return null;
		}

		try {
			FBComment comment = new FBComment();

			JSONObject person = json.getJSONObject("from");

			comment.setId(person.getString("id"));
			comment.setName(person.getString("name"));
			comment.setMessage(json.getString("message"));

			return comment;
		} catch (JSONException e) {
			e.printStackTrace();

		}

		return null;
	}

	public static FBPerson convertLikeJson(JSONObject json) {
		if (json == null) {
			return null;
		}

		try {
			FBPerson person = new FBPerson();

			person.setId(json.getString("id"));
			person.setName(json.getString("name"));

			return person;
		} catch (JSONException e) {
			e.printStackTrace();

		}

		return null;
	}

	/**
	 * should be called in background thread
	 */
	public static String getProfilePicUrl() {

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("redirect", "false");
		params.put("type", "large");
		Response response = getRequest("/me/picture", params);

		if (response != null && response.getError() == null) {
			try {
				JSONObject jsonObject = response.getGraphObject().getInnerJSONObject();
				return jsonObject.getJSONObject("data").getString("url");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * should be called in background thred
	 * 
	 * @return
	 */
	public static String getCoverUrl() {

		Response response = getRequest("/me", "cover");

		if (response != null && response.getError() == null) {
			try {
				JSONObject jsonObject = response.getGraphObject().getInnerJSONObject();
				return jsonObject.getJSONObject("cover").getString("source");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static User getUser() {

		Response response = getRequest("/me", "name,cover,picture.type(large)");

		if (response != null && response.getError() == null) {
			try {
				User user = new User();
				JSONObject jsonObject = response.getGraphObject().getInnerJSONObject();
				try {
					String coverUrl = jsonObject.getJSONObject("cover").getString("source");
					user.setCoverUrl(coverUrl);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					String profileUrl = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url");
					user.setProfileUrl(profileUrl);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					String name = jsonObject.getString("name");
					user.setName(name);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return user;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static String getAlbumPhotosUrl(String albumId) {
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(albumId);
		sb.append("/photos");
		return sb.toString();
	}

	public static Map<String, String> getTaggedPhotoParams(TaggedDownload download) {

		HashMap<String, String> params = new HashMap<String, String>();
		params.put(PARAM_FIELDS, PHOTOS_FIELDS);

		if (download.getSince() != 0) {
			params.put(PARAM_SINCE, String.valueOf(download.getSince()));
		}

		if (download.getUntil() != 0) {
			params.put(PARAM_UNTIL, String.valueOf(download.getUntil()));
		}

		return params;
	}

	public static Map<String, String> getPhotoCommentParams() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(PARAM_FIELDS, COMMENTS_FIELDS);

		// if (afterId != null && afterId.length() > 0) {
		// params.put(PARAM_AFTER, afterId);
		// }

		return params;
	}

	public static String getPhotoCommentUrl(String photoId) {
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(photoId);
		sb.append("/comments");
		return sb.toString();
	}

	public static String getPhotoLikesUrl(String photoId) {
		StringBuilder sb = new StringBuilder();
		sb.append("/");
		sb.append(photoId);
		sb.append("/likes");
		return sb.toString();
	}

	public static String getNext(Response response) {
		try {
			final JSONObject json = response.getGraphObject().getInnerJSONObject();
			JSONObject cursors = json.getJSONObject("paging").getJSONObject("cursors");
			if (cursors.has("after")) {
				String nextId = cursors.getString("after");
				return nextId;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getProfileLink(String profileId) {
		StringBuilder sb = new StringBuilder();
		sb.append("https://www.facebook.com/");
		sb.append(profileId);
		return sb.toString();
	}
}
