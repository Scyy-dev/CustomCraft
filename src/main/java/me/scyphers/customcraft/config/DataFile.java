package me.scyphers.customcraft.config;

import java.io.File;

public interface DataFile {

    File getFile();

    void load() throws Exception;

    void save() throws Exception;

}
