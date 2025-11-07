package be.urpi.software.modular.core.jar.util;

import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Utility class on jar file
 */
public class JarFileUtil {
    private JarFileUtil() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Retrieving all the class names that are located in the jar file and that will be filtered out by the predicate
     *
     * @param jarFile the jar file to retrieves all class names
     * @return entries all the files that passes the predicate
     */
    public static List<String> getEntries(File jarFile, Predicate<String> filter) {
        List<String> entries = new ArrayList<>();
        try (JarFile jf = new JarFile(jarFile)) {
            entries.addAll(jf.stream()
                    .map(ZipEntry::getName)
                    .filter(filter)
                    .toList());
        } catch (Exception e) {
            // do nothing
        }
        return entries;
    }

    public static InputStream getInputStream(ApplicationContext applicationContext, File jarFile, String resource) throws MalformedURLException {
        ClassLoader parentLoader = applicationContext.getClassLoader();
        URL jarUrl = jarFile.toURI().toURL();
        @SuppressWarnings("resource") URLClassLoader moduleLoader = new URLClassLoader(new URL[]{jarUrl}, parentLoader);
        return moduleLoader.getResourceAsStream(resource);
    }
}
