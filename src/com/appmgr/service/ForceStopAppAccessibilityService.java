package com.appmgr.service;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.support.v4.accessibilityservice.AccessibilityServiceInfoCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

public class ForceStopAppAccessibilityService extends AccessibilityService {
	private static final String SERVICE_NAME = "com.symantec.testaccessibility/.ForceStopAppAccessibilityService";

	public static Boolean sMonitoring = false;
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (!sMonitoring) {
	        return;
        }
		
		AccessibilityRecordCompat record = AccessibilityEventCompat
		        .asRecord(event);
		AccessibilityNodeInfoCompat node = record.getSource();
		if (node == null)
			return;

		if (event.getClassName().equals(
		        "com.android.settings.applications.InstalledAppDetailsTop")) {
			ArrayList<AccessibilityNodeInfoCompat> buttons = new ArrayList<AccessibilityNodeInfoCompat>();
			findButtons(node, buttons);

			if (buttons.get(0).isEnabled()) {
				Log.d("XXX", buttons.get(0).getText() + " is enable.");
				buttons.get(0).performAction(
				        AccessibilityNodeInfoCompat.ACTION_CLICK);
			} else {
				Log.d("XXX", "Perform back key");
				this.performGlobalAction(GLOBAL_ACTION_BACK);
			}
		}

		if (event.getClassName().equals("android.app.AlertDialog")) {
			ArrayList<AccessibilityNodeInfoCompat> buttons = new ArrayList<AccessibilityNodeInfoCompat>();
			findButtons(node, buttons);
			Log.i("XXX", "click " + buttons.get(1).getText());
			buttons.get(1).performAction(
			        AccessibilityNodeInfoCompat.ACTION_CLICK);
		}

	}

	@Override
	public void onInterrupt() {
	}

	@Override
	public void onServiceConnected() {
		AccessibilityServiceInfo info = getServiceInfo();
		info.flags |= AccessibilityServiceInfoCompat.FLAG_REPORT_VIEW_IDS;
		setServiceInfo(info);
	}

	private void findButtons(AccessibilityNodeInfoCompat root,
	        ArrayList<AccessibilityNodeInfoCompat> buttons) {
		if (((String) root.getClassName()).endsWith("Button")) {
			buttons.add(root);
		}

		final int childCount = root.getChildCount();
		for (int i = 0; i < childCount; i++) {
			findButtons(root.getChild(i), buttons);
		}
	}

	public static Boolean isEnabled(Context ctx) {
		AccessibilityManager am = (AccessibilityManager) ctx
		        .getSystemService(Context.ACCESSIBILITY_SERVICE);
		List<AccessibilityServiceInfo> runningServices = am
		        .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
		for (AccessibilityServiceInfo service : runningServices) {
			if (SERVICE_NAME.equals(service.getId())) {
				return true;
			}
		}

		return false;
	}
}
