package org.kelvmiao.sgs.message;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kelvin on 3/12/14.
 */
public class BroadcastSender {
    private List<BroadcastReceiver> broadcastReceiverList = new LinkedList<>();

    public void registerBroadcastReceiver(BroadcastReceiver br){
        broadcastReceiverList.add(br);
    }

    public void removeBroadcastReceiver(BroadcastReceiver br){
        broadcastReceiverList.remove(br);
    }

    public void broadcast(String key, JSONObject value){
        JSONObject obj = new JSONObject();
        obj.put(key, value);
        broadcast(obj);
    }

    public void broadcast(JSONObject json){
        for(BroadcastReceiver br : broadcastReceiverList)
            br.onBroadcast(json);
    }
}
