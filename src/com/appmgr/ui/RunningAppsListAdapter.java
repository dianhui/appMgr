package com.appmgr.ui;

import java.util.HashMap;
import java.util.List;

import com.appmgr.AppMgrUtils;
import com.appsmgr.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RunningAppsListAdapter extends BaseAdapter {

	private Context mCtx = null;
	private LayoutInflater mInflater = null;
	private List<ApplicationInfo> mAppsList = null;
	private HashMap<String, Boolean> mRunningAppsSelectState = null;
	
	public RunningAppsListAdapter(Context ctx, List<ApplicationInfo> appsList,
	        HashMap<String, Boolean> appsSelectStates) {
		mCtx = ctx.getApplicationContext();
		mInflater = LayoutInflater.from(ctx);
		mAppsList = appsList;
		mRunningAppsSelectState = appsSelectStates;
	}
	
	@Override
    public int getCount() {
	    return mAppsList.size();
    }

	@Override
    public Object getItem(int position) {
	    return mAppsList.get(position);
    }

	@Override
    public long getItemId(int position) {
	    return position;
    }

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.running_list_item, null);
			holder = new ViewHolder();
			holder.appIcon = (ImageView) convertView
			        .findViewById(R.id.imv_app_icon);
			holder.appName = (TextView) convertView
			        .findViewById(R.id.tv_app_name);
			holder.appMemUsage = (TextView) convertView
			        .findViewById(R.id.tv_mem_usage);
			holder.selectBox = (CheckBox) convertView
			        .findViewById(R.id.cb_select);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final ApplicationInfo appInfo = mAppsList.get(position);
		Drawable drawable = AppMgrUtils.getAppIcon(mCtx, appInfo.packageName);
		if (drawable != null) {
			holder.appIcon.setImageDrawable(drawable);
        } else {
        	//TODO.
        }
		
		holder.appName.setText(AppMgrUtils.getAppLabel(mCtx, appInfo.packageName));
		int usedMem = AppMgrUtils.getAppMemoryUsage(mCtx, appInfo.packageName);
		if (usedMem > 0) {
			holder.appMemUsage.setText(String.format("%.02f %s",
					usedMem / 1000.0f,
			        mCtx.getString(R.string.mb)));
        }
				
		holder.selectBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mRunningAppsSelectState.put(appInfo.packageName, isChecked);
				if (isChecked) {
					AppMgrUtils.removeAppFromWhiteList(mCtx, appInfo.packageName);
                } else {
                	AppMgrUtils.addAppToWhiteList(mCtx, appInfo.packageName);
                }
				
			}
		});
		if (mRunningAppsSelectState.get(appInfo.packageName) == null) {
			holder.selectBox.setChecked(false);
		} else {
			holder.selectBox.setChecked(mRunningAppsSelectState.get(appInfo.packageName));
		}
		
		convertView.setClickable(false);
		return convertView;
	}

	static class ViewHolder {
		ImageView appIcon;
		TextView appName;
		TextView appMemUsage;
		CheckBox selectBox;
    }

}
