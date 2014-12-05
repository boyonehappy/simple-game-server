package org.kelvmiao.sgs.test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kelvmiao.sgs.util.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;

public class SimpleClientController implements Initializable{

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField txt_addres;

    @FXML
    private TextField txt_port;

    private int id = 0;

    @FXML
    void onConnect(ActionEvent event) {
        id++;
        Tab tab = new Tab("Client" + id);
        final TextArea ta = new TextArea();
        ta.setEditable(false);

        final TextField input = new TextField();
        Button btnSubmit = new Button("Send");
        HBox box = new HBox(10, input, btnSubmit);
        HBox.setHgrow(input, Priority.ALWAYS);
        VBox pane = new VBox(10, ta,box);
        pane.setPadding(new Insets(10));
        pane.setFillWidth(true);
        VBox.setVgrow(ta, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.NEVER);
        tab.setContent(pane);
        tab.setClosable(true);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        input.requestFocus();

        try {
            Socket socket = new Socket(txt_addres.getText(),Integer.parseInt(txt_port.getText()));
            SimpleClient.socketList.add(socket);
            final PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName(Config.get("charset"))));
            EventHandler<ActionEvent> sendMsg = (e) -> {
                String data = input.getText();
                writer.println(data);
                ta.appendText("> ");
                ta.appendText(data);
                ta.appendText("\n");
                input.setText("");
            };
            btnSubmit.setOnAction(sendMsg);
            input.setOnAction(sendMsg);
            tab.setOnClosed((ev)->{
                try {
                    SimpleClient.socketList.remove(socket);
                    if(!socket.isClosed())
                        socket.shutdownInput();
                }catch (IOException e){
                    e.printStackTrace();
                }
            });
            new Thread(){
                @Override
                public void run() {
                    try {
                        String line;
                        while((line = reader.readLine()) != null){
                            final String fl = line;
                            Platform.runLater(()->{
                                ta.appendText("< ");
                                ta.appendText(fl);
                                ta.appendText("\n");
                            });
                        }
                        Platform.runLater(() -> {
                            ta.appendText("[Connection closed]\n");
                            input.setEditable(false);
                            btnSubmit.setDisable(true);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
    }
}
