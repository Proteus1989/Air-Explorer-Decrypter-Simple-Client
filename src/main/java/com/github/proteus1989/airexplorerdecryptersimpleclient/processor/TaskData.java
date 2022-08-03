package com.github.proteus1989.airexplorerdecryptersimpleclient.processor;

import com.github.proteus1989.airexplorerdecrypter.AirExplorerInputStream;

import java.io.File;

public record TaskData(AirExplorerInputStream stream, File file, File output, String filename) {
}
