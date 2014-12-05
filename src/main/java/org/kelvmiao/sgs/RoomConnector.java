package org.kelvmiao.sgs;

import org.json.JSONObject;
import org.kelvmiao.sgs.controller.HallController;
import org.kelvmiao.sgs.controller.PlayerController;
import org.kelvmiao.sgs.controller.RoomController;
import org.kelvmiao.sgs.message.BroadcastReceiver;
import org.kelvmiao.sgs.model.Player;
import org.kelvmiao.sgs.util.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Created by kelvin on 2/12/14.
 */
public class RoomConnector implements BroadcastReceiver, Runnable{
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private PlayerController player;
    private RoomServer server;

    public RoomConnector(Socket socket) throws IOException {
        this.server = RoomServer.getInstance();
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName(Config.get("charset"))));
    }

    @Override
    public void run() {
        try {
            final HallController hall = RoomServer.getInstance().getHall();
            String address = socket.getInetAddress().toString();
            int port = socket.getPort();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.printf("Command from %s:%d : %s\n", address, port, line);
                try {
                    JSONObject json = new JSONObject(line);
                    String cmd = json.getString("cmd");
                    if(player == null && !cmd.equals("hello")) {
                        sendError("You've not registered");
                        continue;
                    }
                    switch (cmd) {
                        case "hello":
                            if(player != null) {
                                sendError("You've already registered");
                                break;
                            }
                            player = new PlayerController(new Player(json.getString("name"), address, port));
                            hall.registerPlayer(player);
                            hall.registerBroadcastReceiver(this);
                            sendWelcome();
                            sendRoomList();
                            sendPlayerList();
                            break;
                        case "get-room-list":
                            sendRoomList();
                            break;
                        case "get-player-list":
                            sendPlayerList();
                            break;
                        case "create-room":
                            if (!checkPlayerStatus("hall"))
                                break;
                            RoomController room = hall.createRoom(json.getString("game"), json.getString("name"),
                                    json.getInt("max_player"), json.getInt("min_player"));
                            if(room != null) {
                                room.addPlayer(player);
                                room.registerBroadcastReceiver(this);
                            }else
                                sendError("Failed to create new room");
                            break;
                        case "join-room":
                            if (!checkPlayerStatus("hall"))
                                break;
                            String id = json.getString("id");
                            RoomController selected = hall.findRoom(id);
                            if (selected != null) {
                                if (!selected.containsPlayer(player))
                                    selected.addPlayer(player);
                                selected.registerBroadcastReceiver(this);
                            } else {
                                sendError(String.format("No room with ID:%s is found", id));
                            }
                            break;
                        case "get-room-info":
                            sendRoomInfo(hall.findRoom(json.getString("id")));
                            break;
                        case "set-ready":
                            if(!player.isReady())
                                player.setReady(true);
                            break;
                        case "unset-ready":
                            if(player.isReady())
                                player.setReady(false);
                            break;
                        case "start-game":
                            if (!checkPlayerStatus("room"))
                                break;
                            player.getRoom().startGame(player);
                            break;
                        case "terminate-game":
                            if (!checkPlayerStatus("room"))
                                break;
                            player.getRoom().terminateGame(player);
                            break;
                        case "exit-room":
                            if (!checkPlayerStatus("room"))
                                break;
                            id = json.getString("id");
                            RoomController room_to_exit = hall.findRoom(id);
                            room_to_exit.removeBroadcastReceiver(this);
                            room_to_exit.removePlayer(player);
                            player.setStatus("hall");
                            player.setRoom(null);
                            if(room_to_exit.getPlayerListSize() <= 0) {
                                hall.closeRoom(room_to_exit);
                            }
                            break;
                        case "bye":
                            hall.removeBroadcastReceiver(this);
                            hall.removePlayer(player);
                            player = null;
                            break;
                        default:
                            sendError("Unknown Command: " + cmd);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendError(e);
                }
            }
            System.out.printf("Connection closed from %s:%d\n", address, port);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBroadcast(JSONObject json){
        send(json);
    }

    public synchronized void send(JSONObject json){
        writer.println(json.toString());
    }

    private void sendError(String message) {
        JSONObject json = new JSONObject();
        json.put("error", message);
        send(json);
    }

    private void sendError(Throwable exception) {
        JSONObject json = new JSONObject();
        json.put("error", exception.getLocalizedMessage());
        send(json);
    }

    private void sendWelcome() {
        JSONObject json = new JSONObject();
        json.put("welcome", String.format("Welcome %s", player.getFullName()));
        send(json);
    }

    private void sendRoomList() {
        JSONObject json = new JSONObject();
        json.put("room-list", server.getHall().getRoomListJSON());
        json.put("current", server.getHall().getRoomListVersion());
        send(json);
    }

    private void sendPlayerList() {
        JSONObject json = new JSONObject();
        json.put("player-list", server.getHall().getPlayerListJSON());
        json.put("current", server.getHall().getPlayerListVersion());
        send(json);
    }

    private void sendRoomInfo(RoomController room) {
        JSONObject json = new JSONObject();
        json.put("room-info", room.toJSON());
        send(json);
    }

    private boolean checkPlayerStatus(String validState) {
        String status = player.getStatus();
        if(!status.equals(validState)){
            sendError(String.format("Invalid state. Current state: %s, but expected to be %s", status, validState));
            return false;
        }
        return true;
    }
}
