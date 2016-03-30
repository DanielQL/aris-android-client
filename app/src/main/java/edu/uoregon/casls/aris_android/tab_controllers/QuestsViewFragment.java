package edu.uoregon.casls.aris_android.tab_controllers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uoregon.casls.aris_android.GamePlayActivity;
import edu.uoregon.casls.aris_android.R;


public class QuestsViewFragment extends Fragment {
	/**
	 *  The fragment argument representing the section number for this
	* fragment.
	*/

	private static final String ARG_SECTION_NUMBER = "section_number";
	private transient GamePlayActivity mGamePlayAct;

	/**
	 * Returns a new instance of this fragment for the given section
	 * number.
	 */
	public static QuestsViewFragment newInstance(int sectionNumber) {
		QuestsViewFragment fragment = new QuestsViewFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public static QuestsViewFragment newInstance(String sectionName) {
		QuestsViewFragment fragment = new QuestsViewFragment();
		Bundle args = new Bundle();
		args.putString(ARG_SECTION_NUMBER, sectionName);
		fragment.setArguments(args);
		return fragment;
	}

	// save the fragment's state vars. See: http://stackoverflow.com/a/17135346/1680968
//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		if (savedInstanceState != null) {
//			//Restore the fragment's state here
//		}
//	}
//
//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//
//	}

	public QuestsViewFragment() {
	}

	public void initContext(GamePlayActivity gamePlayAct) {
		mGamePlayAct = gamePlayAct;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_quests_view, container, false);
		if (mGamePlayAct == null)
			mGamePlayAct = (GamePlayActivity) getActivity();
		mGamePlayAct.showNavBar(); // fixme: NPE here when cycling through random tabs in nav bar. is mGamePlay reference getting lost?
		return rootView;
	}

//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//		((GamePlayActivity) activity).onSectionAttached(
//				getArguments().getInt(ARG_SECTION_NUMBER));
//	}

	public void onClickTabActiveQuests(View v) {
		"see udateAllViews() in GamesListActivity for previously working code for list building"
	}

	public void onClickTabCompletedQuests(View v) {

	}
}
