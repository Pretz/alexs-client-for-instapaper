package com.pretzlav.instapaper.activities;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pretzlav.instapaper.api.ApiRequest;
import com.pretzlav.instapaper.application.InstaApper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArticleList extends ListActivity {
    
	ArrayList<JSONObject> mBookmarks;
	BookmarksRequest mRequest;
	InstaActivityHelper mHelper;
	BookmarksAdapter mAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new InstaActivityHelper(this);
        mBookmarks = new ArrayList<JSONObject>();
        mRequest = (BookmarksRequest)getLastNonConfigurationInstance();
        if (mRequest != null) {
        	mRequest.setActivity(this);
        }
        mAdapter = new BookmarksAdapter(this, android.R.layout.simple_list_item_2,
        		mBookmarks);
        setListAdapter(mAdapter);
        loadPage();
    }
    
	void loadPage() {
		showDialog(InstaActivityHelper.DIALOG);
		mRequest = new BookmarksRequest(this);
		mRequest.execute(mBookmarks.size());
	}
	
	void loadComplete() {
		mAdapter.notifyDataSetChanged();
		dismissDialog(InstaActivityHelper.DIALOG);
	}
	
	void addBookmark(JSONObject bookmark) {
		mBookmarks.add(bookmark);
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		return mHelper.onCreateDialog(id);
	}
	
	public BookmarksRequest onRetainNonConfigurationInstance() {
		return mRequest;
	}
	
	public static class BookmarksAdapter extends ArrayAdapter<JSONObject> {
		
		public BookmarksAdapter(Context context, int textViewResourceId, List<JSONObject> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			String title = getItem(position).optString("title");
			if (title == null) {
				title = getItem(position).optString("url");
			}
			((TextView)view.findViewById(android.R.id.text1)).setText(title);
			((TextView)view.findViewById(android.R.id.text2)).setText(
					getItem(position).optString("description"));
			return view;
		}
	}
	
	private static class BookmarksRequest extends AsyncTask<Integer, Void, JSONArray> {
		private ArticleList mActivity;
		ApiRequest mRequest;
		
		public BookmarksRequest(ArticleList activity) {
			mActivity = activity;
		}
		
		public void setActivity(ArticleList activity) {
			mActivity = activity;
		}
		
		@Override
		protected JSONArray doInBackground(Integer... count) {
			List<BasicNameValuePair> params = Arrays
					.asList(new BasicNameValuePair("limit", Integer
							.toString(count[0])));
			mRequest = new ApiRequest("http://www.instapaper.com/api/1/bookmarks/list", params,
					(InstaApper) mActivity.getApplication());
			String response = mRequest.execute();
			if (!TextUtils.isEmpty(response)) {
				try {
					return new JSONArray(response);
				} catch (JSONException e) {
					/* bunnies */
				}
			}
			return null;
		}
		
		@Override
		public void onPostExecute(JSONArray objects) {
			for (int i = 0, j = objects.length(); i < j; i++) {
				JSONObject object = objects.optJSONObject(i);
				if (object != null && "bookmark".equals(object.optString("type"))) {
					mActivity.addBookmark(object);
				}
			}
			mActivity.loadComplete();
		}
	}
    
}