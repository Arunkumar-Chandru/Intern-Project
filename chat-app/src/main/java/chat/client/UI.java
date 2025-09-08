package chat.client;

import chat.common.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class UI extends Application {

    private ChatClient client;
    private ObservableList<String> messages;
    private String nickname;

    @Override
    public void start(Stage primaryStage) {
        messages = FXCollections.observableArrayList();

        ListView<String> chatList = new ListView<>(messages);
        chatList.setFocusTraversable(false);

        chatList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #1E1E2E;");
                } else {
                    setText(item);
                    if (item.startsWith("You:")) {
                        setAlignment(Pos.CENTER_RIGHT);
                        setStyle("-fx-background-color: #1E1E2E; -fx-text-fill: #87CEFA; -fx-font-weight: bold;");
                    } else if (item.startsWith("System:")) {
                        setAlignment(Pos.CENTER);
                        setStyle("-fx-background-color: #1E1E2E; -fx-text-fill: #FFD700; -fx-font-style: italic;");
                    } else {
                        setAlignment(Pos.CENTER_LEFT);
                        setStyle("-fx-background-color: #1E1E2E; -fx-text-fill: white;");
                    }
                }
            }
        });

        TextField inputField = new TextField();
        inputField.setPromptText("Type a message...");
        Button sendButton = new Button("Send");

        HBox inputBox = new HBox(10, inputField, sendButton);
        inputBox.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setCenter(chatList);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Chat App");
        primaryStage.setScene(scene);
        primaryStage.show();

        String host = getUserInput("Enter Server Host:", "127.0.0.1");
        int port = Integer.parseInt(getUserInput("Enter Server Port:", "5555"));
        nickname = getUserInput("Enter Nickname:", "User");
        String key = getUserInput("Enter Shared Key:", "XO");

        client = new ChatClient(host, port, nickname, key, this::onMessageReceived);
        client.start();

        sendButton.setOnAction(e -> {
            String msg = inputField.getText().trim();
            if (!msg.isEmpty()) {
                client.sendMessage(msg);
                messages.add("You: " + msg);
                inputField.clear();
            }
        });

        inputField.setOnAction(sendButton.getOnAction());
    }

    private String getUserInput(String prompt, String defaultVal) {
        TextInputDialog dialog = new TextInputDialog(defaultVal);
        dialog.setHeaderText(prompt);
        return dialog.showAndWait().orElse(defaultVal);
    }

    private void onMessageReceived(Message msg) {
        Platform.runLater(() -> {
            if (msg.getSender().equals(nickname)) {
                // Skip duplicate (don’t show "User: hi" when you already added "You: hi")
                return;
            }
            messages.add(msg.getSender() + ": " + msg.getContent());
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
