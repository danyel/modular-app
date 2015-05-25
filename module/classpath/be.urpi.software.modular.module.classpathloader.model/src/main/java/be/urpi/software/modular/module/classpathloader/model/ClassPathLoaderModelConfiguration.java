package be.urpi.software.modular.module.classpathloader.model;

import org.springframework.context.annotation.ImportResource;

@org.springframework.context.annotation.Configuration
@ImportResource(value = {"classpath:META-INF/context/library-properties-ctx.xml", "classpath*:module-*-ctx.xml"})
public class ClassPathLoaderModelConfiguration {
}
