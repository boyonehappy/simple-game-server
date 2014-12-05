package org.kelvmiao.sgs.model;

import java.util.LinkedList;
import java.util.List;

public class Hall {
    private final List<Room> roomList = new LinkedList<>();
    private final List<Player> playerList = new LinkedList<>();

    public List<Room> getRoomList() {
        return roomList;
    }
    public List<Player> getPlayerList() {
        return playerList;
    }

    public Hall() {
    }
}