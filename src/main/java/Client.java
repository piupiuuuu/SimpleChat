import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;



public class Client extends JFrame {

    public static void main(String[] args) {
        //открыть новый поток, запуская клиента
        SwingUtilities.invokeLater(Client::new);
    }

    private JTextField inputField; // текстовое поле для ввода сообщений
    private JTextArea chatArea; // текстовое поле для вывода сообщений

    private Socket socket; //соединение с сервером
    private DataInputStream inputStream; //поток ввода
    private DataOutputStream outputStream; //поток вывода

    public Client() {
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initGUI();
    }

    //открытие соединения
    public void openConnection() throws IOException {
        socket = new Socket(Constants.HOST, Constants.PORT); //открытие сокета
        inputStream = new DataInputStream(socket.getInputStream()); //доступ к исходящему потоку сервера
        outputStream = new DataOutputStream(socket.getOutputStream()); //доступ к входящему потоку сервера

        //входящие сообщения
        Thread thread = new Thread(() -> {
            try {
                // успешная авторизация
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    chatArea.append(strFromServer + "\n");
                    if (strFromServer.startsWith(Constants.AUTH_OK)) {
                        break;
                    }
                }

                // чтение сообщений
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    if (strFromServer.startsWith(Constants.STOP_WORD)) {
                        break;
                    } else {
                        chatArea.append(strFromServer);
                    }
                    chatArea.append("\n");
                }
            } catch (Exception e) {
                System.out.println("Fail!");
                Runtime.getRuntime().exit(0);
            }

        });
        thread.start();
    }

    //закрытие соединения
    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    }

    // прочитать сообщение из текстового окна и отправить на сервер
    public void sendMessage() {
        if(!inputField.getText().trim().isEmpty()) {
            try {
                outputStream.writeUTF(inputField.getText()); //отправить текст из TF на сервер
                inputField.setText(""); //очистить TF
                inputField.grabFocus(); //установить курсор на TF
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения"); //всплывающее окно с текстом ошибки
            }
        }
    }

    //окно c чатом
    public void initGUI() {
        //параметры окна
        setBounds(600, 300, 500, 500);
        setTitle("Клиент");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //текстовое поле для вывода сообщений
        chatArea = new JTextArea();
        chatArea.setEditable(false); //запрет на редактирование текста
        chatArea.setLineWrap(true); //перенос строк
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        //нижняя панель: поле для ввода ссобщений + кнопка для отправки сообщений
        JPanel panel = new JPanel(new BorderLayout());
        //кнопка для отправки сообщений
        JButton button = new JButton("Отправить");
        panel.add(button, BorderLayout.EAST);
        button.addActionListener(e -> sendMessage()); //нажатие на кнопку - отправка сообщения из TF в TA
        //текстовое поле для ввода сообщений
        inputField = new JTextField();
        panel.add(inputField, BorderLayout.CENTER);
        inputField.addActionListener(e -> sendMessage()); //нажатие на enter в TF - отправка сообщения из TF в TA
        add(panel, BorderLayout.SOUTH);

        //действие на закрытие окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    outputStream.writeUTF(Constants.STOP_WORD);
                    closeConnection();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

}