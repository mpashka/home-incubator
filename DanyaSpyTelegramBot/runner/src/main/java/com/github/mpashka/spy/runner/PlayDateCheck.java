package com.github.mpashka.spy.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlayDateCheck {

    private static final Logger LOGGER = LogManager.getLogger();


    private static final long SLEEP_CHECK_INTERVAL = 1000 * 60;

    private TelegramBot telegramBot;
    private CustomLauncher launcher;
    private boolean stopForcibly = false;
    private long lastCheckDate;

    private void run(String[] args) {
        try {
            run0(args);
        } catch (Exception e) {
            StringWriter errOut = new StringWriter();
            e.printStackTrace(new PrintWriter(errOut));
            JOptionPane.showOptionDialog(null,
                    String.format("%s\n" +
                                    "%s",
                            e.toString(),
                            errOut.toString()
                    ),
                    "Ой. Фто-то пофло не так...",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    null,
                    null);
        }
    }

    private void run0(String[] args) throws Exception {
        telegramBot = new TelegramBot().init();
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        SimpleDateFormat format = new SimpleDateFormat("EEEEEEEEE", new Locale("ru"));
        String dayOfWeekStr = format.format(new Date()).toLowerCase();
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
            telegramBot.sendToChat(String.format(
                    "Даня попытался включить майнкрафт, а ведь сегодня %s и ему нельзя. Что же он скажет на это...",
                    dayOfWeekStr));
            showErrorAndExit(telegramBot, dayOfWeekStr);
        }
        sendToChat(String.format(
                "Даня попытался включить майнкрафт. Сегодня %s и ему можно. Что же, пусть играет...",
                dayOfWeekStr));

        if (args.length < 1) throw new RuntimeException("Launcher type not defined");
        String type = args[0];
        String[] restArgs = new String[args.length-1];
        System.arraycopy(args, 1, restArgs, 0, restArgs.length);
        launcher = CustomLauncher.getLauncher(type);
        waitForStop();
        launcher.run(restArgs);
        if (!stopForcibly) {
            sendToChat("Похоже Даня наигрался. Выходит из майнкрафта");
        }
        System.exit(0);
    }

    /**
     * Check if date has changed. Exit if true
     */
    private void waitForStop() throws Exception {
        lastCheckDate = System.currentTimeMillis();
        Thread waitSleepThred = new Thread(() -> {
            while (true) {
                long now = System.currentTimeMillis();
                if (now - lastCheckDate > SLEEP_CHECK_INTERVAL * 10) {
                    stopForcibly = true;
                    sendToChat("Даня выключал компьютер закрытием крышки ноутбука. Хреначим его майнкрафт...");
                    launcher.stop();

                    JOptionPane.showConfirmDialog(null, "Даня, похож ты выключал комп закрытием крышки ноутбука.\n" +
                            "На всякий случай мы тебе выключим майнкрафт.");
                    System.exit(0);
                }
                try {
                    Thread.sleep(SLEEP_CHECK_INTERVAL);
                } catch (InterruptedException e) {
                }
            }
        });
        waitSleepThred.setDaemon(true);
        waitSleepThred.start();
    }

    private static final String[] responseStrings = {
            "И Даня просто закрыл предупреждение крестиком",
            "Дане ВСЕРАВНО ВСЕРАВНО ВСЕРАВНО",
            "У Дани непруха, но он так надеялся на удачу",
            "Даня извинился. Видимо он просто сделал это по ошибке",
    };

    private void showErrorAndExit(TelegramBot telegramBot, String dayOfWeekStr) {

        //Custom button text
        Object[] options = {"Мне плевать",
                "<html>Я надеялся, <br>что сегодня повезет",
                "<html>Извини папа, <br>сейчас выключу"};
        int option = JOptionPane.showOptionDialog(null,
                String.format("Даня, ты же знаешь, что сегодня %s " +
                        "и поэтому играть в майнкрафт нельзя. Что ты на это скажешь?"
                        , dayOfWeekStr),
                "Простой вопрос",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        option++;
        String chatMessage = option < 0 || option >= responseStrings.length ? "Непонятно" : responseStrings[option];
        telegramBot.sendToChat(chatMessage);


        System.exit(0);
    }

    private void sendToChat(String msg) {
        try {
            telegramBot.sendToChat(msg);
        } catch (Exception e) {
            LOGGER.error("Error sending msg to chat {}", msg, e);
        }
    }

    public static void main(String[] args) {
        new PlayDateCheck().run(args);
    }
}
