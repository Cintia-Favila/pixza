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









//package org.switf.pixza;
//
//import jakarta.ws.rs.core.Application;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.switf.pixza.config.AdminBot;
//import org.telegram.telegrambots.meta.TelegramBotsApi;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//
//@SpringBootApplication
//public class PixzaApplication {
//
//    public static void main(String[] args) throws TelegramApiException {
//        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
//        botsApi.registerBot(new AdminBot());
//    }
//
////    public static void main(String[] args) {
////        SpringApplication.run(Application.class, args);
////    }
//}
