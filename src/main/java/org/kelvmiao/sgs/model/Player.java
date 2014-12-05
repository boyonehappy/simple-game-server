package org.kelvmiao.sgs.model;

import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by kelvin on 2/12/14.
 */
public class Player {
    private String id;
    private String name;
    private String host;
    private String status;
    private boolean ready;
    private int port;

    public Player(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.ready = false;
        this.status = "hall";
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public boolean isReady() {
        return ready;
    }

    public int getPort() {
        return port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public JSONObject toSimpleJSON() {
        JSONObject playerObj = new JSONObject();
        playerObj.put("name", getName());
        playerObj.put("id",getId());
        playerObj.put("status",getStatus());
        return playerObj;
    }

    public JSONObject toJSON() {
        JSONObject playerObj = toSimpleJSON();
        playerObj.put("address", getHost());
        playerObj.put("port", getPort());
        playerObj.put("ready", isReady());
        return playerObj;
    }
}
