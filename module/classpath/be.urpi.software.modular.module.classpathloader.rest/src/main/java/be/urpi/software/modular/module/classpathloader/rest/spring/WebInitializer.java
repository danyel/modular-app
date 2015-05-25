package be.urpi.software.modular.module.classpathloader.rest.spring;

import be.urpi.software.modular.core.application.reload.Reloader;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.io.IOException;

public class WebInitializer implements WebApplicationInitializer {
    public void onStartup(final ServletContext servletContext) throws ServletException {
        try {
            Reloader.reload("/Users/danyel/test/classpath");
            final AnnotationConfigWebApplicationContext uiContext = new AnnotationConfigWebApplicationContext();
            uiContext.setDisplayName("UI Web");
            uiContext.register(ClassPathLoaderRestConfiguration.class);
            servletContext.addListener(new ContextLoaderListener(uiContext));

            //Dispatcher servlet
            final ServletRegistration.Dynamic uiDispatcher = servletContext.addServlet("uiDispatcher", new DispatcherServlet(uiContext));
            uiDispatcher.setLoadOnStartup(1);
            uiDispatcher.addMapping("/api/*");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
