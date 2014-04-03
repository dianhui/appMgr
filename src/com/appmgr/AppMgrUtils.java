package com.appmgr;

import java.util.ArrayList;
import java.util.List;

import com.appmgr.DatabaseMgr.HibernateListColumn;
import com.appmgr.DatabaseMgr.WhiteListColumn;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Debug.MemoryInfo;
import android.util.Log;

public class AppMgrUtils {
	private static final String TAG = "AppHelper";

	/**
	 * get list of running applications.
	 * 
	 */
	public static ArrayList<ApplicationInfo> getRunningApps(Context ctx,
	        Boolean needSelf, Boolean needSystem) {
		ArrayList<ApplicationInfo> runningApps = new ArrayList<ApplicationInfo>();

		PackageManager packMgr = ctx.getPackageManager();
		ActivityManager activityMgr = (ActivityManager) ctx
		        .getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processList = activityMgr
		        .getRunningAppProcesses();
		ApplicationInfo selfAppInfo = ctx.getApplicationInfo();
		for (RunningAppProcessInfo rInfo : processList) {
			try {
				ApplicationInfo appInfo = packMgr.getApplicationInfo(
				        rInfo.processName, PackageManager.GET_META_DATA);
				
				if (appInfo.packageName.equals(selfAppInfo.packageName)
				        && !needSelf) {
					continue;
				}
				
				if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1
				        && !needSystem) {
					continue;
				}
				runningApps.add(appInfo);
			} catch (NameNotFoundException e) {
				Log.d(TAG, "Not found the app info: " + rInfo.processName);
			}
		}

		return runningApps;
	}

	/**
	 * kill the given application.
	 * 
	 * @param - ctx - appInfo, The given application
	 */
	public static void killApp(Context ctx, ApplicationInfo appInfo) {
		ActivityManager activityMgr = (ActivityManager) ctx
		        .getSystemService(Context.ACTIVITY_SERVICE);
		if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
			Log.d(TAG, appInfo + " is system app, just return.");
			return;
		}

		if (appInfo.packageName.equals(ctx.getApplicationInfo().packageName)) {
			Log.d(TAG, "Not kill this app.");
			return;
		}

		activityMgr.killBackgroundProcesses(appInfo.packageName);
	}

	/**
	 * kill the applications in the given list.
	 */
	public static void killApps(Context ctx, ArrayList<ApplicationInfo> appList) {
		if (appList == null || appList.size() == 0) {
			Log.d(TAG, "Input app list is empty.");
			return;
		}

		for (ApplicationInfo appInfo : appList) {
			killApp(ctx, appInfo);
		}
	}

	/**
	 * kill the background applications which are not in white list.
	 */
	public static void killBackgroundApps(Context ctx) {
		ArrayList<ApplicationInfo> appsList = getRunningApps(ctx, false, false);
		ArrayList<String> whiteList = getWhiteList(ctx);
		for (ApplicationInfo appInfo : appsList) {
			if (whiteList != null && !whiteList.isEmpty()
			        && whiteList.contains(appInfo.packageName)) {
				continue;
			}

			killApp(ctx, appInfo);
		}
	}

	/**
	 * add given package name into white list.
	 */
	public static void addAppToWhiteList(Context ctx, String packageName) {
		DatabaseMgr dbMgr = new DatabaseMgr(ctx);
		dbMgr.insertAppToWhiteList(packageName);
		dbMgr.closeDbHelper();
	}

	/**
	 * remove given package name from white list.
	 */
	public static void removeAppFromWhiteList(Context ctx, String packageName) {
		DatabaseMgr dbMgr = new DatabaseMgr(ctx);
		dbMgr.deleteAppFromWhiteList(packageName);
		dbMgr.closeDbHelper();
	}

	/**
	 * return application's package names in white list.
	 */
	public static ArrayList<String> getWhiteList(Context ctx) {
		Cursor cursor = new DatabaseMgr(ctx).queryWhiteList(new String[] {
		        WhiteListColumn.FIELD_ID, WhiteListColumn.FIELD_PACKAGE_NAME },
		        null, null, null);
		if (cursor == null) {
			Log.d(TAG, "White list is empty.");
			return null;
		}
		
		ArrayList<String> whiteList = new ArrayList<String>();
		while (cursor.moveToNext()) {
			String packName = cursor.getString(cursor
			        .getColumnIndex(WhiteListColumn.FIELD_PACKAGE_NAME));
	        whiteList.add(packName);
        }
		cursor.close();
		
		return whiteList;
	}
	
	/**
	 * add given app into hibernate list.
	 */
	public static void addAppToHibernateList(Context ctx, String packName) {
		DatabaseMgr dbMgr = new DatabaseMgr(ctx);
		dbMgr.insertAppToHibernateList(packName);
		dbMgr.closeDbHelper();
	}
	
	/**
	 * remove given app from hibernate list.
	 */
	public static void removeAppFromHibernateList(Context ctx, String packName) {
		DatabaseMgr dbMgr = new DatabaseMgr(ctx);
		dbMgr.deleteAppFromHibernateList(packName);
		dbMgr.closeDbHelper();
	}

	/**
	 * return application's package names in hibernate list.
	 */
	public static ArrayList<String> getHibernateList(Context ctx) {
		Cursor cursor = new DatabaseMgr(ctx).queryHibernateList(new String[] {
		        HibernateListColumn.FIELD_ID, HibernateListColumn.FIELD_PACKAGE_NAME },
		        null, null, null);
		if (cursor == null) {
			Log.d(TAG, "White list is empty.");
			return null;
		}
		
		ArrayList<String> hibernateList = new ArrayList<String>();
		while (cursor.moveToNext()) {
			String packName = cursor.getString(cursor
			        .getColumnIndex(HibernateListColumn.FIELD_PACKAGE_NAME));
			hibernateList.add(packName);
        }
		cursor.close();
		
		return hibernateList;
	}
	
	/**
	 * get the foreground application's package name.
	 */
	public static String getForegroundApp(Context ctx) {
		ActivityManager activityMgr = (ActivityManager) ctx
		        .getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = activityMgr
		        .getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		return componentInfo.getPackageName();
	}

	/**
	 * get app's label by given application.
	 */
	public static String getAppLabel(Context ctx, String packName) {
		PackageManager packMgr = ctx.getPackageManager();
		try {
			return (String) packMgr.getApplicationLabel(packMgr
			        .getApplicationInfo(packName, 0));
		} catch (NameNotFoundException e) {
			Log.d(TAG, "Not found the application label of: " + packName);
			return packName;
		}
	}

	/**
	 * get given app's icon
	 */
	public static Drawable getAppIcon(Context ctx, String packageName) {
		PackageManager packMgr = ctx.getPackageManager();
		try {
			return packMgr.getApplicationIcon(packageName);
		} catch (NameNotFoundException e) {
			Log.d(TAG, "Not found icon of: " + packageName);
			return null;
		}
	}

	/**
	 * get given app's memory usage in KB.
	 */
	public static int getAppMemoryUsage(Context ctx, String packName) {
		ActivityManager am = (ActivityManager) ctx
		        .getSystemService(Context.ACTIVITY_SERVICE);
		int pid = getProcessId(ctx, packName);
		if (pid < 0) {
			Log.d(TAG, "Failed to get process id of: " + packName);
			return -1;
        }
		
		int pids[] = { pid };
		MemoryInfo[] memoryInfos = am.getProcessMemoryInfo(pids);
		MemoryInfo memoryInfo = memoryInfos[0];

		return memoryInfo.getTotalPrivateDirty()
		        + memoryInfo.getTotalSharedDirty();
	}
	
	/**
	 * get given app's application info.
	 * */
	public static ApplicationInfo getAppInfo(Context ctx, String packName) {
		PackageManager packMgr = ctx.getPackageManager();
		try {
			return packMgr.getApplicationInfo(packName,
			        PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
			Log.d(TAG, "Application info not found: " + packName);
        }
		
		return null;
	}
	
	public static Boolean isAppRunning(Context ctx, String packName) {
		ArrayList<ApplicationInfo> appsList = getRunningApps(ctx, false, false);
		for (ApplicationInfo appInfo : appsList) {
	        if (appInfo.packageName.equals(packName)) {
	            return true;
            }
        }
		
		return false;
	}
	
	private static int getProcessId(Context ctx, String packName) {
		ActivityManager am = (ActivityManager) ctx
		        .getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processList = am
		        .getRunningAppProcesses();
		PackageManager packMgr = ctx.getPackageManager();
		for (RunningAppProcessInfo rInfo : processList) {
			try {
				ApplicationInfo appInfo = packMgr.getApplicationInfo(
				        rInfo.processName, PackageManager.GET_META_DATA);
				if (appInfo.packageName.equals(packName)) {
	                return rInfo.pid;
                }
				
            } catch (NameNotFoundException e) {
				Log.d(TAG, "Not found application info of: "
				        + rInfo.processName);
            }
			
        }
		return -1;
	}
	
}