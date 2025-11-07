package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.jar.util.JarFileUtil;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An utility class that will update the application context with the bean definitions that comes from the child context will be processing a jar containing spring beans
 *
 * @author danyel
 */
@Slf4j
final
class ApplicationContextUtil {
    // registry for "caching" the child context so that we don't need to process existing jar already added to the application context
    static final Map<File, AnnotationConfigApplicationContext> MODULE_REGISTRY = new ConcurrentHashMap<>();
    // begin package names to exclude
    static final String MODULAR_PACKAGE = "be.urpi.software.modular";
    static final String SPRING_PACKAGE = "org.springframework";
    // end package names to exclude
    // the predicate to filter out the classes to be registered
    static final Predicate<String> FILTER_PREDICATE = name -> name.endsWith(".class") && (!name.contains(MODULAR_PACKAGE.replace(".", "/")) || !name.contains(SPRING_PACKAGE.replace(".", "/")));

    ApplicationContextUtil() {
        throw new IllegalAccessError("You can not instantiate an utility class.");
    }

    /**
     * For each new bean definition that is not already been registered in the parent context than a new bean definition will be added to the parent context that comes from the child context
     *
     * @param parentContext the application context from the application
     * @param childContext  the new created application context that has the parent context as its parent
     */
    static void mergeIntoParent(ApplicationContext parentContext, AnnotationConfigApplicationContext childContext) {
        if (!(parentContext.getAutowireCapableBeanFactory() instanceof BeanDefinitionRegistry parentRegistry)) {
            throw new IllegalStateException("Parent context is not a BeanDefinitionRegistry");
        }

        Arrays.stream(childContext.getBeanDefinitionNames())
                .parallel()
                .filter(beanName -> !parentRegistry.containsBeanDefinition(beanName))
                .forEach(beanName -> {
                    BeanDefinition beanDefinition = childContext.getBeanFactory().getBeanDefinition(beanName);
                    parentRegistry.registerBeanDefinition(beanName, beanDefinition);
                });

        RestServiceManager restServiceManager = parentContext.getBean(RestServiceManager.BEAN_NAME, RestServiceManager.class);
        restServiceManager.load(parentContext);
        RequestMappingHandlerMapping mapping = parentContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        mapping.afterPropertiesSet();
    }

    /**
     * After the application has been started and if there are jar files to be registered into the application context so that the beans are accessible from the application
     *
     * @param parentContext the application context that has been created from the start-up of the application
     * @param jarFile       the jar file to be registered into the application context
     */
    static void refresh(ApplicationContext parentContext, File jarFile) {
        try {
            // only process new files
            if (!MODULE_REGISTRY.containsKey(jarFile)) {
                AnnotationConfigApplicationContext childContext = createChildContext(parentContext, jarFile);
                List<String> entries = JarFileUtil.getEntries(jarFile, FILTER_PREDICATE);
                registerBeans(childContext, entries);
                // Diagnostics: log bean names and classloader
                printBeans(jarFile, childContext);
                // Keep the module context alive for future use
                MODULE_REGISTRY.put(jarFile, childContext);
                mergeIntoParent(parentContext, childContext);
            }
        } catch (Throwable t) {
            log.error("Failed to refresh module {}: {}", jarFile.getAbsolutePath(), t.getMessage(), t);
        }
    }

    /**
     * For each entry that has been retrieved from the jar file will be registered in the child context after it has been checked for spring bean annotations.
     *
     * @param childContext the child context where the class names will be registered
     * @param entries      the class names retrieved from the jar file
     */
    private static void registerBeans(AnnotationConfigApplicationContext childContext, List<String> entries) {
        entries.forEach(entry -> {
            String className = entry.replace('/', '.').replace(".class", "");
            try {
                Class<?> clazz = Class.forName(className, false, childContext.getClassLoader());
                // only register classes that look like configuration or component classes
                if (hasSpringAnnotation(clazz)) {
                    childContext.register(clazz);
                }
            } catch (ClassNotFoundException ignored) {
                // skip it
            }
        });
        childContext.refresh();
    }

    /**
     * Creates a child context with a class loader that will contain the jar file
     *
     * @param parentContext the parent context that has been created on start up of the application
     * @param jarFile       the jar file to be put on the class loader to create the bean definitions
     * @return child context will be returned with its own class loader
     * @throws MalformedURLException this should never have happened because we already have checked that the jar file exists
     */
    private static AnnotationConfigApplicationContext createChildContext(ApplicationContext parentContext, File jarFile) throws MalformedURLException {
        ClassLoader parentLoader = parentContext.getClassLoader();
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader moduleLoader = new URLClassLoader(new URL[]{jarUrl}, parentLoader);
        AnnotationConfigApplicationContext moduleContext = new AnnotationConfigApplicationContext();
        moduleContext.setParent(parentContext);
        moduleContext.setClassLoader(moduleLoader);
        return moduleContext;
    }

    /**
     * Prints the beans that are going to be added to the application context
     *
     * @param jarFile      the jar file
     * @param childContext the child context
     */
    private static void printBeans(File jarFile, AnnotationConfigApplicationContext childContext) {
        log.debug("Loaded module from JAR: {}", jarFile.getAbsolutePath());
        Stream.of(childContext.getBeanDefinitionNames())
                .sorted()
                .forEach(beanName -> {
                    try {
                        Object bean = childContext.getBean(beanName);
                        log.debug("  bean: {} -> {}", beanName, bean.getClass().getName());
                    } catch (Exception e) {
                        log.error("  bean: {} -> (could not instantiate) {}", beanName, e.getMessage(), e);
                    }
                });
    }

    /**
     * checks it the class is annotated with a spring annotation to create/configure spring beans
     *
     * @param clazz the class
     * @return true if annotated otherwise false
     */
    private static boolean hasSpringAnnotation(Class<?> clazz) {
        return clazz.getAnnotation(org.springframework.context.annotation.Configuration.class) != null
                || clazz.getAnnotation(org.springframework.stereotype.Controller.class) != null
                || clazz.getAnnotation(org.springframework.stereotype.Component.class) != null
                || clazz.getAnnotation(org.springframework.stereotype.Service.class) != null
                || clazz.getAnnotation(org.springframework.stereotype.Repository.class) != null
                || clazz.getAnnotation(org.springframework.web.bind.annotation.RestController.class) != null;
    }
}
