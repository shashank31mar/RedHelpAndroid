package org.redhelp.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.redhelp.app.R;
import org.redhelp.common.GetBloodProfileRequest;
import org.redhelp.common.GetBloodProfileResponse;
import org.redhelp.network.RestClientCall;

/**
 * Created by harshis on 8/16/14.
 */
public class ViewBloodProfileAsyncTask extends AsyncTask<GetBloodProfileRequest, Void, String> {
    private static final String TAG = "RedHelp:ViewBloodProfileAsyncTask";

    public interface IViewBloodProfileAsyncTaskListener {
        void handleViewBloodProfileError();
        void handleViewBloodProfileResponse(GetBloodProfileResponse response);
    }

    private Context ctx;
    private IViewBloodProfileAsyncTaskListener listener;

    private Exception e;
    public ViewBloodProfileAsyncTask(Context ctx, IViewBloodProfileAsyncTaskListener listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(GetBloodProfileRequest... getBloodProfileRequests) {
        Gson gson = new Gson();
        String json_response = null;
        try{
            String json_request = gson.toJson(getBloodProfileRequests[0]);
            json_response = RestClientCall.postCall("/bloodProfile/getBloodProfile", json_request);
        }catch(Exception e) {
            this.e = e;
            return null;
        }
        return json_response;
    }

    @Override
    protected void onPostExecute(String json_response) {
        Toast toast = Toast.makeText(ctx,"", Toast.LENGTH_SHORT);
        if(e != null) {
            Log.d(TAG, e.toString());
            toast.setText(R.string.toast_network_error);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        }
        else {
            Gson gson = new Gson();
            GetBloodProfileResponse response = null;
            try {
                response = gson.fromJson(json_response, GetBloodProfileResponse.class);
            }catch(Exception e) {
                Log.e(TAG, "Exception while deserializing :"+e.toString());
            }
            listener.handleViewBloodProfileResponse(response);
        }
    }
}
