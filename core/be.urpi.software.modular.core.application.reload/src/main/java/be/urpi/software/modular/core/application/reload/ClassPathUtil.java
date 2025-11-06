package be.urpi.software.modular.core.application.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

class ClassPathUtil {
    static final Logger log = LoggerFactory.getLogger(ClassPathUtil.class);
    static final String JAVA_CLASS_PATH = "java.class.path";

    ClassPathUtil() {
        throw new IllegalAccessError("You can not instantiate an utility class.");
    }

    static void setOnClassPath(File destinationFile) {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        try {
            URLClassLoader newLoader = new URLClassLoader(new URL[]{destinationFile.toURI().toURL()}, parent);
            Thread.currentThread().setContextClassLoader(newLoader);
            String classPath = System.getProperty(JAVA_CLASS_PATH);

            if (classPath != null) {
                classPath += File.pathSeparatorChar + destinationFile.getCanonicalPath();
            } else {
                classPath = destinationFile.getCanonicalPath();
            }

            for (Thread t : Thread.getAllStackTraces().keySet()) {
                if (t != Thread.currentThread() && !t.getClass().getSimpleName().equals("InnocuousThread")) {
                    t.setContextClassLoader(newLoader);
                }
            }

            System.setProperty(JAVA_CLASS_PATH, classPath);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
