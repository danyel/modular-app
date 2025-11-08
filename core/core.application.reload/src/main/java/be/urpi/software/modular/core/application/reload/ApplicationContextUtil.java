package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.jar.util.JarFileUtil;
import be.urpi.software.modular.core.properties.ModularProperties;
import be.urpi.software.modular.core.properties.SpringContextType;
import be.urpi.software.modular.core.service.manager.RestServiceManager;
import be.urpi.software.modular.core.watcher.WatchAbleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
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
    static final Map<File, ApplicationContext> MODULE_REGISTRY = new ConcurrentHashMap<>();
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
    static void mergeIntoParent(ModularProperties modularProperties, ApplicationContext parentContext, GenericApplicationContext childContext) {
        if (!(parentContext.getAutowireCapableBeanFactory() instanceof BeanDefinitionRegistry parentRegistry)) {
            throw new IllegalStateException("Parent context is not a BeanDefinitionRegistry");
        }

        Arrays.stream(childContext.getBeanDefinitionNames())
                .parallel()
                .filter(beanName -> !parentRegistry.containsBeanDefinition(beanName))
                .forEach(beanName -> {
                    BeanDefinition beanDefinition = null;

                    if (modularProperties.getType() == SpringContextType.JAVA) {
                        beanDefinition = childContext.getBeanFactory().getBeanDefinition(beanName);
                    } else {
                        BeanDefinition childBeanDefinition = childContext.getBeanFactory().getBeanDefinition(beanName);
                        RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
                        rootBeanDefinition.setOriginatingBeanDefinition(childBeanDefinition);

                        // Force the bean class to be resolved using the child's loader
                        if (childBeanDefinition.getBeanClassName() != null) {
                            try {
                                Class<?> clazz = Class.forName(childBeanDefinition.getBeanClassName(), true, childContext.getClassLoader());
                                rootBeanDefinition.setBeanClass(clazz);
                                beanDefinition = rootBeanDefinition;
                            } catch (ClassNotFoundException e) {
                                throw new IllegalStateException("Could not load class " + childBeanDefinition.getBeanClassName(), e);
                            }
                        }
                    }

                    if (beanDefinition != null) {
                        parentRegistry.registerBeanDefinition(beanName, beanDefinition);
                    }
                });

        RestServiceManager restServiceManager = parentContext.getBean(RestServiceManager.BEAN_NAME, RestServiceManager.class);
        restServiceManager.load(parentContext);
        RequestMappingHandlerMapping mapping = parentContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        reinitializeRequestMappings(mapping);
        log.info("Done");
    }

    private static void reinitializeRequestMappings(RequestMappingHandlerMapping mapping) {
        try {
            // Access the MappingRegistry (protected inner class)
            Field registryField = AbstractHandlerMethodMapping.class.getDeclaredField("mappingRegistry");
            registryField.setAccessible(true);
            Object mappingRegistry = registryField.get(mapping);

            Field registry = mappingRegistry.getClass().getDeclaredField("registry");
            Field pathLookup = mappingRegistry.getClass().getDeclaredField("pathLookup");
            Field nameLookup = mappingRegistry.getClass().getDeclaredField("nameLookup");
            registry.setAccessible(true);
            registry.set(mappingRegistry, new HashMap<>());
            pathLookup.setAccessible(true);
            pathLookup.set(mappingRegistry, new LinkedMultiValueMap<>());
            nameLookup.setAccessible(true);
            nameLookup.set(mappingRegistry, new ConcurrentHashMap<>());
            registry.setAccessible(false);
            pathLookup.setAccessible(false);
            nameLookup.setAccessible(false);
        } catch (Exception e) {
            // fallback: clear handler methods directly
        }

        // rebuild
        mapping.afterPropertiesSet();
    }


    /**
     * After the application has been started and if there are jar files to be registered into the application context so that the beans are accessible from the application
     *
     * @param parentContext the application context that has been created from the start-up of the application
     * @param jarFile       the jar file to be registered into the application context
     */
    static void refresh(ModularProperties properties, ApplicationContext parentContext, File jarFile) {
        try {
            if (!MODULE_REGISTRY.containsKey(jarFile)) {
                AnnotationConfigApplicationContext childContext = createChildContext(parentContext, jarFile);
                List<String> entries = JarFileUtil.getEntries(jarFile, FILTER_PREDICATE);
                registerBeans(childContext, entries);
                printBeans(jarFile, childContext);
                // Keep the module context alive for future use
                mergeIntoParent(properties, parentContext, childContext);
            }
        } catch (Throwable t) {
            log.error("Failed to refresh module {}: {}", jarFile.getAbsolutePath(), t.getMessage(), t);
        }
    }

    /**
     * After the application has been started and if there are jar files to be registered into the application context so that the beans are accessible from the application which are configured via xml
     *
     * @param properties    modular configuration properties
     * @param parentContext the application context created on start-up
     * @param jarFile       the jar file where the xml context will be fetched from
     */
    static void refreshXml(ModularProperties properties, GenericApplicationContext parentContext, File jarFile) {
        try {
            if (!MODULE_REGISTRY.containsKey(jarFile)) {
                GenericXmlApplicationContext childContext = createChildXmlContext(properties, parentContext, jarFile);
                printBeans(jarFile, childContext);
                MODULE_REGISTRY.put(jarFile, childContext);
                mergeIntoParent(properties, parentContext, childContext);
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
        URLClassLoader childLoader = new URLClassLoader(new URL[]{jarUrl}, parentLoader);
        AnnotationConfigApplicationContext moduleContext = new AnnotationConfigApplicationContext();
        moduleContext.setParent(parentContext);
        moduleContext.setClassLoader(childLoader);
        return moduleContext;
    }

    /**
     * Creates a child xml context with a class loader that contains the jar file
     *
     * @param properties    Modular properties
     * @param parentContext the application context which has been created oin start-up
     * @param jarFile       the file to scan
     * @return the child context
     * @throws MalformedURLException when the url can not be parsed but that can not happen
     */
    private static GenericXmlApplicationContext createChildXmlContext(ModularProperties properties, GenericApplicationContext parentContext, File jarFile) throws MalformedURLException {
        ClassLoader parentLoader = parentContext.getClassLoader();
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader childLoader = new URLClassLoader(new URL[]{jarUrl}, parentLoader);

        String resourceName = "modular-%s-ctx.xml".formatted(properties.getName());
        URL resourceUrl = childLoader.getResource(resourceName);
        if (resourceUrl == null) {
            throw new WatchAbleException(
                    new IllegalArgumentException("Resource not found: " + jarFile.getAbsolutePath())
            );
        }
        GenericXmlApplicationContext childContext = new GenericXmlApplicationContext();
        childContext.setParent(parentContext);
        childContext.setClassLoader(childLoader);
        childContext.load(resourceUrl.toString());
        childContext.refresh();
        for (String entry : JarFileUtil.getEntries(jarFile, name -> true)) {
            log.info("Classpath entry: {}", entry);
        }

        return childContext;
    }

    /**
     * Prints the beans that are going to be added to the application context
     *
     * @param jarFile      the jar file
     * @param childContext the child context
     */
    private static void printBeans(File jarFile, ApplicationContext childContext) {
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
