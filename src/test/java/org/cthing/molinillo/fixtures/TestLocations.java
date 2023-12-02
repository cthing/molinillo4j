package org.cthing.molinillo.fixtures;

import java.io.File;


public final class TestLocations {

    public static final File CASE_DIR = new File(System.getProperty("testResourcesDir"), "case");
    public static final File INDEX_DIR = new File(System.getProperty("testResourcesDir"), "index");

    private TestLocations() {
    }
}
