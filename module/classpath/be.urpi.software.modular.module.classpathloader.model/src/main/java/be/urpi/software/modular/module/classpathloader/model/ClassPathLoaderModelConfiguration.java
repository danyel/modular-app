package be.urpi.software.modular.module.classpathloader.model;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(value = {"classpath:META-INF/context/module-*-ctx.xml"})
public class ClassPathLoaderModelConfiguration {
}
