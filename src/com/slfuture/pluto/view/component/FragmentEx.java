package com.slfuture.pluto.view.component;

import java.lang.reflect.Field;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 界面拓展
 */
public class FragmentEx extends Fragment {
	/**
	 * 界面创建
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Class<?> clazz = this.getClass();
		while(clazz.getName().contains("$")) {
			clazz = clazz.getSuperclass();
			if(null == clazz) {
				return super.onCreateView(inflater, container, savedInstanceState);
			}
		}
		com.slfuture.pluto.view.annotation.ResourceView activityView = clazz.getAnnotation(com.slfuture.pluto.view.annotation.ResourceView.class);
		if(null == activityView) {
			return super.onCreateView(inflater, container, savedInstanceState);
		}
		else {
			// Activity
			View result = inflater.inflate(activityView.id(), container, attachToRoot());
			// Control
			for(Field field : clazz.getFields()) {
				com.slfuture.pluto.view.annotation.ResourceView controlView = field.getAnnotation(com.slfuture.pluto.view.annotation.ResourceView.class);
				if(null == controlView) {
					continue;
				}
				try {
					field.set(this, result.findViewById(controlView.id()));
				}
				catch (IllegalAccessException e) {
					Log.e("pluto", "FragmentEx.onCreate() failed", e);
				}
				catch (IllegalArgumentException e) {
					Log.e("pluto", "FragmentEx.onCreate() failed", e);
				}
			}
			return result;
		}
	}

	/**
	 * 是否附着到根视图
	 * 
	 * @return 是否附着到根视图
	 */
	protected boolean attachToRoot() {
		return true;
	}
}
