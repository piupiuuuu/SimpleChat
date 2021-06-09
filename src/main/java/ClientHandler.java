import java.io.*;
import java.net.Socket;

/**
 * Обмен сообщениями между клиентами и сервером
 */
public class ClientHandler {

    private MyServer server;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String name; // ник пользователя

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.name = "";

            Thread thread = new Thread(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
            thread.start();

        } catch (IOException e) {
            System.out.println("Проблема при создании клиента");
        }
    }

    /**
     * Авторизация - сообщение в чат ввиде: /auth login pass
     */
    private void authentication() throws IOException {
        while (true) {
            String message = inputStream.readUTF();
            if (message.startsWith(Constants.AUTH_COMMAND)) {
                String[] parts = message.split("\\s+"); // разбить сообщение на массив строк
                String nick = server.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                if (nick != null) {
                    // проверка что такого клиента нет
                    if (!server.isNickBusy(nick)) {
                        name = nick;
                        server.subscribe(this); // добавить пользователя в сервис обмена сообщениями
                        sendMessages(Constants.AUTH_OK);
                        server.broadcastMessage(name + " вошел в чат"); // сообщение всем авторизованным пользователям: ник вошел в чат
                        return;
                    } else {
                        sendMessages("Ник уже используется");
                    }
                }
            } else sendMessages("Неверный логин или пароль");
        }
    }

    /**
     * Чтение сообщения сервером от клиента
     */
    private void readMessages() throws IOException {
        while (true) {
            String messageFromClient = inputStream.readUTF(); // читаем сообщение от клиента
            System.out.println("от " + name + ": " + messageFromClient); // вывод сообщение от клиента в консоль сервера

            // если сообщение /end: выходим из цикла
            if (messageFromClient.equals(Constants.STOP_WORD)) {
                return;
            } else {
                String messageFull = "[" + name + "]: " + messageFromClient;
                server.broadcastMessage(messageFull);
            }
        }
    }


    public void sendMessages(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        server.unsubscribe(this); // закрываем соединение с сервером обмена сообщениями
        server.broadcastMessage(name + " вышел из чата"); // сообщение всем авторизованным пользователям: ник вышел из чата
        //закрываем все открытые потоки
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
