import java.sql.SQLException;

/**
 * Интерфейс авторизации
 */
public interface AuthService {

    // запустить сервис
    void start() throws SQLException;

    // отстановить сервис
    void stop();

    // получить ник
    String getNickByLoginPass(String login, String pass);

    // проверить что ник есть в базе - false
    default boolean isNickFree(String nick) {
        return true;
    }

    // изменить ник в базе
    default void updateNick(String newNick, String nick) {

    }

}
