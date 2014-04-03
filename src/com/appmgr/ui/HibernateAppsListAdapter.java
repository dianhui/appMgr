package com.appmgr.ui;

import java.util.List;

import com.appmgr.AppMgrUtils;
import com.appsmgr.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HibernateAppsListAdapter extends BaseAdapter {

	private Context mCtx = null;
	private LayoutInflater mInflater = null;
	private List<ApplicationInfo> mAppsList = null;
	
	public HibernateAppsListAdapter(Context ctx, List<ApplicationInfo> appsList) {
		mCtx = ctx.getApplicationContext();
		mInflater = LayoutInflater.from(ctx);
		mAppsList = appsList;
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
			convertView = mInflater.inflate(R.layout.hibernate_list_item, null);
			holder = new ViewHolder();
			holder.appIcon = (ImageView) convertView
			        .findViewById(R.id.imv_app_icon);
			holder.appName = (TextView) convertView
			        .findViewById(R.id.tv_app_name);
			holder.appState = (TextView) convertView
			        .findViewById(R.id.tv_app_state);

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
		
		holder.appName.setText(AppMgrUtils.getAppLabel(mCtx,
		        appInfo.packageName));
		
		convertView.setClickable(true);
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PackageManager pMgr = mCtx.getPackageManager();
				Intent intent = pMgr
				        .getLaunchIntentForPackage(appInfo.packageName);

				if (intent == null) {
					Toast.makeText(mCtx, mCtx.getString(R.string.fail_to_start_app),
					        Toast.LENGTH_SHORT).show();
					return;
				}

				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				mCtx.startActivity(intent);
			}
		});
		return convertView;
	}

	static class ViewHolder {
		ImageView appIcon;
		TextView appName;
		TextView appState;
    }

}
