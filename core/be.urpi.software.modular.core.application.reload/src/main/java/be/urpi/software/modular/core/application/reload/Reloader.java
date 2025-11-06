package be.urpi.software.modular.core.application.reload;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class Reloader {
    public static void reload(final String destinationFolder) throws IOException {
        File file = new File(destinationFolder);
        Arrays.stream(Objects.requireNonNullElse(file.listFiles(), new File[0]))
                .forEach(ClassPathUtil::setOnClassPath);
    }
}
