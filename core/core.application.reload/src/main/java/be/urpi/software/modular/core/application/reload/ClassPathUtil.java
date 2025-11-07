package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.service.manager.RestServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

class ClassPathUtil {
    static final Logger log = LoggerFactory.getLogger(ClassPathUtil.class);
    static final String JAVA_CLASS_PATH = "java.class.path";
    private static final Map<File, AnnotationConfigApplicationContext> modules = new ConcurrentHashMap<>();
    private final static String baseName = "be.urpi.software.modular";

    ClassPathUtil() {
        throw new IllegalAccessError("You can not instantiate an utility class.");
    }

    static void setOnClassPath(File destinationFile) {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        try {
            URLClassLoader newLoader = new URLClassLoader(new URL[]{destinationFile.toURI().toURL()}, parent);
            Thread.currentThread().setContextClassLoader(newLoader);
            String classPath = System.getProperty(JAVA_CLASS_PATH);

            if (classPath != null && !classPath.contains(destinationFile.getCanonicalPath())) {
                classPath += File.pathSeparatorChar + destinationFile.getCanonicalPath();
            } else {
                classPath = destinationFile.getCanonicalPath();
            }
            log.debug("😇 Setting class path to: {}", classPath);
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

    public static void mergeIntoParent(ApplicationContext parent, AnnotationConfigApplicationContext child) {
        if (!(parent.getAutowireCapableBeanFactory() instanceof BeanDefinitionRegistry parentRegistry)) {
            throw new IllegalStateException("Parent context is not a BeanDefinitionRegistry");
        }
        String[] beanNames = child.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = child.getBeanFactory().getBeanDefinition(beanName);

            if (!parentRegistry.containsBeanDefinition(beanName)) {
                parentRegistry.registerBeanDefinition(beanName, beanDefinition);
            }
        }
        RestServiceManager restServiceManager = parent.getBean(RestServiceManager.BEAN_NAME, RestServiceManager.class);
        restServiceManager.load(parent);
        RequestMappingHandlerMapping mapping = parent.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        mapping.afterPropertiesSet();
    }

    static void refresh(ApplicationContext applicationContext, File file) {
        try {
            AnnotationConfigApplicationContext annotationConfigApplicationContext = modules.get(file);
            if (annotationConfigApplicationContext == null) {
                ClassLoader parentLoader = applicationContext.getClassLoader();
                URL jarUrl = file.toURI().toURL();
                URLClassLoader moduleLoader = new URLClassLoader(new URL[]{jarUrl}, parentLoader);

                AnnotationConfigApplicationContext moduleContext = new AnnotationConfigApplicationContext();
                moduleContext.setParent(applicationContext);
                moduleContext.setClassLoader(moduleLoader);

                try (JarFile jf = new JarFile(file)) {
                    var entries = jf.stream()
                            .map(ZipEntry::getName)
                            .filter(name -> name.endsWith(".class") && !name.contains(baseName.replace(".", "/")))
                            .toList();

                    for (String entry : entries) {
                        String className = entry.replace('/', '.').replace(".class", "");
                        try {
                            Class<?> clazz = Class.forName(className, false, moduleLoader);
                            // only register classes that look like configuration or component classes
                            if (clazz.getAnnotation(org.springframework.context.annotation.Configuration.class) != null
                                    || clazz.getAnnotation(org.springframework.stereotype.Component.class) != null
                                    || clazz.getAnnotation(org.springframework.web.bind.annotation.RestController.class) != null) {
                                moduleContext.register(clazz);
                            }
                        } catch (ClassNotFoundException ignored) {
                            // skip it
                        }
                    }
                } catch (Exception e) {
                    // do nothing
                }

                moduleContext.refresh();

                // Diagnostics: log bean names and classloader
                String[] beanNames = moduleContext.getBeanDefinitionNames();
                Arrays.sort(beanNames);
                log.debug("Loaded module from JAR: {}", file.getAbsolutePath());
                log.debug("Module classloader: {}", moduleContext.getClassLoader());
                for (String b : beanNames) {
                    try {
                        Object bean = moduleContext.getBean(b);
                        log.debug("  bean: {} -> {}", b, bean.getClass().getName());
                    } catch (Exception e) {
                        log.error("  bean: {} -> (could not instantiate) {}", b, e.getMessage(), e);
                    }
                }

                // Keep the module context alive for future use
                modules.put(file, moduleContext);
                mergeIntoParent(applicationContext, moduleContext);
            }
        } catch (Throwable t) {
            log.error("Failed to refresh module {}: {}", file.getAbsolutePath(), t.getMessage(), t);
        }
    }
}
