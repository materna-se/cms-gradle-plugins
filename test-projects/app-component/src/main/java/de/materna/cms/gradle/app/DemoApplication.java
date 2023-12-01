package de.materna.cms.gradle.app;

import io.github.classgraph.ScanResult;

import java.util.LinkedList;
import java.util.List;

public class DemoApplication {

    public static void main(String[] args) {
        System.out.println("Hallo Welt");
        System.out.println(ScanResult.class);

        List list = new LinkedList();
    }
}
