package be.urpi.software.modular.core.application.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Reloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(Reloader.class);

    public static void reload(final String destinationFOlder) throws IOException {
        final File file = new File(destinationFOlder);

        for (File destinationFile : file.listFiles()) {
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method m;
            try {
                m = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});

                m.setAccessible(true);
                m.invoke(urlClassLoader, destinationFile.toURI().toURL());
                String cp = System.getProperty("java.class.path");
                if (cp != null) {
                    cp += File.pathSeparatorChar + destinationFile.getCanonicalPath();
                } else {
                    cp = destinationFile.toURI().getPath();
                }
                System.setProperty("java.class.path", cp);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
