package com.wowwee.snappetssampleproject.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.wowwee.snappetssampleproject.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FragmentHelper {
    private static List<WeakReference<Fragment>> fragments = new ArrayList<WeakReference<Fragment>>();

    private static ArrayList<String> backStackKeys = new ArrayList<String>();
    private static int backStackIndex = 0;

    public static void showWithoutSlideAnimation(FragmentManager fragmentManager, Fragment fragment, int containgViewId, boolean addToBackStack) {
        switchFragment(fragmentManager, fragment, containgViewId, addToBackStack, true);
    }

    public static void switchFragment(FragmentManager fragmentManager, Fragment fragment, int containViewId, boolean addToBackStack) {
        switchFragment(fragmentManager, fragment, containViewId, addToBackStack, false);
    }

    private static void switchFragment(FragmentManager fragmentManager, Fragment fragment, int containViewId, boolean addToBackStack, boolean isPopup) {
        //		for (WeakReference<Fragment> frag : fragments){
//			if (frag.get() != null && frag.get().getView() != null && (frag.get() instanceof UserContactListFragment)){
//				frag.get().getView().setClickable(false);
//				frag.get().getView().setFocusable(false);
//				frag.get().getView().setFocusableInTouchMode(false);
//				frag.get().getView().setEnabled(false);
//				frag.get().getView().setOnTouchListener(new View.OnTouchListener() {
//
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						// TODO Auto-generated method stub
//						return true;
//					}
//				});
//			}
//		}

        fragments.add(new WeakReference<Fragment>(fragment));

        if (fragment.getView() != null) {
            fragment.getView().setClickable(true);
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (addToBackStack) {
            backStackIndex++;
            String key = "" + backStackIndex;
            transaction.addToBackStack(key);
            backStackKeys.add(key);
        } else {
            transaction.addToBackStack(null);
        }


        if (!isPopup) {
            transaction.setCustomAnimations(
                    R.anim.fragment_slide_in_right,
                    R.anim.fragment_slide_out_left,
                    R.anim.fragment_slide_in_left,
                    R.anim.fragment_slide_out_right
            );
        }

        transaction.replace(containViewId, fragment, "" + backStackIndex);

        transaction.commitAllowingStateLoss();
    }

    public static boolean popBackStack(FragmentManager fragmentManager) {
        if (backStackKeys.size() > 0) {
            String popToKey = backStackKeys.get(backStackKeys.size() - 1);
            backStackKeys.remove(backStackKeys.size() - 1);
            fragmentManager.popBackStack(popToKey, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return true;
        } else {
            return false;
        }
    }

    public static Fragment getPreviousFragment(FragmentManager fragmentManager) {
        if (backStackKeys.size() > 1) {
            String backKey = backStackKeys.get(backStackKeys.size() - 2);
            return fragmentManager.findFragmentByTag(backKey);
        } else {
            return null;
        }
    }

    public static void popToRoot(FragmentManager fragmentManager) {
        if (backStackKeys.size() > 0) {
            String popToKey = backStackKeys.get(0);
            backStackKeys.clear();
            fragmentManager.popBackStack(popToKey, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public static void removeFragment(FragmentManager fragmentManager, int containViewId) {
        Fragment fragment = fragmentManager.findFragmentById(containViewId);
        if (fragment != null) {
            fragments.remove(fragment);

            // TODO: Remove from backStackKeys


            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragment);
            transaction.commit();
        }
    }

    public static void clearAllBackStackFragments(FragmentManager fragmentManager) {
        FragmentTransaction ft = fragmentManager.beginTransaction();

        for (WeakReference<Fragment> ref : fragments) {
            Fragment fragment = ref.get();
            if (fragment != null) {
                ft.remove(fragment);
            }
        }
        fragments.clear();
        backStackKeys.clear();

        ft.commit();
    }

    public static Fragment getCurrentFragment() {
        Fragment fragment = null;
        if (fragments.size() > 0) {
            fragment = (fragments.get((fragments.size() - 1))).get();
        }
        return fragment;
    }
}
