package org.switf.pixza;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.switf.pixza.telegram.AdminBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class PixzaApplication implements CommandLineRunner {

    @Autowired
    private AdminBot adminBot;

    public static void main(String[] args) {
        SpringApplication.run(PixzaApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(adminBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
