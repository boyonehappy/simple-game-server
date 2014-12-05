package org.kelvmiao.sgs;

import org.kelvmiao.sgs.controller.HallController;
import org.kelvmiao.sgs.model.Hall;
import org.kelvmiao.sgs.util.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kelvin on 2/12/14.
 */
public class RoomServer {
    private static RoomServer instance;
    private final HallController hall;

    private RoomServer(){
        hall = new HallController(new Hall());
    }
    public static RoomServer getInstance(){
        if(instance == null)
            instance = new RoomServer();
        return instance;
    }

    public HallController getHall() {
        return hall;
    }

    public void start() throws IOException {
        int port = Integer.parseInt(Config.get("port"));
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.printf("Starting room server at port %d...\n", serverSocket.getLocalPort());
        Socket clientSocket;
        while (true) {
            clientSocket = serverSocket.accept();
            System.out.printf("Connection established with %s:%d\n", clientSocket.getInetAddress().toString(),
                    clientSocket.getPort());
            new Thread(new RoomConnector(clientSocket)).start();
        }
    }

    public static void main(String[] args) {
        try {
            RoomServer.getInstance().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
