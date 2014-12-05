package org.kelvmiao.sgs.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kelvmiao.sgs.message.BroadcastSender;
import org.kelvmiao.sgs.model.Hall;
import org.kelvmiao.sgs.model.Room;
import org.kelvmiao.sgs.util.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by kelvin on 3/12/14.
 */
public class HallController extends BroadcastSender {
    private Hall hall;
    private ReadWriteLock roomListLock = new ReentrantReadWriteLock();
    private ReadWriteLock playerListLock = new ReentrantReadWriteLock();
    private final Map<String, Room> roomMap = new HashMap<>();
    private final Map<Room, RoomController> roomControllerMap = new HashMap<>();
    private UUID roomListVersion;
    private UUID playerListVersion;

    public HallController(Hall hall) {
        this.hall = hall;
        roomListVersion = UUID.randomUUID();
        playerListVersion = UUID.randomUUID();
    }

    public UUID getRoomListVersion() {
        Lock lock = roomListLock.readLock();
        lock.lock();
        try {
            return roomListVersion;
        }finally {
            lock.unlock();
        }
    }

    public UUID getPlayerListVersion() {
        Lock lock = playerListLock.readLock();
        lock.lock();
        try {
            return playerListVersion;
        }finally {
            lock.unlock();
        }
    }

    public JSONArray getRoomListJSON() {
        Lock lock = roomListLock.readLock();
        lock.lock();
        try {
            JSONArray array = new JSONArray();
            hall.getRoomList().stream().forEach(room -> array.put(room.toSimpleJSON()));
            return array;
        }finally {
            lock.unlock();
        }
    }

    public JSONArray getPlayerListJSON() {
        Lock lock = playerListLock.readLock();
        lock.lock();
        try {
            JSONArray array = new JSONArray();
            hall.getPlayerList().stream().forEach(player -> array.put(player.toSimpleJSON()));
            return array;
        }finally {
            lock.unlock();
        }
    }

    public RoomController findRoom(String id) {
        Lock lock = roomListLock.readLock();
        lock.lock();
        try {
            return roomControllerMap.get(roomMap.get(id));
        }finally {
            lock.unlock();
        }
    }

    public RoomController createRoom(String game, String name, int max_player, int min_player) {
        Lock lock = roomListLock.writeLock();
        lock.lock();
        try {
            if (hall.getRoomList().size() >= Integer.parseInt(Config.get("max_rooms")))
                return null;
            Room room = new Room(game, name, max_player, min_player);
            RoomController controller = new RoomController(room);
            roomMap.put(room.getId(), room);
            roomControllerMap.put(room, controller);
            hall.getRoomList().add(room);
            JSONObject obj = new JSONObject();
            obj.put("add-room", room.toJSON());
            obj.put("previous", roomListVersion);
            obj.put("current", roomListVersion = UUID.randomUUID());
            broadcast(obj);
            return controller;
        }finally {
            lock.unlock();
        }
    }

    public void closeRoom(RoomController room) {
        Lock lock = roomListLock.writeLock();
        lock.lock();
        try {
            roomMap.remove(room.getId());
            room.removeFromList(hall.getRoomList());
            JSONObject obj = new JSONObject();
            obj.put("remove-room", room.getId());
            obj.put("previous", roomListVersion);
            obj.put("current", roomListVersion = UUID.randomUUID());
            broadcast(obj);
        }finally {
            lock.unlock();
        }
    }

    public void registerPlayer(PlayerController player) {
        Lock lock = playerListLock.writeLock();
        lock.lock();
        try {
            player.addToList(hall.getPlayerList());
            JSONObject obj = new JSONObject();
            obj.put("add-player", player.toSimpleJSON());
            obj.put("previous", playerListVersion);
            obj.put("current", playerListVersion = UUID.randomUUID());
            broadcast(obj);
        }finally {
            lock.unlock();
        }
    }

    public void removePlayer(PlayerController player) {
        Lock lock = playerListLock.writeLock();
        lock.lock();
        try {
            player.removeFromList(hall.getPlayerList());
            JSONObject obj = new JSONObject();
            obj.put("remove-player", player.getId());
            obj.put("previous", playerListVersion);
            obj.put("current", playerListVersion = UUID.randomUUID());
            broadcast(obj);
        }finally {
            lock.unlock();
        }
    }
}
