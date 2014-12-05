package org.kelvmiao.sgs.controller;

import org.json.JSONObject;
import org.kelvmiao.sgs.RoomServer;
import org.kelvmiao.sgs.message.BroadcastSender;
import org.kelvmiao.sgs.model.Player;
import org.kelvmiao.sgs.model.Room;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by kelvin on 3/12/14.
 */
public class RoomController extends BroadcastSender {
    public static class RoomException extends ControllerException{
        public RoomException(String msg){
            super(msg);
        }
    }

    private Room room;
    private ReadWriteLock roomLock = new ReentrantReadWriteLock();

    public RoomController(Room room){
        this.room = room;
    }

    public String getId() {
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return room.getId();
        }finally {
            lock.unlock();
        }
    }

    public String getStatus() {
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return room.getStatus();
        }finally {
            lock.unlock();
        }
    }

    public void setStatus(String status) {
        Lock lock = roomLock.writeLock();
        lock.lock();
        try {
            room.setStatus(status);
        }finally {
            lock.unlock();
        }
    }

    public String getGame() {
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return room.getGame();
        }finally {
            lock.unlock();
        }
    }

    public String getName() {
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return room.getName();
        }finally {
            lock.unlock();
        }
    }

    public int getMax_player() {
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return room.getMax_player();
        }finally {
            lock.unlock();
        }
    }

    public int getMin_player() {
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return room.getMin_player();
        }finally {
            lock.unlock();
        }
    }

    public JSONObject toJSON() {
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return room.toJSON();
        }finally {
            lock.unlock();
        }
    }

    public void addPlayer(PlayerController player) throws ControllerException {
        Lock lock = roomLock.writeLock();
        lock.lock();
        try {
            if (!room.getStatus().equals("waiting"))
                throw new RoomException("Room is not in waiting state");
            if (room.getPlayerList().size() >= room.getMax_player())
                throw new RoomException("Room has been full.");
            player.setStatus("room");
            player.setRoom(this);
            player.addToList(room.getPlayerList());
            if(player.isReady())
                player.setReady(false);
            broadcastInHall();
        }finally {
            lock.unlock();
        }
    }

    public void removePlayer(PlayerController player) throws RoomException {
        Lock lock = roomLock.writeLock();
        lock.lock();
        try {
            if (!room.getStatus().equals("waiting"))
                throw new RoomException("Room is not in waiting state");
            if (!player.removeFromList(room.getPlayerList()))
                throw new RoomException(String.format("Player %s is not in Room %s(%s)", player.getFullName(), room.getName(), room.getId()));
            broadcastInHall();
        }finally {
            lock.unlock();
        }
    }

    private void broadcastInHall() {
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            RoomServer.getInstance().getHall().broadcast("room-info", room.toJSON());
        }finally {
            lock.unlock();
        }
    }

    public boolean containsPlayer(PlayerController player){
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return player.inList(room.getPlayerList());
        }finally {
            lock.unlock();
        }
    }

    public int getPlayerListSize(){
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return room.getPlayerList().size();
        }finally {
            lock.unlock();
        }
    }

    public boolean addToList(List<Room> roomList){
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return roomList.add(room);
        }finally {
            lock.unlock();
        }
    }

    public boolean removeFromList(List<Room> roomList){
        Lock lock = roomLock.readLock();
        lock.lock();
        try {
            return roomList.remove(room);
        }finally {
            lock.unlock();
        }
    }

    public void startGame(PlayerController playerController) throws RoomException {
        Lock lock = roomLock.writeLock();
        lock.lock();
        try {
            if (!playerController.getStatus().equals("room"))
                throw new RoomException(String.format("Player %s is not in a room", playerController.getName()));
            if (playerController.indexInList(room.getPlayerList()) != 0)
                throw new RoomException(String.format("Player %s is not owner of Room %s", playerController.getName(), room.getName()));
            if (!room.getStatus().equals("waiting"))
                throw new RoomException("Room is not in waiting state");
            for (Player player : room.getPlayerList()) {
                if (!player.isReady())
                    throw new RoomException(String.format("Player %s is not ready yet.", player.getName()));
            }
            int current = getPlayerListSize(), min = getMin_player();
            if (current < min)
                throw new RoomException(String.format("No enough players, currently %d, but at least %d is needed", current, min));
            room.setStatus("playing");
            broadcastInHall();
        }finally {
            lock.unlock();
        }
    }

    public void terminateGame(PlayerController playerController) throws RoomException{
        Lock lock = roomLock.writeLock();
        lock.lock();
        try {
            if (!playerController.getStatus().equals("room"))
                throw new RoomException(String.format("Player %s is not in a room", playerController.getName()));
            if (!room.getStatus().equals("playing"))
                throw new RoomException("Room is not in playing state");
            broadcast("terminate-game-by", playerController.toSimpleJSON());
            endGame();
        }finally {
            lock.unlock();
        }
    }

    public void endGame() throws RoomException{
        Lock lock = roomLock.writeLock();
        lock.lock();
        try {
            if (!room.getStatus().equals("playing"))
                throw new RoomException("Room is not in playing state");
            for (Player player : room.getPlayerList()) {
                player.setReady(false);
            }
            room.setStatus("waiting");
            broadcastInHall();
        }finally {
            lock.unlock();
        }
    }
}
