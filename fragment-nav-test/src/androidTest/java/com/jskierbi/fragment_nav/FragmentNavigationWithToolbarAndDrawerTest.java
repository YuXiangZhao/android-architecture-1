package com.jskierbi.fragment_nav;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import com.jskierbi.commons.navigation.BackstackAdd;
import com.jskierbi.commons.navigation.FragmentNavigation;

import java.util.UUID;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static com.jskierbi.commons_espresso.ActivityConfigChangeUtils.getCurrentActivity;
import static com.jskierbi.commons_espresso.ActivityConfigChangeUtils.orientationChange;
import static com.jskierbi.commons_espresso.NavigationDrawerViewActions.closeDrawer;
import static com.jskierbi.commons_espresso.NavigationDrawerViewActions.openDrawer;
import static com.jskierbi.commons_espresso.NavigationDrawerViewMatcher.isDrawerOpen;
import static com.jskierbi.commons_espresso.NavigationDrawerViewMatcher.isDrawerVisible;
import static org.hamcrest.Matchers.not;


public class FragmentNavigationWithToolbarAndDrawerTest
		extends ActivityInstrumentationTestCase2<ActivityWithToolbarAndDrawer> {

	private static final String TAG = FragmentNavigationWithToolbarAndDrawerTest.class.getSimpleName();

	public FragmentNavigationWithToolbarAndDrawerTest() {
		super(ActivityWithToolbarAndDrawer.class);
	}

	@Override protected void setUp() throws Exception {
		super.setUp();
		getActivity();
	}

	@Override protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDefaultFragmentConfigChangeState() {

		final String KEY1 = "INSTANCE_STATE_ID";
		final String VALUE1 = UUID.randomUUID().toString();

		FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);

		{   // Set fragment parameter to be saved and then restored
			StateSavingFragment fragment = (StateSavingFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentById(fragmentNavigationAnnotation.fragmentContainerId());
			assertNotNull("Default fragment is properly added by ", fragment);
			// Set some parameters to be restored on fragment after orientation change
			fragment.setStateParameter(KEY1, VALUE1);
		}

		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		{   // Check, whether fragment state is restored properly
			StateSavingFragment fragment = (StateSavingFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentById(fragmentNavigationAnnotation.fragmentContainerId());
			assertNotNull("Default fragment is available", fragment);
			assertEquals("Default fragment: state is restored after orientation change", VALUE1, fragment.getStateParameter(KEY1));
		}
	}

	public void testNavigateTo() {
		final StateSavingFragment fragment = new StateSavingFragment();
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateTo(fragment);
			}
		});
		getInstrumentation().waitForIdleSync();
		assertEquals("Previous fragment added to backstack", 1, getActivity().getSupportFragmentManager().getBackStackEntryCount());

		// Orientation change
		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));
		assertEquals("Previous fragment added to backstack", 1, getActivity().getSupportFragmentManager().getBackStackEntryCount());
	}

	public void testNavigateBack() {
		final StateSavingFragment nextFragment = new StateSavingFragment();

		final String KEY1 = "STATE_KEY1";
		final String KEY2 = "STATE_KEY2";
		final String VALUE1 = UUID.randomUUID().toString();
		final String VALUE2 = UUID.randomUUID().toString();

		FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);

		{   // Set fragment parameter to be saved and then restored
			StateSavingFragment fragment = (StateSavingFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentById(fragmentNavigationAnnotation.fragmentContainerId());
			assertNotNull("Default fragment is properly added by ", fragment);
			// Set some parameters to be restored on fragment after orientation change
			fragment.setStateParameter(KEY1, VALUE1);
		}

		// Navigate to fragment
		nextFragment.setStateParameter(KEY2, VALUE2);
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateTo(nextFragment);
			}
		});
		getInstrumentation().waitForIdleSync();
		assertEquals("Previous fragment added to backstack", 1, getActivity().getSupportFragmentManager().getBackStackEntryCount());
		assertSame("Navigated to proper fragment",
				nextFragment,
				getActivity().getSupportFragmentManager().findFragmentById(fragmentNavigationAnnotation.fragmentContainerId()));

		// Change orientation
		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));
		assertEquals("Previous fragment added to backstack", 1, getActivity().getSupportFragmentManager().getBackStackEntryCount());

		{
			StateSavingFragment fragment = (StateSavingFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentById(fragmentNavigationAnnotation.fragmentContainerId());
			assertNotNull("navigated to proper fragment", fragment);
			assertEquals("Fragment state restored properly", VALUE2, fragment.getStateParameter(KEY2));
		}

		// Navigate back
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateBack();
			}
		});
		getInstrumentation().waitForIdleSync();
		assertEquals("Previous fragment popped from backstack", 0, getActivity().getSupportFragmentManager().getBackStackEntryCount());

		{
			StateSavingFragment fragment = (StateSavingFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentById(fragmentNavigationAnnotation.fragmentContainerId());
			assertNotNull("Navigated back to original fragment", fragment);
			assertEquals("Original fragment sstate restored properly", VALUE1, fragment.getStateParameter(KEY1));
		}
	}

	public void testNavigateNoBackstack() {

		final String KEY = "VALUE_KEY";
		final String VALUE1 = "VALUE_1";
		final String VALUE2 = "VALUE_2";
		final String VALUE3 = "VALUE_3";

		FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);

		final StateSavingFragment fragment1 = new StateSavingFragment();
		fragment1.setStateParameter(KEY, VALUE1);
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateTo(fragment1, BackstackAdd.NO);
			}
		});
		getInstrumentation().waitForIdleSync();
		assertEquals("Backstack is empty", 0, getActivity().getSupportFragmentManager().getBackStackEntryCount());

		final StateSavingFragment fragment2 = new StateSavingFragment();
		fragment2.setStateParameter(KEY, VALUE2);
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateTo(fragment2, BackstackAdd.YES);
			}
		});
		getInstrumentation().waitForIdleSync();
		assertEquals("Backstack has 1 entry", 1, getActivity().getSupportFragmentManager().getBackStackEntryCount());

		// Change orientation
		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		final StateSavingFragment fragment3 = new StateSavingFragment();
		fragment3.setStateParameter(KEY, VALUE3);
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateTo(fragment3, BackstackAdd.NO);
			}
		});
		getInstrumentation().waitForIdleSync();

		// Change orientation
		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		assertEquals("Backstack has 1 entry", 1, getActivity().getSupportFragmentManager().getBackStackEntryCount());
		{
			StateSavingFragment fragment = (StateSavingFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentById(fragmentNavigationAnnotation.fragmentContainerId());
			assertNotNull("navigated to proper fragment", fragment);
			assertEquals("Fragment state restored properly", VALUE3, fragment.getStateParameter(KEY));
		}

		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateBack();
			}
		});
		getInstrumentation().waitForIdleSync();

		assertEquals("Backstack is empty", 0, getActivity().getSupportFragmentManager().getBackStackEntryCount());
		{
			StateSavingFragment fragment = (StateSavingFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentById(fragmentNavigationAnnotation.fragmentContainerId());
			assertNotNull("navigated to proper fragment", fragment);
			assertEquals("Fragment state restored properly", VALUE1, fragment.getStateParameter(KEY));
		}
	}

	public void testNavigateClearBackstack() {

		final String KEY = "VALUE_KEY";
		final String VALUE1 = "VALUE_1";
		final String VALUE2 = "VALUE_2";
		final String VALUE3 = "VALUE_3";

		FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);

		{   // Set fragment parameter to be saved and then restored
			StateSavingFragment fragment = (StateSavingFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentById(fragmentNavigationAnnotation.fragmentContainerId());
			assertNotNull("Default fragment is properly added by ", fragment);
			// Set some parameters to be restored on fragment after orientation change
			fragment.setStateParameter(KEY, VALUE1);
		}

		// Navigate to fragment
		final StateSavingFragment secondFragment = new StateSavingFragment();
		final StateSavingFragment thirdFragment = new StateSavingFragment();
		final StateSavingFragment fourthFragment = new StateSavingFragment();
		final StateSavingFragment fifthFragment = new StateSavingFragment();
		secondFragment.setStateParameter(KEY, VALUE2);
		fifthFragment.setStateParameter(KEY, VALUE3);
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateTo(secondFragment, BackstackAdd.NO);
				getActivity().getFragmentNavigationController().navigateTo(thirdFragment);
				getActivity().getFragmentNavigationController().navigateTo(fourthFragment, BackstackAdd.NO);
				getActivity().getFragmentNavigationController().navigateTo(fifthFragment);
				getActivity().getSupportFragmentManager().executePendingTransactions();
			}
		});
		getInstrumentation().waitForIdleSync();
		assertEquals("Previous fragment added to backstack", 2, getActivity().getSupportFragmentManager().getBackStackEntryCount());
		assertSame("Navigated to proper fragment",
				fifthFragment,
				getActivity().getSupportFragmentManager().findFragmentById(fragmentNavigationAnnotation.fragmentContainerId()));

		// Change orientation
		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		// Check if fragment is properly restored
		assertEquals("Previous fragment added to backstack", 2, getActivity().getSupportFragmentManager().getBackStackEntryCount());
		{
			StateSavingFragment fragment = (StateSavingFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentById(fragmentNavigationAnnotation.fragmentContainerId());
			assertNotNull("Navigated back to original fragment", fragment);
			assertEquals("Original fragment sstate restored properly", VALUE3, fragment.getStateParameter(KEY));
		}

		// Clear backstack!!!
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().clearBackstack();
			}
		});
		getInstrumentation().waitForIdleSync();
		assertEquals("Previous fragment popped from backstack", 0, getActivity().getSupportFragmentManager().getBackStackEntryCount());
		{
			StateSavingFragment fragment = (StateSavingFragment) getActivity()
					.getSupportFragmentManager()
					.findFragmentById(fragmentNavigationAnnotation.fragmentContainerId());
			assertNotNull("Navigated back to original fragment", fragment);
			assertEquals("Original fragment sstate restored properly", VALUE2, fragment.getStateParameter(KEY));
		}
	}

	public void testNavigationDrawer() {
		// @see testing navigation drawer:
		//      https://groups.google.com/forum/#!topic/android-test-kit-discuss/bmLQUlcI5U4
		final FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);
		assertTrue("Drawer layout defined in @FragmentNavigation", 0 != fragmentNavigationAnnotation.drawerLayoutId());

		onView(withId(fragmentNavigationAnnotation.drawerLayoutId()))
				.check(matches(not(isDrawerVisible(GravityCompat.START))))
				.check(matches(not(isDrawerOpen(GravityCompat.START))))
				.perform(openDrawer(GravityCompat.START))
				.check(matches(isDrawerVisible(GravityCompat.START)))
				.check(matches(isDrawerOpen(GravityCompat.START)));

		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		onView(withId(fragmentNavigationAnnotation.drawerLayoutId()))
				.check(matches(isDrawerVisible(GravityCompat.START)))
				.check(matches(isDrawerOpen(GravityCompat.START)))
				.perform(closeDrawer(GravityCompat.START))
				.check(matches(not(isDrawerVisible(GravityCompat.START))))
				.check(matches(not(isDrawerOpen(GravityCompat.START))));

		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		onView(withId(fragmentNavigationAnnotation.drawerLayoutId()))
				.check(matches(not(isDrawerVisible(GravityCompat.START))))
				.check(matches(not(isDrawerOpen(GravityCompat.START))));
	}

	public void testBackActionToolbarIntegration() {

		final FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);

		// Set navigate up content description, so we can retrive proper view in next steps
		final StateSavingFragment detailFragment = new StateSavingFragment();
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateTo(detailFragment);
				// Set content description on toggle, so we can find it later
				getActivity().getDrawerToggleDelegate().setActionBarUpIndicator(
						getActivity().getV7DrawerToggleDelegate().getThemeUpIndicator(),
						R.string.navigate_back);
			}
		});
		getInstrumentation().waitForIdleSync();
		assertEquals("Backstack has 1 entry", 1, getActivity().getSupportFragmentManager().getBackStackEntryCount());

		// Click back on toolbar
		onView(withContentDescription(getActivity().getString(R.string.navigate_back)))
				.perform(click());
		getInstrumentation().waitForIdleSync();
		assertEquals("Navigate back performed properly via toolbar home icon", 0, getActivity().getSupportFragmentManager().getBackStackEntryCount());

		// Check if drawer is available after navigating back
		onView(withContentDescription(getActivity().getString(R.string.open_drawer)))
				.perform(click());
		onView(withId(fragmentNavigationAnnotation.drawerLayoutId()))
				.check(matches(isDrawerOpen(GravityCompat.START)))
				.perform(closeDrawer(GravityCompat.START))
				.check(matches(not(isDrawerOpen(GravityCompat.START))));

		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateTo(new StateSavingFragment(), BackstackAdd.NO);
			}
		});
		getInstrumentation().waitForIdleSync();

		// Check if drawer is still available (no backstack entry added)
		onView(withContentDescription(getActivity().getString(R.string.open_drawer)))
				.perform(click());
		onView(withId(fragmentNavigationAnnotation.drawerLayoutId()))
				.check(matches(isDrawerOpen(GravityCompat.START)))
				.perform(closeDrawer(GravityCompat.START))
				.check(matches(not(isDrawerOpen(GravityCompat.START))));

		// Navigate somewhere
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().getFragmentNavigationController().navigateTo(new StateSavingFragment());
				getActivity().getFragmentNavigationController().navigateTo(new StateSavingFragment());
			}
		});
		getInstrumentation().waitForIdleSync();

		// Change orientation
		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		assertEquals("Navigated to details properly", 2, getActivity().getSupportFragmentManager().getBackStackEntryCount());

		// Check if navigate back via toolbar still works
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				// Set content description on toggle, so we can find it later
				getActivity().getDrawerToggleDelegate().setActionBarUpIndicator(
						getActivity().getV7DrawerToggleDelegate().getThemeUpIndicator(),
						R.string.navigate_back);
			}
		});
		onView(withContentDescription(getActivity().getString(R.string.navigate_back)))
				.perform(click());
		getInstrumentation().waitForIdleSync();

		assertEquals("Navigate back performed ok", 1, getActivity().getSupportFragmentManager().getBackStackEntryCount());

		// Change orientation
		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		// Check if navigate back via toolbar still works
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				// Set content description on toggle, so we can find it later
				getActivity().getDrawerToggleDelegate().setActionBarUpIndicator(
						getActivity().getV7DrawerToggleDelegate().getThemeUpIndicator(),
						R.string.navigate_back);
			}
		});
		onView(withContentDescription(getActivity().getString(R.string.navigate_back)))
				.perform(click());
		getInstrumentation().waitForIdleSync();

		assertEquals("Navigate back performed ok", 0, getActivity().getSupportFragmentManager().getBackStackEntryCount());

		// Change orientation
		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		// Check if drawer toggle still opens nav drawer
		onView(withContentDescription(getActivity().getString(R.string.open_drawer)))
				.perform(click());
		onView(withId(fragmentNavigationAnnotation.drawerLayoutId()))
				.check(matches(isDrawerOpen(GravityCompat.START)))
				.perform(closeDrawer(GravityCompat.START))
				.check(matches(not(isDrawerOpen(GravityCompat.START))));

	}
	public void testNavDrawerToolbarIntegration() {

		final FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);
		assertTrue("Drawer layout defined in @FragmentNavigation", 0 != fragmentNavigationAnnotation.drawerLayoutId());

		// Open drawer via toggle
		onView(withContentDescription(getActivity().getString(R.string.open_drawer)))
				.perform(click());
		onView(withId(fragmentNavigationAnnotation.drawerLayoutId()))
				.check(matches(isDrawerOpen(GravityCompat.START)));

		// Change orientation
		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		// Close drawer via toggle
		onView(withContentDescription(getActivity().getString(R.string.close_drawer)))
				.perform(click());
		onView(withId(fragmentNavigationAnnotation.drawerLayoutId()))
				.check(matches(not(isDrawerOpen(GravityCompat.START))));
	}

	public void testNavDrawerRightToolbarIntegration() {

		final FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);
		assertTrue("Drawer layout defined in @FragmentNavigation", 0 != fragmentNavigationAnnotation.drawerLayoutId());

		onView(withId(fragmentNavigationAnnotation.drawerLayoutId()))
				.perform(openDrawer(GravityCompat.END))
				.check(matches(isDrawerOpen(GravityCompat.END)))
				.check(matches(isDrawerVisible(GravityCompat.END)));

		// Try close END drawer with toolbar action
		onView(withContentDescription(getActivity().getString(R.string.close_drawer)))
				.perform(click());

		onView(withId(fragmentNavigationAnnotation.drawerLayoutId()))
				.check(matches(not(isDrawerOpen(GravityCompat.END))))
				.check(matches(not(isDrawerVisible(GravityCompat.END))));
	}

	public void testDoubleBackToExit() {
		final FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);
		assertTrue("Double back to exit text is defined", 0 != fragmentNavigationAnnotation.doubleBackToExitWithText());

		Espresso.pressBack();
		getInstrumentation().waitForIdleSync();

		// Check if toast appeared
		onView(withText(fragmentNavigationAnnotation.doubleBackToExitWithText()))
				.inRoot(withDecorView(not(getActivity().getWindow().getDecorView())))
				.check(matches(isDisplayed()));

	}

	public void testDoubleBackToExitMultipleActivities() {
		final FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);
		assertTrue("Double back to exit text is defined", 0 != fragmentNavigationAnnotation.doubleBackToExitWithText());

		final Activity oldActivity = getActivity();
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getActivity().startActivity(new Intent(getActivity(), ActivityWithToolbarAndDrawer.class));
			}
		});
		getInstrumentation().waitForIdleSync();
		setActivity(getCurrentActivity(getInstrumentation()));

		final Activity newActivity = getActivity();
		assertNotSame("New activity is started", oldActivity, newActivity);

		// Back to first activity
		Espresso.pressBack();
		getInstrumentation().waitForIdleSync();

		// Check if toast does not appeared
		onView(withText(fragmentNavigationAnnotation.doubleBackToExitWithText()))
				.check(doesNotExist());

		setActivity(getCurrentActivity(getInstrumentation()));
		assertNotSame("We've navigated back through activities", newActivity, getActivity());

		Espresso.pressBack();
		getInstrumentation().waitForIdleSync();

		// Check if toast appeared
		onView(withText(fragmentNavigationAnnotation.doubleBackToExitWithText()))
				.inRoot(withDecorView(not(getActivity().getWindow().getDecorView())))
				.check(matches(isDisplayed()));
	}

	public void testDraerOptionsEnableAlwaysToggleOnRoot() {

		final FragmentNavigation fragmentNavigationAnnotation = getActivity().getClass().getAnnotation(FragmentNavigation.class);
		assertNotNull("Activity is annotated with @FragmentNavigation", fragmentNavigationAnnotation);

		{ // Drawer enabled
			DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(fragmentNavigationAnnotation.drawerLayoutId());
			assertEquals("Drawer is unlocked", DrawerLayout.LOCK_MODE_UNLOCKED, drawerLayout.getDrawerLockMode(GravityCompat.START));
			assertEquals("Drawer is unlocked", DrawerLayout.LOCK_MODE_UNLOCKED, drawerLayout.getDrawerLockMode(GravityCompat.END));
		}

		// Navigate to fragment
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				StateSavingFragment fragment = new StateSavingFragment();
				getActivity().getFragmentNavigationController().navigateTo(fragment);
			}
		});
		getInstrumentation().waitForIdleSync();

		{ // Drawer enabled
			DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(fragmentNavigationAnnotation.drawerLayoutId());
			assertEquals("Drawer is unlocked", DrawerLayout.LOCK_MODE_UNLOCKED, drawerLayout.getDrawerLockMode(GravityCompat.START));
			assertEquals("Drawer is unlocked", DrawerLayout.LOCK_MODE_UNLOCKED, drawerLayout.getDrawerLockMode(GravityCompat.END));
		}

		onView(isRoot()).perform(orientationChange());
		setActivity(getCurrentActivity(getInstrumentation()));

		{ // Drawer enabled
			DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(fragmentNavigationAnnotation.drawerLayoutId());
			assertEquals("Drawer is unlocked", DrawerLayout.LOCK_MODE_UNLOCKED, drawerLayout.getDrawerLockMode(GravityCompat.START));
			assertEquals("Drawer is unlocked", DrawerLayout.LOCK_MODE_UNLOCKED, drawerLayout.getDrawerLockMode(GravityCompat.END));
		}
	}
}