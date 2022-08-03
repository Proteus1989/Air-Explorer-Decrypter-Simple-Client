package com.github.proteus1989.airexplorerdecryptersimpleclient.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileChecker {

    private final Map<FileStates, List<File>> filesMap;

    public FileChecker(Stream<File> filesStream) {
        filesMap = filesStream.collect(Collectors.groupingBy(this::groupingFiles, Collectors.toUnmodifiableList()));
    }

    public void logNotValidFiles(Consumer<String> consumer) {
        filesMap.entrySet().stream()
                .filter(entry -> entry.getKey() != FileStates.FINE)
                .forEach(entry -> entry.getValue()
                        .forEach(file -> consumer.accept("Skipping %s file. Reason: %s".formatted(
                                file.getName(), entry.getKey().getDescription()))));
    }

    private FileStates groupingFiles(File file) {
        if (!file.exists()) {
            return FileStates.NOT_EXISTS;
        }
        if (!(file.getName().toLowerCase().endsWith(".cloudencoded2") || file.getName().toLowerCase().endsWith(".cloudencoded"))) {
            return FileStates.UNKNOWN_EXTENSION;
        }
        if (!file.isFile()) {
            return FileStates.NOT_A_FILE;
        }
        if (!file.canRead()) {
            return FileStates.NOT_READABLE;
        }
        return FileStates.FINE;
    }

    public List<File> getValidFiles() {
        return filesMap.getOrDefault(FileStates.FINE, Collections.emptyList());
    }

    @Getter
    @AllArgsConstructor
    private enum FileStates {
        FINE(""),
        NOT_EXISTS("File not found"),
        UNKNOWN_EXTENSION("File extension is not valid"),
        NOT_A_FILE("Selected path is not a file"),
        NOT_READABLE("Application hasn't got privileges to read the file");

        private final String description;
    }

}
