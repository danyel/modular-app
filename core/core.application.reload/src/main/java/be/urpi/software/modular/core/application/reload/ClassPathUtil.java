package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.service.manager.RestServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Slf4j
class ClassPathUtil {
    static final Map<File, AnnotationConfigApplicationContext> REGISTERED_MODULES = new ConcurrentHashMap<>();
    static final String MODULAR_PACKAGE = "be.urpi.software.modular";
    static final String SPRING_PACKAGE = "org.springframework";
    static final Predicate<String> FILTER_PREDICATE = name -> name.endsWith(".class") && (!name.contains(MODULAR_PACKAGE.replace(".", "/")) || !name.contains(SPRING_PACKAGE.replace(".", "/")));

    ClassPathUtil() {
        throw new IllegalAccessError("You can not instantiate an utility class.");
    }

    static void mergeIntoParent(ApplicationContext parentContext, AnnotationConfigApplicationContext childContext) {
        if (!(parentContext.getAutowireCapableBeanFactory() instanceof BeanDefinitionRegistry parentRegistry)) {
            throw new IllegalStateException("Parent context is not a BeanDefinitionRegistry");
        }

        for (String beanName : childContext.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = childContext.getBeanFactory().getBeanDefinition(beanName);

            if (!parentRegistry.containsBeanDefinition(beanName)) {
                parentRegistry.registerBeanDefinition(beanName, beanDefinition);
            }
        }
        RestServiceManager restServiceManager = parentContext.getBean(RestServiceManager.BEAN_NAME, RestServiceManager.class);
        restServiceManager.load(parentContext);
        RequestMappingHandlerMapping mapping = parentContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        mapping.afterPropertiesSet();
    }

    static void refresh(ApplicationContext parentContext, File file) {
        try {
            if (!REGISTERED_MODULES.containsKey(file)) {
                AnnotationConfigApplicationContext childContext = createChildContext(parentContext, file);
                List<String> entries = scanSpringBean(file);
                registerBeans(childContext, entries);
                // Diagnostics: log bean names and classloader
                printBeans(file, childContext);
                // Keep the module context alive for future use
                REGISTERED_MODULES.put(file, childContext);
                mergeIntoParent(parentContext, childContext);
            }
        } catch (Throwable t) {
            log.error("Failed to refresh module {}: {}", file.getAbsolutePath(), t.getMessage(), t);
        }
    }

    private static void registerBeans(AnnotationConfigApplicationContext moduleContext, List<String> entries) {
        entries.forEach(entry -> {
            String className = entry.replace('/', '.').replace(".class", "");
            try {
                Class<?> clazz = Class.forName(className, false, moduleContext.getClassLoader());
                // only register classes that look like configuration or component classes
                if (hasSpringAnnotation(clazz)) {
                    moduleContext.register(clazz);
                }
            } catch (ClassNotFoundException ignored) {
                // skip it
            }
        });
        moduleContext.refresh();
    }

    private static List<String> scanSpringBean(File file) {
        List<String> entries = new ArrayList<>();
        try (JarFile jarFile = new JarFile(file)) {
            entries.addAll(jarFile.stream()
                    .map(ZipEntry::getName)
                    .filter(FILTER_PREDICATE)
                    .toList());
        } catch (Exception e) {
            // do nothing
        }
        return entries;
    }

    private static AnnotationConfigApplicationContext createChildContext(ApplicationContext applicationContext, File file) throws MalformedURLException {
        ClassLoader parentLoader = applicationContext.getClassLoader();
        URL jarUrl = file.toURI().toURL();
        URLClassLoader moduleLoader = new URLClassLoader(new URL[]{jarUrl}, parentLoader);
        AnnotationConfigApplicationContext moduleContext = new AnnotationConfigApplicationContext();
        moduleContext.setParent(applicationContext);
        moduleContext.setClassLoader(moduleLoader);
        return moduleContext;
    }

    private static void printBeans(File file, AnnotationConfigApplicationContext moduleContext) {
        String[] beanNames = moduleContext.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        log.debug("Loaded module from JAR: {}", file.getAbsolutePath());
        for (String beanName : beanNames) {
            try {
                Object bean = moduleContext.getBean(beanName);
                log.debug("  bean: {} -> {}", beanName, bean.getClass().getName());
            } catch (Exception e) {
                log.error("  bean: {} -> (could not instantiate) {}", beanName, e.getMessage(), e);
            }
        }
    }

    private static boolean hasSpringAnnotation(Class<?> clazz) {
        return clazz.getAnnotation(org.springframework.context.annotation.Configuration.class) != null
                || clazz.getAnnotation(org.springframework.stereotype.Controller.class) != null
                || clazz.getAnnotation(org.springframework.stereotype.Component.class) != null
                || clazz.getAnnotation(org.springframework.stereotype.Service.class) != null
                || clazz.getAnnotation(org.springframework.stereotype.Repository.class) != null
                || clazz.getAnnotation(org.springframework.web.bind.annotation.RestController.class) != null;
    }
}
