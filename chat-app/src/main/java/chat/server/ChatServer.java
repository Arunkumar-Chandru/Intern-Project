package chat.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import chat.common.CryptoUtil;
import chat.common.Message;

public class ChatServer {
    private final int port;
    private final String key;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public ChatServer(int port, String key) {
        this.port = port;
        this.key = key;
    }

    public void start() throws Exception {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port + " (key='" + key + "')");
            while (true) {
                Socket s = ss.accept();
                new Thread(new ClientHandler(s)).start();
            }
        }
    }

    private void broadcast(Message msg) {
        clients.values().forEach(ch -> ch.send(msg));
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String nick;

        public ClientHandler(Socket s) {
            this.socket = s;
        }

        @Override
        public void run() {
            try (socket) {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                Message joinMsg = (Message) in.readObject();
                nick = joinMsg.getSender();
                clients.put(nick, this);

                System.out.println(nick + " connected from " + socket.getRemoteSocketAddress());
                broadcast(new Message("System", nick + " joined the chat"));

                Message msg;
                while ((msg = (Message) in.readObject()) != null) {
                    String decrypted = CryptoUtil.decrypt(msg.getContent(), key);
                    msg.setContent(decrypted);
                    System.out.println("📩 " + msg);
                    broadcast(msg);
                }
            } catch (Exception e) {
                System.out.println("Client error (" + nick + "): " + e.getMessage());
            } finally {
                if (nick != null) {
                    clients.remove(nick);
                    broadcast(new Message("System", nick + " left the chat"));
                }
            }
        }

        public void send(Message msg) {
            try {
                Message encrypted = new Message(msg.getSender(),
                    CryptoUtil.encrypt(msg.getContent(), key));
                out.writeObject(encrypted);
                out.flush();
            } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 5555;
        String key = "XO";
        new ChatServer(port, key).start();
    }
}
