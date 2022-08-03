package com.github.proteus1989.airexplorerdecryptersimpleclient.userinterfaces;

import com.github.proteus1989.airexplorerdecryptersimpleclient.AirExplorerFileDecrypterMain;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.AirExplorerDecrypterSimpleClient;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners.TaskProcessorOnChangeListener;
import com.github.tomaslanger.chalk.Chalk;
import lombok.extern.java.Log;

import static java.util.logging.Level.SEVERE;

@Log
public class Console extends UserInterface {

    public Console(AirExplorerDecrypterSimpleClient airExplorerDecrypterSimpleClient) {
        super(airExplorerDecrypterSimpleClient);
    }

    @Override
    public void log(String text) {
        log.info(text);
    }

    @Override
    public void errorLog(String text) {
        log.log(SEVERE, text);
    }

    public void init() {
        log.info("Starting " + Chalk.on(AirExplorerFileDecrypterMain.name).green().bold());
        airExplorerDecrypterSimpleClient.addOnStartListener((srcFilepath, srcName, destFilepath, destName) -> log("Decrypting " + destName + " in " + destFilepath));
        airExplorerDecrypterSimpleClient.addOnChangeListener(new TaskProcessorOnChangeListener() {
            long last = 0;
            @Override
            public void onChange(double percent, long size, long processed, String srcFolderPath, String srcName, String destFolderPath, String destName) {
                if(last + 50 < System.currentTimeMillis() || processed == size) {
                    last = System.currentTimeMillis();
                    String out = new String(new char[115]).replace("\0", "\b") +
                            "[" +
                            Chalk.on(new String(new char[(int)percent]).replace("\0", "|")).green().bold() +
                            new String(new char[100]).replace("\0", " ").substring(Math.min(99,(int)percent)) +
                            "]" +
                            " %.2f%%".formatted(percent);
                    System.out.printf("%-115s", out);
                    if(processed == size){
                        System.out.println();
                    }
                }


            }
        });
        airExplorerDecrypterSimpleClient.addOnFinishListener((srcFilepath, srcName, destFilepath, destName) -> log("Decrypted " + destName + " in " + destFilepath));
    }
}
