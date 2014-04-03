package com.appmgr.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.appmgr.AppMgrUtils;
import com.appmgr.service.ForceStopAppAccessibilityService;
import com.appsmgr.R;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final int IDX_RUNNING_TAB = 0;
	private static final int IDX_HIBERNATE_TAB = 1;
	
	private Button mRunningTabBtn = null;
	private Button mHibernateTabBtn = null;
	private ProgressBar mProgressBar = null;
	private FrameLayout mListsContainer = null;
	private LinearLayout mRunningLayout = null;
	private LinearLayout mHibernateLayout = null;
	private ListView mRunningListView = null;
	private ListView mHibernateListView = null;
	private Button mRefreshBtn = null;
	private Button mKillBtn = null;
	
	private int mCurrentTabIndex = -1;
	
	private ArrayList<ApplicationInfo> mRunningApps = null;
	private HashMap<String, Boolean> mRunningAppsSelectState = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mListsContainer = (FrameLayout)this.findViewById(R.id.lists_container);
		mRunningLayout = (LinearLayout)this.getLayoutInflater().inflate(R.layout.apps_list_view, null, false);
		mHibernateLayout = (LinearLayout)this.getLayoutInflater().inflate(R.layout.apps_list_view, null, false);
		mListsContainer.addView(mRunningLayout);
		mListsContainer.addView(mHibernateLayout);
		
		mProgressBar = (ProgressBar)this.findViewById(R.id.progress_bar);
		
		mRunningTabBtn = (Button)this.findViewById(R.id.tab_btn_running);
		mRunningTabBtn.setOnClickListener(new OnClickListener(){  
            public void onClick(View v) {
            	setCurrentTab(IDX_RUNNING_TAB);
            }
        });
		
		mHibernateTabBtn = (Button)this.findViewById(R.id.tab_btn_hibernate);
		mHibernateTabBtn.setOnClickListener(new OnClickListener(){  
            public void onClick(View v) {  
            	setCurrentTab(IDX_HIBERNATE_TAB);
            }
        });
		
		mKillBtn = (Button) this
		        .findViewById(R.id.btn_kill);
		mKillBtn.setOnClickListener(new OnClickListener() {
			@TargetApi(Build.VERSION_CODES.GINGERBREAD)
            @Override
			public void onClick(View v) {
				if (ForceStopAppAccessibilityService.isEnabled(getApplicationContext())) {
					ForceStopAppAccessibilityService.sMonitoring = true;
					forceStopApp();
				} else {
					AppMgrUtils.killApps(getApplicationContext(), getKillApps());
					new GetListItemsTask().execute(IDX_RUNNING_TAB);
				}
				
			}
		});
		
		mRefreshBtn = (Button) this
		        .findViewById(R.id.btn_refresh);
		mRefreshBtn.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
            	new GetListItemsTask().execute(IDX_RUNNING_TAB);
			}
		});
		
		Button enableServiceBtn = (Button)mHibernateLayout.findViewById(R.id.btn_enable_service);
		enableServiceBtn.setOnClickListener(new OnClickListener() {
            @Override
			public void onClick(View v) {
            	toEnableAccessiblity();
			}
		});
	}
	
	private void setCurrentTab(int tabIndx) {
		if (tabIndx == -1) {
			tabIndx = IDX_RUNNING_TAB;
        }
		
		mCurrentTabIndex = tabIndx;
		switch(tabIndx) {
		case IDX_RUNNING_TAB:
			mRefreshBtn.setVisibility(View.VISIBLE);
			mKillBtn.setVisibility(View.VISIBLE);
			mRunningLayout.setVisibility(View.VISIBLE);
			mHibernateLayout.setVisibility(View.GONE);
			
			mRunningTabBtn.setBackgroundResource(R.drawable.tabs_selected_default);
			mHibernateTabBtn.setBackgroundResource(R.drawable.tabs_unselected_default);
			new GetListItemsTask().execute(IDX_RUNNING_TAB);
        	break;
		case IDX_HIBERNATE_TAB:
			mRunningLayout.setVisibility(View.GONE);
			mHibernateLayout.setVisibility(View.VISIBLE);
			mRefreshBtn.setVisibility(View.GONE);
			mKillBtn.setVisibility(View.GONE);
			mRunningTabBtn.setBackgroundResource(R.drawable.tabs_unselected_default);
			mHibernateTabBtn.setBackgroundResource(R.drawable.tabs_selected_default);
			
			ListView lv = (ListView) mHibernateLayout
			        .findViewById(R.id.lv_apps_list);
			RelativeLayout enableServiceLayout = (RelativeLayout) mHibernateLayout
			        .findViewById(R.id.rl_enable_service_guide);
			if (ForceStopAppAccessibilityService.isEnabled(getApplicationContext())) {
				lv.setVisibility(View.VISIBLE);
				enableServiceLayout.setVisibility(View.GONE);
				
				new GetListItemsTask().execute(IDX_HIBERNATE_TAB);
            } else {
            	lv.setVisibility(View.GONE);
				enableServiceLayout.setVisibility(View.VISIBLE);
            }
			
			
        	break;
		default:
			mRunningLayout.setVisibility(View.VISIBLE);
			mHibernateLayout.setVisibility(View.GONE);
        	break;
		}
	}
	
	private void setupListView(int type, List<ApplicationInfo> appsList) {
		if (type == IDX_RUNNING_TAB) {
			mRunningApps = (ArrayList<ApplicationInfo>)appsList;
			setupRunningAppsSelectState();
			
			ListAdapter listAdapter = new RunningAppsListAdapter(this,
			        mRunningApps, mRunningAppsSelectState);
			mRunningListView = (ListView)mRunningLayout.findViewById(R.id.lv_apps_list);
			mRunningListView.setAdapter(listAdapter);
        } else {
        	mHibernateListView = (ListView)mHibernateLayout.findViewById(R.id.lv_apps_list);
			ListAdapter listAdapter = new HibernateAppsListAdapter(this,
			        appsList);
        	mHibernateListView.setAdapter(listAdapter);
        }
	}
	
	private List<ApplicationInfo> getListItems(int type) {
		List<ApplicationInfo> appItems = new ArrayList<ApplicationInfo>();
		if (type == IDX_RUNNING_TAB) {
	        appItems = AppMgrUtils.getRunningApps(this, false, false);
        } else {
			for (String packageName : AppMgrUtils.getHibernateList(this)) {
				ApplicationInfo appInfo = AppMgrUtils.getAppInfo(this, packageName);
				if (appInfo == null) {
					continue;
                }
				
				if (AppMgrUtils.isAppRunning(getApplicationContext(), packageName)) {
	                continue;
                }
				
				appItems.add(appInfo);
            }
        }
		
		return appItems;
	}
	
	private void setupRunningAppsSelectState() {
		List<String> whiteList = AppMgrUtils.getWhiteList(this);
		
		mRunningAppsSelectState = new HashMap<String, Boolean>(mRunningApps.size());
		for (ApplicationInfo app : mRunningApps) {
			if (whiteList.contains(app.packageName)) {
	            mRunningAppsSelectState.put(app.packageName, false);
            } else {
            	mRunningAppsSelectState.put(app.packageName, true);
            }
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (ForceStopAppAccessibilityService.isEnabled(getApplicationContext())) {
			setCurrentTab(mCurrentTabIndex);
        } else {
        	setCurrentTab(IDX_HIBERNATE_TAB);
        }
		
	}
	
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			forceStopApp();
        }
		
		if (requestCode == 2) {
			mCurrentTabIndex = -1;
			setCurrentTab(IDX_HIBERNATE_TAB);
        }
		
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void forceStopApp() {
		try {
			ApplicationInfo killApp = getNextKillApp();
			if (killApp == null) {
				new GetListItemsTask().execute(IDX_RUNNING_TAB);
				
				ForceStopAppAccessibilityService.sMonitoring = false;
				Toast.makeText(this, "All apps are force stopped.", Toast.LENGTH_LONG).show();
	            return;
            }
			mRunningApps.remove(killApp);
			AppMgrUtils.addAppToHibernateList(getApplicationContext(),
			        killApp.packageName);
			
			// Open the specific App Info page.
			Intent intent = new Intent(
			        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.parse("package:" + killApp.packageName));
			startActivityForResult(intent, 1);

		} catch (ActivityNotFoundException e) {
			// Open the generic Apps page.
			Intent intent = new Intent(
			        android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
			startActivityForResult(intent, 1);
		}
	}
	
	private ApplicationInfo getNextKillApp() {
		for (ApplicationInfo app : mRunningApps) {
	        if (mRunningAppsSelectState.get(app.packageName)) {
	            return app;
            }
        }
		
		return null;
	}
	
	private ArrayList<ApplicationInfo> getKillApps() {
		ArrayList<ApplicationInfo> appsList = new ArrayList<ApplicationInfo>();
		for (ApplicationInfo app : mRunningApps) {
	        if (!mRunningAppsSelectState.get(app.packageName)) {
	            continue;
            }
	        
	        appsList.add(app);
        }
		
		return appsList;
	}
	
	private void toEnableAccessiblity() {
		Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
		startActivityForResult(intent, 2);
	}
	
	private class GetListItemsTask extends AsyncTask<Integer, Integer, List<ApplicationInfo>> {
		private int type;
		
		@Override
		protected void onPreExecute() {
			mListsContainer.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
        }
		
		@Override
        protected List<ApplicationInfo> doInBackground(Integer... params) {
			type = params[0];
			return getListItems(type);
        }
		
		@Override
		protected void onPostExecute(List<ApplicationInfo> result) {
			mListsContainer.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			
			setupListView(type, result);
        } 
	}

}
