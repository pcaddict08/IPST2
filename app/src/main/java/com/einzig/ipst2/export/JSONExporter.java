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

package com.einzig.ipst2.export;

import android.app.Activity;

import com.einzig.ipst2.portal.PortalAccepted;
import com.einzig.ipst2.portal.PortalRejected;
import com.einzig.ipst2.portal.PortalResponded;
import com.einzig.ipst2.portal.PortalSubmission;
import com.einzig.ipst2.util.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * @author Ryan Porterfield
 * @since 2017-07-27
 */
public class JSONExporter extends Exporter<Void, Void, Void> {

    public JSONExporter(Activity activity) {
        super(activity);
    }

    public JSONExporter(Activity activity, boolean acceptedOnly) {
        super(activity, acceptedOnly);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            FileWriter writer = getOutputWriter();
            writer.write(",\n");
            exportAccepted(writer);
            if (!getAcceptedOnly()) {
                writer.write("{\n");
                exportPending(writer);
                writer.write(",\n");
                exportRejected(writer);
                writer.write("\n}");
            }
        } catch (IOException e) {
            Logger.e("JSONExporter", e.toString());
        }
        return null;
    }

    private void exportAccepted(FileWriter writer) throws IOException {
        writer.write("\"acceptedPortals\": [\n");
        Vector<PortalAccepted> portals = getDb().getAllAccepted(getHelper().isSeerOnly());
        for (int i = 0; i < portals.size(); ++i) {
            if (i > 0)
                writer.write(",\n");
            writer.write(getAcceptedJSON(portals.get(i)));
        }
        writer.write("\n]");
    }

    private void exportPending(FileWriter writer) throws IOException {
        writer.write("\"pendingPortals\": [\n");
        Vector<PortalSubmission> portals = getDb().getAllPending(getHelper().isSeerOnly());
        for (int i = 0; i < portals.size(); ++i) {
            if (i > 0)
                writer.write(",\n");
            writer.write(getPendingJSON(portals.get(i)));
        }
        writer.write("\n]");
    }

    private void exportRejected(FileWriter writer) throws IOException {
        writer.write("\"rejectedPortals\": [\n");
        Vector<PortalRejected> portals = getDb().getAllRejected(getHelper().isSeerOnly());
        for (int i = 0; i < portals.size(); ++i) {
            if (i > 0)
                writer.write(",\n");
            writer.write(getRejectedJSON(portals.get(i)));
        }
        writer.write("\n]");
    }

    private String getAcceptedJSON(PortalAccepted portal) {
        String json = "{" + getBaseJSON(portal) + ", ";
        json += getRespondedJSON(portal) + "\", ";
        json += "\"liveAddress\": \"" + portal.getLiveAddress() + "\", ";
        json += "\"intelURL\": \"" + portal.getIntelLinkURL() + "\"}";
        return json;
    }

    private String getPendingJSON(PortalSubmission portal) {
        return  "{\n" + getBaseJSON(portal) + "}";
    }

    private String getRejectedJSON(PortalRejected portal) {
        String json = "{\n" + getBaseJSON(portal) + ", " + getRespondedJSON(portal) + ", ";
        json += "\"rejectionReason\": \"" + portal.getRejectionReason() + "\"}";
        return json;
    }

    private String getBaseJSON(PortalSubmission portal) {
        String json = "\"name\": \"" + portal.getName() + "\", ";
        json += "\"pictureURL\": \"" + portal.getPictureURL() + "\", ";
        json += "\"dateSubmitted\": \"";
        json += getHelper().getUIFormatter().print(portal.getDateSubmitted()) + "\"";
        return json;
    }

    private String getRespondedJSON(PortalResponded portal) {
        String json = "\"dateResponded\": \"";
        json += getHelper().getUIFormatter().print(portal.getDateResponded());
        json += "\"";
        return json;
    }

    @Override
    String getExportFileType() {
        return "json";
    }
}
