package org.redhelp.fagment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ListView;

import org.redhelp.adapter.BloodRequestListViewAdapter;
import org.redhelp.adapter.items.RequestItem;
import org.redhelp.app.R;
import org.redhelp.common.BloodRequestSearchResponse;
import org.redhelp.common.SearchResponse;
import org.redhelp.types.Constants;

import java.util.Set;

/**
 * Created by harshis on 7/13/14.
 */
public class BloodRequestListFragment  extends android.support.v4.app.ListFragment {
    public  static final String BUNDLE_BLOOD_REQUEST = "bundle_blood_request";

    public static BloodRequestListFragment createBloodRequestListFragmentInstance(SearchResponse searchResponse) {
        BloodRequestListFragment bloodRequestListFragment = new BloodRequestListFragment();
        Bundle content = new Bundle();
        content.putSerializable(BUNDLE_BLOOD_REQUEST, searchResponse);
        bloodRequestListFragment.setArguments(content);
        return bloodRequestListFragment;
    }

    private BloodRequestListViewAdapter bloodRequestListViewAdapter;


    @Override
    public void onStart() {
        super.onStart();

        Bundle data_passed = getArguments();
        Set<BloodRequestSearchResponse> bloodRequestSearchResponse = null;
        if(data_passed != null){
            try {
                SearchResponse searchResponse = (SearchResponse) data_passed.getSerializable(BUNDLE_BLOOD_REQUEST);
                bloodRequestSearchResponse = searchResponse.getSet_blood_requests();
            }catch (Exception e){
                return;
            }
        }

        if(bloodRequestSearchResponse != null) {
            bloodRequestListViewAdapter = createAdapter(bloodRequestSearchResponse);
            setListAdapter(bloodRequestListViewAdapter);
            ListView listView = getListView();
            listView.setDivider(getResources().getDrawable(R.drawable.list_divider));
        }




    }

    private BloodRequestListViewAdapter createAdapter(Set<BloodRequestSearchResponse> bloodRequestSearchResponse){
        BloodRequestListViewAdapter adapter = new BloodRequestListViewAdapter(getActivity());
        for(BloodRequestSearchResponse request : bloodRequestSearchResponse) {

            String blood_grps_str = "-";
            if(request.getBlood_grps_requested_str()!=null)
                blood_grps_str = request.getBlood_grps_requested_str();

            String venue = "-";
            if(request.getVenue()!=null)
                venue = request.getVenue();

            RequestItem requestItem = new RequestItem(request.getB_r_id(),
                    "2.0", request.getTitle(), venue, blood_grps_str);

            adapter.add(requestItem);
        }

        return adapter;

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        RequestItem requestItem = null;
        try {
            requestItem = (RequestItem) getListAdapter().getItem(position);
        }catch (Exception e){

        }
        if(requestItem != null) {

            Bundle data_to_pass = new Bundle();
            data_to_pass.putLong(Constants.BUNDLE_B_R_ID, requestItem.b_r_id);
            ViewBloodRequestFragment viewBloodRequestFragment = new ViewBloodRequestFragment();
            viewBloodRequestFragment.setArguments(data_to_pass);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame_main_screen, viewBloodRequestFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

    }
}