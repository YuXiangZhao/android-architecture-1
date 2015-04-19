package com.jskierbi.commons.navigation;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jakub on 04/10/2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FragmentNavigation {

	@IdRes int fragmentContainerId();
	Class defaultFragmentClass();

	@IdRes int toolbarId() default 0;
	boolean doubleBackToExitEnabled() default false;
	@StringRes int doubleBackToExitText() default 0;

	@IdRes int primaryDrawerLayoutId() default 0;
	@IdRes int secondaryDrawerLayoutId() default 0;
}