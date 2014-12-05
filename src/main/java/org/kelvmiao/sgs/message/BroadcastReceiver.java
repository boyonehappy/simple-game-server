package org.kelvmiao.sgs.message;

import org.json.JSONObject;

/**
 * Created by kelvin on 3/12/14.
 */
public interface BroadcastReceiver {
    public void onBroadcast(JSONObject json);
}
