package com.jskierbi.app_template.modules;

import com.jskierbi.app_template.base.BaseFragment;
import com.jskierbi.app_template.ui.MainFragment;
import dagger.Module;

/**
 * Created by jakub on 01/27/2015.
 */
@Module(
		injects = {
				MainFragment.class
		},
		addsTo = ActivityModule.class
)
public class FragmentModule {

	private BaseFragment mFragment;

	public FragmentModule(BaseFragment fragment) {
		mFragment = fragment;
	}
}