import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Простейшая реализация сервиса авторизации, которая работает на встроенном списке пользователей
 */
public class BaseAuthService implements AuthService {

    private static class Entry {
        private final String nick;
        private final String login;
        private final String pass;

        public Entry(String nick, String login, String pass) {
            this.nick = nick;
            this.login = login;
            this.pass = pass;
        }
    }

    private final List<Entry> entries;
    private static final Logger LOGGER3 = LogManager.getLogger(MyServer.class);

    public BaseAuthService() {
        entries = Arrays.asList(
                new Entry("nick1", "login1", "pass1"),
                new Entry("nick2", "login2", "pass2"),
                new Entry("nick3", "login3", "pass3")
        );
    }

    @Override
    public void start() {
        LOGGER3.info(Constants.LOGG_SERV + this.getClass().getName() + " запуск");
    }

    @Override
    public void stop() {
        LOGGER3.info(Constants.LOGG_SERV + this.getClass().getName() + " отключение");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for(Entry entry : entries) {
            if(entry.login.equals(login) && entry.pass.equals(pass)) return entry.nick;
        }
        return null;
    }

}