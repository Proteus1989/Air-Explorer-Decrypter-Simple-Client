package com.suapp.airexplorerfiledecrypter.userinterfaces.command;

import com.suapp.airexplorerfiledecrypter.AirExplorerFileDecrypterMain;
import com.suapp.airexplorerfiledecrypter.processor.AirExplorerDecrypter;
import com.suapp.airexplorerfiledecrypter.processor.Task;
import com.suapp.airexplorerfiledecrypter.streamwrapper.AirExplorerInputStreamWrapper;
import com.suapp.airexplorerfiledecrypter.utils.Document;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.LinkedList;

public class CommandInterpreterImpl
{

    @CommandLine.Option(names = { "-p", "password"}, paramLabel = "PASSWORD", description = "create a new archive")
    String password;

    @CommandLine.Option(names = { "-o", "--output" }, paramLabel = "OUTPUT", description = "the destination folder")
    File archive;

    @CommandLine.Parameters(paramLabel = "FILE", description = "one or more files to decrypt")
    File[] files;

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;
}
