package com.github.proteus1989.airexplorerdecryptersimpleclient.userinterfaces;

import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.AirExplorerDecrypterSimpleClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class UserInterface {

    protected final AirExplorerDecrypterSimpleClient airExplorerDecrypterSimpleClient;

    public abstract void log(String text);

    public abstract void errorLog(String text);

    public abstract void init();
}
