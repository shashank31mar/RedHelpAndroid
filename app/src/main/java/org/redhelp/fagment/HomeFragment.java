package org.redhelp.fagment;

import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import org.redhelp.adapter.items.TabItem;
import org.redhelp.adapter.items.TabsItem;
import org.redhelp.app.HomeScreenActivity;
import org.redhelp.app.R;
import org.redhelp.common.SearchRequest;
import org.redhelp.common.SearchResponse;
import org.redhelp.common.types.GetBloodProfileType;
import org.redhelp.common.types.Location;
import org.redhelp.common.types.SearchItemTypes;
import org.redhelp.data.SearchPrefData;
import org.redhelp.location.LocationUtil;
import org.redhelp.task.SearchAsyncTask;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by harshis on 7/11/14.
 */
public class HomeFragment extends ProgressFragment implements TabsFragmentNew.ITabsFragment,
        SearchAsyncTask.ISearchAsyncTaskCaller, ErrorHandlerFragment.IErrorHandlerFragment,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private View fragmentContent;

    // Get current locaiton related stuff.
    private LocationClient mLocationClient;
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    TabsItem tabs;
    TabsFragmentNew tabsFragment;
    private SearchAsyncTask searchAsyncTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentContent = inflater.inflate(R.layout.fragment_home_layout, container, false);
        return super.onCreateView(inflater, container, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialiseViews();
        mLocationClient.connect();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mLocationClient.disconnect();
    }

    private void initialiseViews() {
        setContentView(fragmentContent);
        mLocationClient = new LocationClient(getActivity(), this, this);

    }



    private void fetchAndShowData(Long b_p_id, GetBloodProfileType get_type) {
        setContentShown(false);

    }

    public void showData(String name, String email, String phone_number, String blood_group, TabsItem tabs) {

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();


        TabsFragment tabsFragment = TabsFragment.createBloodProfileTabsFragmentInstance(tabs);
        transaction.add(R.id.fl_blood_profile_tabs_blood_profile_layout, tabsFragment);
        transaction.commit();
    }

    @Override
    public TabsItem getTabs() {
        return tabs;
    }

    private void createAndExecuteSearchAsyncTask() {
        SearchPrefData prefData = null;

        if (getActivity() instanceof HomeScreenActivity) {
            prefData = ((HomeScreenActivity) getActivity()).searchPrefData;
        } else {
            Log.e("HomeFragment", "Activity must implement HomeScreenActivity interface");
            return;
        }
        Location northEast = new Location();


        northEast.latitude = prefData.getNorthEastLocation().latitude;
        northEast.longitude = prefData.getNorthEastLocation().longitude;
        Location southWest = new Location();
        southWest.latitude = prefData.getSouthWestLocation().latitude;
        southWest.longitude = prefData.getSouthWestLocation().longitude;

        SearchRequest searchRequest = new SearchRequest();

        searchRequest.setSearchRequestType(prefData.getSearchRequestType());
        searchRequest.setSearchItems(prefData.getSearchItemTypes());
        searchRequest.setNorthEastLocation(northEast);
        searchRequest.setSouthWestLocation(southWest);
        searchAsyncTask = new SearchAsyncTask(this);
        searchAsyncTask.execute(searchRequest);
    }

    @Override
    public void handleSearchResult(SearchResponse searchResponse) {
        android.location.Location location = getLocation();
        HomeListFragment homeListFragment = HomeListFragment.createHomeListFragmentInstance(searchResponse);

        HomeMapFragment homeMapFragment = HomeMapFragment.createHomeMapFragmentInstance(searchResponse, location);

        LinkedList<TabItem> tabsItemList = new LinkedList<TabItem>();
        TabItem list_tab = new TabItem("Home", 1, homeListFragment, 12);
        TabItem home_map_tab = new TabItem("Map View", 2, homeMapFragment, 12);

        tabsItemList.add(list_tab);
        tabsItemList.add(home_map_tab);


        TabsItem tabs = new TabsItem();
        tabs.tabs = tabsItemList;

        this.tabs = tabs;

        showTabs();

    }

    private void showTabs() {

        if(this.isDetached())
            return;

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        tabsFragment = TabsFragmentNew.createTabsFragmentInstance(this);
        transaction.add(R.id.fl_content_home_layout, tabsFragment);
        transaction.commit();

        setContentShown(true);
    }

    @Override
    public void handleError(Exception e) {
        try {
            ErrorHandlerFragment errorHandlerFragment = ErrorHandlerFragment.createErrorHandlerFragmentInstance("", this);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame_main_screen, errorHandlerFragment);
            transaction.commit();
        } catch (Exception exp){

        }
    }

    @Override
    public void onRefreshButtonClickHandler() {
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame_main_screen, homeFragment);
        transaction.commit();

    }

    @Override
    public void onConnected(Bundle bundle) {
        getLocation();

        Set<SearchItemTypes> searchItemTypes = new HashSet<SearchItemTypes>();
        searchItemTypes.add(SearchItemTypes.EVENTS);
        searchItemTypes.add(SearchItemTypes.BLOOD_PROFILE);
        searchItemTypes.add(SearchItemTypes.BLOOD_REQUEST);
        createAndExecuteSearchAsyncTask();


    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
         /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            //LocationUtil.showErrorDialog(connectionResult.getErrorCode());
        }

    }

    /////////////////////////////////////////////////
    // Getting current location related stuff below//
    /////////////////////////////////////////////////
    private android.location.Location getLocation() {
        // If Google Play Services is available
        if (LocationUtil.servicesConnected(getActivity())) {
            // Get the current location
            android.location.Location currentLocation = mLocationClient.getLastLocation();
            if(currentLocation!=null)
                Log.e("HomeFragment,Location", currentLocation.getLatitude() + ":" + currentLocation.getLongitude());
            else
                Log.e("HomeFragment,Location", "currentLocation is null");
            return currentLocation;
        }
        return null;
    }
}

