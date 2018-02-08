package my.test.teleg;

import javax.swing.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlayDateCheck {

    public static void main(String[] args) throws Exception {
        DanyaBot danyaBot = new DanyaBot().init();
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        SimpleDateFormat format = new SimpleDateFormat("EEEEEEEEE", new Locale("ru"));
        String dayOfWeekStr = format.format(new Date()).toLowerCase();
        if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
            danyaBot.sendToChat(String.format(
                    "Даня попытался включить майнкрафт, а ведь сегодня %s и ему нельзя. Что же он скажет на это...",
                    dayOfWeekStr));
            showErrorAndExit(danyaBot, dayOfWeekStr);
        }
        danyaBot.sendToChat(String.format(
                "Даня попытался включить майнкрафт, и сегодня %s и ему можно. Что же, пусть играет...",
                dayOfWeekStr));
        if (args.length < 1) throw new RuntimeException("Main class not defined");
        Class mineClass = Class.forName(args[0]);
        Method mineMain = mineClass.getMethod("main", String[].class);
        String[] restArgs = new String[args.length - 1];
        if (args.length > 1) {
            System.arraycopy(args, 1, restArgs, 0, restArgs.length);
        }
        mineMain.invoke(null, restArgs);
    }

    private static final String[] responseStrings = {
            "И Даня просто закрыл предупреждение крестиком",
            "Дане ВСЕРАВНО ВСЕРАВНО ВСЕРАВНО",
            "У Дани непруха, но он так надеялся на удачу",
            "Даня извинился. Видимо он просто сделал это по ошибке",
    };

    private static void showErrorAndExit(DanyaBot danyaBot, String dayOfWeekStr) {

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
        danyaBot.sendToChat(chatMessage);


        System.exit(0);
    }
}
