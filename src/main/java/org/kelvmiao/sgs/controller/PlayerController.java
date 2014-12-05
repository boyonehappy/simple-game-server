package org.kelvmiao.sgs.controller;

import org.json.JSONObject;
import org.kelvmiao.sgs.model.Player;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by kelvin on 3/12/14.
 */
public class PlayerController {
    public static class PlayerException extends ControllerException{
        public PlayerException(String msg){
            super(msg);
        }
    }
    private Player player;
    private ReadWriteLock playerLock = new ReentrantReadWriteLock();
    private RoomController room;
    public PlayerController(Player player){
        this.player = player;
    }

    public boolean addToList(List<Player> playerList){
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return playerList.add(player);
        }finally {
            lock.unlock();
        }
    }
    public boolean removeFromList(List<Player> playerList){
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return playerList.remove(player);
        }finally {
            lock.unlock();
        }
    }

    public boolean inList(List<Player> playerList){
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return playerList.contains(player);
        }finally {
            lock.unlock();
        }
    }

    public int indexInList(List<Player> playerList){
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return playerList.indexOf(player);
        }finally {
            lock.unlock();
        }
    }

    public String getId() {
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return player.getId();
        }finally {
            lock.unlock();
        }
    }

    public void setRoom(RoomController room) {
        Lock lock = playerLock.writeLock();
        lock.lock();
        try {
            this.room = room;
        }finally {
            lock.unlock();
        }
    }

    public RoomController getRoom() {
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return room;
        }finally {
            lock.unlock();
        }
    }

    public String getName() {
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return player.getName();
        }finally {
            lock.unlock();
        }
    }

    public JSONObject toSimpleJSON() {
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return player.toSimpleJSON();
        }finally {
            lock.unlock();
        }
    }

    public boolean isReady() {
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return player.isReady();
        }finally {
            lock.unlock();
        }
    }

    public void setReady(boolean ready) throws PlayerException {
        Lock lock = playerLock.writeLock();
        lock.lock();
        try {
            if (!player.getStatus().equals("room"))
                throw new PlayerException(String.format("Player %s is not in a room", player.getName()));
            player.setReady(ready);
            JSONObject obj = new JSONObject();
            obj.put("player-ready", ready);
            obj.put("id", player.getId());
            room.broadcast(obj);
        }finally {
            lock.unlock();
        }
    }

    public int getPort() {
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return player.getPort();
        }finally {
            lock.unlock();
        }
    }

    public JSONObject toJSON() {
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return player.toJSON();
        }finally {
            lock.unlock();
        }
    }

    public String getStatus() {
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return player.getStatus();
        }finally {
            lock.unlock();
        }
    }

    public String getHost() {
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return player.getHost();
        }finally {
            lock.unlock();
        }
    }

    public void setStatus(String status) {
        Lock lock = playerLock.writeLock();
        lock.lock();
        try {
            player.setStatus(status);
        }finally {
            lock.unlock();
        }
    }

    public String getFullName(){
        Lock lock = playerLock.readLock();
        lock.lock();
        try {
            return String.format("%s(%s:%d)", player.getName(), player.getHost(), player.getPort());
        }finally {
            lock.unlock();
        }
    }
}
