package com.github.mpashka.spy.runner;

import java.lang.reflect.Method;

public interface CustomLauncher {
    class CustomLauncherClass implements CustomLauncher {
        @Override
        public void run(String[] args) throws Exception {
            String point = args[0];
            String[] restArgs = new String[args.length-1];
            Class<?> mineClass = Class.forName(point);
            Method mineMain = mineClass.getMethod("main", String[].class);
            mineMain.invoke(null, (Object) restArgs);
        }

        @Override
        public void stop() {
            Thread.currentThread().interrupt();
            System.exit(0);
        }
    };


    class CustomLauncherApp implements CustomLauncher {
        private Process process;

        @Override
        public void run(String[] args) throws Exception {
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            process = pb.start();
            process.waitFor();
        }

        @Override
        public void stop() {
            process.destroyForcibly();
        }
    }

    void run(String[] args) throws Exception;
    void stop();

    static CustomLauncher getLauncher(String name) {
        switch (name) {
            case "class":
                return new CustomLauncherClass();
            case "app":
                return new CustomLauncherApp();
            default:
                throw new RuntimeException("Unknown launcher " + name);
        }
    }
}
