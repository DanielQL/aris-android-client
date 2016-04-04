package edu.uoregon.casls.aris_android.tab_controllers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collection;

import edu.uoregon.casls.aris_android.GamePlayActivity;
import edu.uoregon.casls.aris_android.R;
import edu.uoregon.casls.aris_android.data_objects.Item;
import edu.uoregon.casls.aris_android.data_objects.Media;


public class InventoryViewFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

	private transient GamePlayActivity mGamePlayAct;
	public View mThisFragsView;

//	private OnFragmentInteractionListener mListener;

	public InventoryViewFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
//			mParam1 = getArguments().getString(ARG_PARAM1);
//			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mThisFragsView = inflater.inflate(R.layout.fragment_inventory_view, container, false);
		if (mGamePlayAct == null)
			mGamePlayAct = (GamePlayActivity) getActivity();

		this.updateList();
		return mThisFragsView;
	}

	public void updateList() {
		LinearLayout llInventoryList = (LinearLayout) mThisFragsView.findViewById(R.id.ll_inventory_list);
		llInventoryList.removeAllViews(); // refresh visible views so they don't accumulate
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		layoutParams.setMargins(0, -1, 0, -1);

		Collection<Item> listItem = mGamePlayAct.mGame.itemsModel.items().values();

		if (listItem == null || listItem.size() < 1) {
			TextView tvNoItemsMessage = new TextView(mGamePlayAct);
			tvNoItemsMessage.setText("No Active Quests");
			tvNoItemsMessage.setTextSize(getResources().getDimension(R.dimen.textsize_small));
			tvNoItemsMessage.setGravity(Gravity.CENTER_HORIZONTAL);
			tvNoItemsMessage.setPadding(0, 15, 0, 0);
			tvNoItemsMessage.setLayoutParams(layoutParams);
			llInventoryList.addView(tvNoItemsMessage);
		}
		// populate with active quests.
		else {
			for (Item item : listItem) {
				LayoutInflater inflater = (LayoutInflater) mGamePlayAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View itemView = inflater.inflate(R.layout.inventory_list_item, null);
				// icon/graphic
				WebView wvItemIcon = (WebView) itemView.findViewById(R.id.wv_inventory_item_icon);
				if (item.icon_media_id == 0) {
					wvItemIcon.setBackgroundColor(0x00000000);
					wvItemIcon.setBackgroundResource(R.drawable.logo_icon); //todo: default item icon here.
				}
				else {
					wvItemIcon.getSettings().setJavaScriptEnabled(false);
					wvItemIcon.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
					wvItemIcon.getSettings().setLoadWithOverviewMode(true); // causes the content (image) to fit into webview's window size.
					wvItemIcon.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
					Media itemIconMedia = mGamePlayAct.mGameMedia.get(item.icon_media_id);
					String item_icon_URL = itemIconMedia.mediaCD.remoteURL;
					String iconAsHtmlImg = "<html><body style=\"margin: 0; padding: 0\"><img src=\"" + item_icon_URL + "\" width=\"100%\" height=\"100%\"/></body></html>";
					wvItemIcon.loadData(iconAsHtmlImg, "text/html", null);
				}
				TextView tvItemName = (TextView) itemView.findViewById(R.id.tv_inventory_item_name);
				tvItemName.setText(item.name);
				TextView tvItemDesc = (TextView) itemView.findViewById(R.id.tv_inventory_item_desc);
				tvItemDesc.setText(item.desc);
				TextView tvItemQty = (TextView) itemView.findViewById(R.id.tv_inventory_item_qty);
//				tvItemQty.setText(item.); // todo: fix this.
				llInventoryList.addView(itemView);
			}
		}
	}


//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//		((GamePlayActivity) activity).onSectionAttached(
//				getArguments().getString(ARG_SECTION_NAME));
//		try {
//			mListener = (OnFragmentInteractionListener) activity;
//		} catch (ClassCastException e) {
//			throw new ClassCastException(activity.toString()
//					+ " must implement OnFragmentInteractionListener");
//		}
//	}

	@Override
	public void onDetach() {
		super.onDetach();
//		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
//	public interface OnFragmentInteractionListener {
//		// TODO: Update argument type and name
//		public void onFragmentInteraction(Uri uri);
//	}

}
