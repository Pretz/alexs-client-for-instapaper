package com.pretzlav.instapaper.activities;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pretzlav.instapaper.R;
import com.pretzlav.instapaper.api.ApiRequest;
import com.pretzlav.instapaper.application.InstaApper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArticleList extends ListActivity {
    
	public static final int ACTIVITY_VIEWER = 12;
	
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
        if (mRequest == null) {
            loadPage();
        }
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.bookmark_list_menu, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVITY_VIEWER && resultCode == RESULT_OK && 
				data.hasExtra(ArticleViewer.EXTRA_BOOKMARK_ID)) {
			int index = -1;
			int extra =  data.getIntExtra(ArticleViewer.EXTRA_BOOKMARK_ID, -1);
			for (int i = 0, j = mBookmarks.size(); i < j; i++) {
				if (extra == mBookmarks.get(i).optInt("bookmark_id", -2)) {
					index = i;
					break;
				}
			}
			if (index != -1) {
				mBookmarks.remove(index);
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.logout_item:
			logout();
			return true;
		case R.id.refresh_item:
			mBookmarks.clear();
			loadPage();
			return true;
		}
		return false;
	}
	
	void logout() {
		InstaApper app = (InstaApper)getApplication();
		app.saveOAuthToken(null);
		app.saveOAuthTokenSecret(null);
		startActivity(new Intent(this, Login.class));
		Toast.makeText(this, "You have logged out.", Toast.LENGTH_SHORT).show();
		finish();
	}
	
	@Override
	public BookmarksRequest onRetainNonConfigurationInstance() {
		return mRequest;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, ArticleViewer.class);
		JSONObject bookmark = mBookmarks.get(position);
		intent.putExtra(ArticleViewer.EXTRA_BOOKMARK_ID, 
				bookmark.optInt("bookmark_id"));
		intent.putExtra(ArticleViewer.EXTRA_BOOKMARK_HASH, bookmark.optString("hash"));
		startActivityForResult(intent, ACTIVITY_VIEWER);
	}
	
	private static class BookmarksAdapter extends ArrayAdapter<JSONObject> {
		
		public BookmarksAdapter(Context context, int textViewResourceId, List<JSONObject> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = LayoutInflater.from(parent.getContext())
					.inflate(android.R.layout.simple_list_item_2, null);
			} else {
				view = convertView;
			}
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
			String response = mRequest.executeAndRead();
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