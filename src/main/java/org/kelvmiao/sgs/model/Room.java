package org.kelvmiao.sgs.model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kelvmiao.sgs.message.BroadcastSender;

import java.util.*;

/**
 * Created by kelvin on 2/12/14.
 */
public class Room {
    private String name;
    private String game;
    private String status;
    private String id;
    private int max_player, min_player;
    private List<Player> playerList;


    public String getName() {
        return name;
    }

    public String getGame() {
        return game;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public int getMax_player() {
        return max_player;
    }

    public int getMin_player() {
        return min_player;
    }

    public Room(String game, String name, int max_player, int min_player) {
        this.game = game;
        this.name = name;
        this.max_player = max_player;
        this.min_player = min_player;

        id = UUID.randomUUID().toString();
        playerList = new LinkedList<>();
        status = "waiting";
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public JSONObject toSimpleJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", getId());
        obj.put("name", getName());
        obj.put("game", getGame());
        obj.put("max_player", getMax_player());
        obj.put("min_player", getMin_player());
        obj.put("status", getStatus());
        JSONArray array = new JSONArray();
        for(Player player : playerList){
            array.put(player.toSimpleJSON());
        }
        obj.put("player_list", array);
        return obj;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", getId());
        obj.put("name", getName());
        obj.put("game", getGame());
        obj.put("max_player", getMax_player());
        obj.put("min_player", getMin_player());
        obj.put("status", getStatus());
        JSONArray array = new JSONArray();
        for(Player player : playerList){
            array.put(player.toJSON());
        }
        obj.put("player_list", array);
        return obj;
    }
}
