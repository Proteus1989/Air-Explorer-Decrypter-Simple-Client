package com.github.proteus1989.airexplorerdecryptersimpleclient.userinterfaces;

import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.AirExplorerDecrypterSimpleClient;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.TaskData;
import com.github.proteus1989.airexplorerdecryptersimpleclient.utils.Pair;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

public class Launcher {

    public static void start(String[] args) {
        CommandLineInterpreter initialConfig = new CommandLineInterpreter();

        if (args.length > 0) {
            int exitCode = new picocli.CommandLine(initialConfig).setTrimQuotes(true).execute(args);
            if (exitCode != 0) {
                System.exit(exitCode);
            }
            if (initialConfig.isHelp()) {
                System.exit(0);
            }
        }

        AirExplorerDecrypterSimpleClient airExplorerDecrypterSimpleClient = new AirExplorerDecrypterSimpleClient();
        UserInterface userInterface;

        if (initialConfig.isGui() || args.length == 0) {
            userInterface = new GUI(airExplorerDecrypterSimpleClient);
        } else {
            userInterface = new Console(airExplorerDecrypterSimpleClient);
        }

        userInterface.init();

        if (args.length > 0) {
            initialConfig.getFileChecker().logNotValidFiles(userInterface::errorLog);
            if (initialConfig.getFiles() == null || initialConfig.getFiles().isEmpty()) {
                userInterface.errorLog("There aren't files to process\n\nExiting in 5 seconds...");
                sleepAndExit(1);
            }

            Pair<List<Future<TaskData>>, List<File>> tuple = airExplorerDecrypterSimpleClient
                    .enqueueTasks(initialConfig.getFiles(), initialConfig.getOutput(), initialConfig.getPassword());

            List<Future<TaskData>> futures = tuple.getLeft();
            List<File> wrongPass = tuple.getRight();

            wrongPass.stream().map(file -> "Mismatching password for " + file.getName() + ". Skipping it...")
                    .forEach(userInterface::errorLog);

            if (!futures.isEmpty()) {
                for (Future<TaskData> future : futures) {
                    try {
                        future.get();
                    } catch (Exception ex) {
                        userInterface.errorLog(ex.getMessage());
                    }
                }
                userInterface.log("All files have been decrypted\n\nExiting in 5 seconds...");
                sleepAndExit(0);
            }

            if(!futures.isEmpty() || !wrongPass.isEmpty()) {
                userInterface.errorLog("There aren't files to process\n\nExiting in 5 seconds...");
                sleepAndExit(0);
            }
        }
    }

    private static void sleepAndExit(int status) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // do nothing
        }
        System.exit(status);
    }
}
