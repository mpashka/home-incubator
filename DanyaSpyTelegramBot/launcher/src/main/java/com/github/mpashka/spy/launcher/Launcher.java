package com.github.mpashka.spy.launcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class Launcher {

    private static final Logger LOGGER = LogManager.getLogger();

    private void runChildProcess(String[] args) {
        try {
            String url = args[0];
            String mainClass = args[1];
            String[] restArgs = new String[args.length - 2];
            System.arraycopy(args, 2, restArgs, 0, restArgs.length);
            LOGGER.debug("Starting launcher {} {} {}", url, mainClass, Arrays.toString(args));
            runChildProcess0(url, mainClass, args);
        } catch (MalformedURLException|ClassNotFoundException|NoSuchMethodException|InvocationTargetException|IllegalAccessException|RuntimeException e) {
            LOGGER.error("Error starting launcher {}", Arrays.toString(args), e);
            showError(e);
        }
    }

    private void showError(Exception e) {
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

    private void runChildProcess0(String urlsString, String mainClassName, String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String pathSeparator = System.getProperty("path.separator");
        String[] urlsStrings = urlsString.split(pathSeparator);
        URL[] urls = new URL[urlsStrings.length];
        for (int i = 0; i < urlsStrings.length; i++) {
            urls[i] = new URL(urlsStrings[i]);
        }
        ClassLoader childClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
        Class<?> mainClass = childClassLoader.loadClass(mainClassName);
        Method mainMethod = mainClass.getMethod("main", new Class[]{String[].class});
        mainMethod.invoke(null, args);
    }


    public static void main(String[] args) throws Exception {
        LOGGER.debug("Starting launcher with args {}", Arrays.toString(args));
        new Launcher().runChildProcess(args);
    }
}
