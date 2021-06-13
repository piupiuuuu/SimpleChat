import java.sql.*;

public class DataBase implements AuthService {
    private Connection connection; // интерфейс для соединение с базой данных
    private Statement statement; // интерфейс для отправки запросов в БД

    @Override
    public void start() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost/chat?serverTimezone=Europe/Moscow&useSSL=false", "root", "root");
        statement = connection.createStatement();
        createTable();
        insertTable();
    }

    // создать таблицу, если ее нет
    public void createTable() {
        String sqlCommand = "CREATE TABLE IF NOT EXISTS chat.users (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                " nick VARCHAR(45) NOT NULL, " +
                " login VARCHAR(45) NOT NULL, " +
                " pass VARCHAR(45) NOT NULL)";

        try {
            statement.executeUpdate(sqlCommand);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    // проверить, что таблица пустая
    public boolean tableIsEmpty() {
        boolean result = false;
        String sqlCommand = "SELECT * FROM chat.users;";
        try (ResultSet resultSet = statement.executeQuery(sqlCommand)) {
            result = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return !result;
    }

    // заполнить таблицу
    public void insertTable() {
        if(tableIsEmpty()) {
            String sqlCommand =
                    "INSERT INTO chat.users (nick, login, pass) VALUES ('nick1', 'login1', 'pass1'), " +
                            "('nick2', 'login2', 'pass2'), " +
                            "('nick3', 'login3', 'pass3');";
            try {
                statement.executeUpdate(sqlCommand);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }


    @Override
    public void stop() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // получить ник пользователя
    @Override
    public String getNickByLoginPass(String login, String pass) {
        String nick = null;
        String sqlCommand = "SELECT nick FROM chat.users WHERE login = '" + login + "' AND pass ='" + pass + "';";
        try {
            ResultSet resultSet = statement.executeQuery(sqlCommand);
            while(resultSet.next()){
                nick = resultSet.getString(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return nick;
    }

    // проверить, что ник свободен
    @Override
    public boolean isNickFree(String nick) {
        boolean result = false;
        String sqlCommand = "SELECT nick FROM chat.users WHERE nick = '" + nick + "';";
        try (ResultSet resultSet = statement.executeQuery(sqlCommand)) {
            result = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return !result;
    }

    // получить id пользователя
    public int getIdUsers(String nick) {
        int id = 0;
        String sqlCommand = "SELECT id FROM chat.users where nick = '" + nick + "';";
        try {
            ResultSet resultSet = statement.executeQuery(sqlCommand);
            while(resultSet.next()){
                id = resultSet.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return id;
    }

    // обновить ник в таблице
    @Override
    public void updateNick(String newNick, String nick) {
        int id = getIdUsers(nick);
        String sqlCommand = "UPDATE chat.users SET nick = '" + newNick + "' WHERE (id = '" + id + "')";
        try {
            statement.executeUpdate(sqlCommand);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}