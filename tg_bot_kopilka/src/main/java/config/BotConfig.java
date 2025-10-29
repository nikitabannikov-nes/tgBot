package config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class BotConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream inputStream = BotConfig.class.getClassLoader()
                .getResourceAsStream("application.properties");
             Reader rdr = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            properties.load(rdr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getBotUserName() {
        return properties.getProperty("bot.username");
    }

    public static String getBotToken() {
        return properties.getProperty("bot.token");
    }

    public static String getRootFolder() {
        return properties.getProperty("bot.root.topics");
    }

    public static String getAnswerFolder() {
        return properties.getProperty("bot.root.answers");
    }

}
