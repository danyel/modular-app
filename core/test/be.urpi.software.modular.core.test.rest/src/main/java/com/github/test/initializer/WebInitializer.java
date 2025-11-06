package com.github.test.initializer;

import com.github.test.spring.config.TestWebConfiguration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class WebInitializer implements WebApplicationInitializer {
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext uiContext = new AnnotationConfigWebApplicationContext();
        uiContext.setDisplayName("UI Web");
        uiContext.register(TestWebConfiguration.class);
        servletContext.addListener(new ContextLoaderListener(uiContext));

        //Dispatcher servlet
        ServletRegistration.Dynamic uiDispatcher = servletContext.addServlet("uiDispatcher", new DispatcherServlet(uiContext));
        uiDispatcher.setLoadOnStartup(1);
        uiDispatcher.addMapping("/api/*");
    }
}
