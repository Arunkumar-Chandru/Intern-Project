package chat.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

import chat.common.CryptoUtil;
import chat.common.Message;

public class ChatClient extends Thread {
    private final String host;
    private final int port;
    private final String nickname;
    private final String key;
    private final Consumer<Message> onMessage;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ChatClient(String host, int port, String nickname, String key, Consumer<Message> onMessage) {
        this.host = host;
        this.port = port;
        this.nickname = nickname;
        this.key = key;
        this.onMessage = onMessage;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            sendMessage(nickname + " joined");

            while (true) {
                Message msg = (Message) in.readObject();
                String decrypted = CryptoUtil.decrypt(msg.getContent(), key);
                msg.setContent(decrypted);
                onMessage.accept(msg);
            }
        } catch (Exception e) {
            onMessage.accept(new Message("System", "Disconnected from server"));
        }
    }

    public void sendMessage(String text) {
        try {
            String encrypted = CryptoUtil.encrypt(text, key);
            Message msg = new Message(nickname, encrypted);
            out.writeObject(msg);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}