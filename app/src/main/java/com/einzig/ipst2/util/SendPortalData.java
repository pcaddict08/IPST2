/******************************************************************************
 * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                  *
 * Permission is hereby granted, free of charge, to any person obtaining a    *
 * copy of this software and associated documentation files (the "Software"), *
 * to deal in the Software without restriction, including without limitation  *
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,   *
 * and/or sell copies of the Software, and to permit persons to whom the      *
 * Software is furnished to do so, subject to the following conditions:       *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included in *
 * all copies or substantial portions of the Software.                        *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE*
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER     *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING    *
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER        *
 * DEALINGS IN THE SOFTWARE.                                                  *
 ******************************************************************************/

package com.einzig.ipst2.util;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.einzig.ipst2.database.DatabaseInterface;
import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalSubmission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;

/*
 * Created by Steven Foskett on 6/20/2017.
 */

public class SendPortalData extends AsyncTask<Void, Void, Void> {
    private Context context;

    public SendPortalData(Context context) {
        this.context = context;
    }

    protected Void doInBackground(Void... voids) {
        JSONArray objsToSend = new JSONArray();
        JSONObject finalObj = new JSONObject();
        DatabaseInterface db = new DatabaseInterface(context);
        PreferencesHelper helper = new PreferencesHelper(context);
        Vector<PortalSubmission> portalList = db.getAllPortals(helper.isSeerOnly());
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        for (PortalSubmission ps : portalList) {
            JSONObject newJSON = new JSONObject();
            try {
                if (ps.getDateSubmitted() != null)
                    newJSON.put("Date Submitted", sdf.format(ps.getDateSubmitted().toDate()));
                newJSON.put("Picture URL", ps.getPictureURL());
                if (ps instanceof PortalRejected) {
                    if (((PortalRejected) ps).getDateResponded() != null)
                        newJSON.put("Date Rejected", sdf.format(((PortalRejected) ps)
                                .getDateResponded().toDate()));
                    String rejecReason = "";
                    if (((PortalRejected) ps).getRejectionReason() != null)
                        rejecReason = ((PortalRejected) ps).getRejectionReason();
                    if (ps.getDateSubmitted() != null)
                        newJSON.put("Rejection Reason", rejecReason);
                } else if (ps instanceof PortalAccepted) {
                    if (((PortalAccepted) ps).getDateResponded() != null)
                        newJSON.put("Date Accepted", sdf.format(((PortalAccepted) ps)
                                .getDateResponded().toDate()));
                    if (ps.getDateSubmitted() != null)
                        newJSON = addLatLonToJSON(newJSON, (PortalAccepted) ps);
                }
                Logger.d("JSON OBJ: " + newJSON);
                objsToSend.put(newJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String urlString = "https://demaerschalck.eu/ingress/api/IPSTapp.php";
        String resultToDisplay = "";
        try {
            finalObj.put("Portals", objsToSend);
            finalObj.put("Date Pattern", "MM-dd-yyyy");
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("json", finalObj.toString());
            String query = builder.build().getEncodedQuery();
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    resultToDisplay += line;
                }
            } else {
                resultToDisplay = "fail";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "doInBackground: " + resultToDisplay);

        return null;
    }

    protected void onPostExecute(Void result) {
        Toast.makeText(this.context, "Finished Uploading Portal Data", Toast.LENGTH_SHORT).show();
    }

    private JSONObject addLatLonToJSON(JSONObject jsonObject, PortalAccepted ps) {
        if (ps.getIntelLinkURL() != null && !ps.getIntelLinkURL().equalsIgnoreCase("")) {
            String intelLink;
            try {
                intelLink = URLDecoder.decode(ps.getIntelLinkURL(), "UTF-8");
                String firstSub =
                        intelLink.substring(intelLink.indexOf("ll=") + 3, intelLink.length() - 1);
                String lat = firstSub.substring(0, firstSub.indexOf(","));
                String lon = firstSub.substring(firstSub.indexOf(",") + 1, firstSub.indexOf("&"));
                jsonObject.put("Lat", lat);
                jsonObject.put("Lon", lon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
}