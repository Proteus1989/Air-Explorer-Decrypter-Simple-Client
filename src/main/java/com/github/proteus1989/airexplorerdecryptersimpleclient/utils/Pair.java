package com.github.proteus1989.airexplorerdecryptersimpleclient.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Pair<K, V> {
    private K left;
    private V right;
}
