package org.kelvmiao.sgs.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kelvin on 2/12/14.
 */
public class SimpleClient extends Application{
    public static List<Socket> socketList = new ArrayList<>();
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        for(Socket socket : socketList) {
            if (socket != null) {
                if(!socket.isClosed())
                    socket.shutdownInput();
            }
        }
        super.stop();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("SimpleClient");
        Scene scene = new Scene(FXMLLoader.<Parent>load(this.getClass().getResource("simple-client.fxml")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
