package be.urpi.software.modular.core.test.rest.initializer;

import be.urpi.software.modular.core.test.rest.spring.config.TestWebConfiguration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class WebInitializer implements WebApplicationInitializer {
    public void onStartup(final ServletContext servletContext) throws ServletException {
        final AnnotationConfigWebApplicationContext uiContext = new AnnotationConfigWebApplicationContext();
        uiContext.setDisplayName("UI Web");
        uiContext.register(TestWebConfiguration.class);
        servletContext.addListener(new ContextLoaderListener(uiContext));

        //Dispatcher servlet
        final ServletRegistration.Dynamic uiDispatcher = servletContext.addServlet("uiDispatcher", new DispatcherServlet(uiContext));
        uiDispatcher.setLoadOnStartup(1);
        uiDispatcher.addMapping("/api/*");
    }
}
