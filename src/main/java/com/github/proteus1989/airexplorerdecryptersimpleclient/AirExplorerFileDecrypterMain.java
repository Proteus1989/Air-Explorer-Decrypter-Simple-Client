package com.github.proteus1989.airexplorerdecryptersimpleclient;


import com.github.proteus1989.airexplorerdecryptersimpleclient.userinterfaces.Launcher;

public class AirExplorerFileDecrypterMain {
    public static final String version = "2.0";
    public static final String name = "Air Explorer Decrypter Simple Client";

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
    }

    public static void main(String[] args) {
        Launcher.start(args);
    }

}
