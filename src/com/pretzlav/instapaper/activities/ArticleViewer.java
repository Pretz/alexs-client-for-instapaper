package com.pretzlav.instapaper.activities;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;

import com.pretzlav.instapaper.R;
import com.pretzlav.instapaper.api.ApiRequest;
import com.pretzlav.instapaper.application.InstaApper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class ArticleViewer extends Activity {
	
	public static final String EXTRA_BOOKMARK_ID = "bookmark_id";
	public static final String EXTRA_BOOKMARK_HASH= "bookmark_hash";
	
	InstaActivityHelper mHelper;
	WebView mWebView;
	File mFile;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new InstaActivityHelper(this);
        mWebView = new WebView(this);
        setContentView(mWebView);
        mFile = getFileForBookmark(getIntent().getIntExtra(EXTRA_BOOKMARK_ID, 0), getIntent().getStringExtra(EXTRA_BOOKMARK_HASH));
        if (mFile.exists()) {
        	setHtmlContent(mFile);
        } else {
        	refresh();	
        }
    }
	
	public Dialog onCreateDialog(int id) {
		return mHelper.onCreateDialog(id);
	}
	
	public void refresh() {
        BookmarkHtmlRequest request = new BookmarkHtmlRequest(this);
        request.execute(getIntent().getIntExtra(EXTRA_BOOKMARK_ID, 0));
        showDialog(InstaActivityHelper.DIALOG);
	}
	
	void setHtmlContent(String content) {
		mWebView.loadData(content, "text/html", "utf-8");
		dismissDialog(InstaActivityHelper.DIALOG);
	}
	
	void setHtmlContent(File file) {
		mWebView.loadUrl(file.toURI().toString());
	}
	
	void onError() {
		dismissDialog(InstaActivityHelper.DIALOG);
		Toast.makeText(this, "Error loading page.", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.article_viewer_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.archive_item:
			archive();
			return true;
		case R.id.refresh_item:
			refresh();
			return true;
		}
		return false;
	}
	
	public File getFileForBookmark(int bookmarkId, String hash) {
		File cacheFile = new File(((InstaApper)getApplication()).getPageCacheDir(), Integer.toString(bookmarkId));
		cacheFile.mkdirs();
		return new File(cacheFile, hash);
	}
	
	public File getBookmarkFile() {
		return mFile;
	}
	
	void archive() {
		showDialog(InstaActivityHelper.DIALOG);
		ArchiveHtmlRequest request = new ArchiveHtmlRequest(this);
		request.execute(getIntent().getIntExtra(EXTRA_BOOKMARK_ID, 0));
	}
	
	private static class ArchiveHtmlRequest extends AsyncTask<Integer, Void, String> {
		private ArticleViewer mActivity;
		private ApiRequest mRequest;
		
		public ArchiveHtmlRequest(ArticleViewer activity) {
			mActivity = activity;
		}
		
		@Override
		protected String doInBackground(Integer... bookmarkId) {
			List<BasicNameValuePair> params = Arrays
					.asList(new BasicNameValuePair("bookmark_id", Integer
							.toString(bookmarkId[0])));
			mRequest = new ApiRequest(
					"http://www.instapaper.com/api/1/bookmarks/archive",
					params, (InstaApper) mActivity.getApplication());
			return mRequest.executeAndRead();
		}
		
		@Override
		public void onPostExecute(String bookmark) {
			if (!TextUtils.isEmpty(bookmark)) {
				mActivity.setResult(RESULT_OK, mActivity.getIntent());
				mActivity.finish();
				Toast.makeText(mActivity, "Article moved to archive.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mActivity, "Error moving to archive. Please to be trying again.",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private static class BookmarkHtmlRequest extends AsyncTask<Integer, Void, File> {

		private ArticleViewer mActivity;
		private ApiRequest mRequest;
		
		public BookmarkHtmlRequest(ArticleViewer activity) {
			mActivity = activity;
		}
		
//		public void setApplication(ArticleViewer activity) {
//			mActivity = activity;
//		}
		
		@Override
		protected File doInBackground(Integer... bookmarkId) {
			List<BasicNameValuePair> params = Arrays
					.asList(new BasicNameValuePair("bookmark_id", Integer
							.toString(bookmarkId[0])));
			InstaApper app = (InstaApper) mActivity.getApplication();
			mRequest = new ApiRequest(
					"http://www.instapaper.com/api/1/bookmarks/get_text",
					params, app);
			InputStream response = mRequest.execute();
			File cacheFile = mActivity.getBookmarkFile();
			try {
				ApiRequest.readResponseToFile(response, cacheFile);
			} catch (FileNotFoundException e) {
				return null;
			}
			return cacheFile;
		}
		
		@Override
		public void onPostExecute(File location) {
			if (location != null && location.exists()) {
				mActivity.setHtmlContent(location);
				mActivity.dismissDialog(InstaActivityHelper.DIALOG);
			} else {
				mActivity.onError();
			}
		}
	}
}
