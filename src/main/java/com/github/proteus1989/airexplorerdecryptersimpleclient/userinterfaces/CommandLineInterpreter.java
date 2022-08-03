package com.github.proteus1989.airexplorerdecryptersimpleclient.userinterfaces;

import com.github.proteus1989.airexplorerdecryptersimpleclient.utils.FileChecker;
import lombok.Getter;
import lombok.extern.java.Log;
import picocli.CommandLine;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Log
@Getter
@CommandLine.Command(name = "java AirExplorerDecrypterSimpleClient.jar -jar", footer = "Copyright(c) 2022",
        description = "Decrypts <file> to destination directory (encrypted file directory is chosen by default).")
public class CommandLineInterpreter implements Runnable {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;
    @CommandLine.Option(names = {"-p", "--password"}, required = true, description = "the password")
    private String password;
    private File output;
    private List<File> files;
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean help = false;
    @CommandLine.Option(names = {"-g", "--gui"}, description = "display the graphical window")
    private boolean gui = false;
    @Getter
    private FileChecker fileChecker;

    @Override
    public void run() {

    }

    @CommandLine.Option(names = {"-o", "--output"}, description = "the destination folder")
    private void getOutputDirectory(String output) {
        File file = new File(output);
        if (!file.isDirectory()) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for option '--output': destination is not a folder.", output));
        }
        if (!file.canWrite()) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    String.format("Invalid value '%s' for option '--output': Application hasn't got privileges to write here.", output));
        }
        this.output = file;
    }

    @CommandLine.Parameters(arity = "1..*", description = "one or more files to decrypt")
    private void getFile(String... files) {
        fileChecker = new FileChecker(Arrays.stream(files).map(File::new));
        this.files = fileChecker.getValidFiles();
    }


}
