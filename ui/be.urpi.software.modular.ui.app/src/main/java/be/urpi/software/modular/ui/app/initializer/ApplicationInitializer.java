package be.urpi.software.modular.ui.app.initializer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.io.IOException;

public class ApplicationInitializer implements WebApplicationInitializer {
    public void onStartup(final ServletContext servletContext) throws ServletException {
        final AbstractRefreshableWebApplicationContext uiContext = new AbstractRefreshableWebApplicationContext() {
            protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
            }

            @Override
            protected void loadBeanDefinitions(final DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
                // Create a new XmlBeanDefinitionReader for the given BeanFactory.
                XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

                // Configure the bean definition reader with this context's
                // resource loading environment.
                beanDefinitionReader.setEnvironment(getEnvironment());
                beanDefinitionReader.setResourceLoader(this);
                beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

                // Allow a subclass to provide custom initialization of the reader,
                // then proceed with actually loading the bean definitions.
                initBeanDefinitionReader(beanDefinitionReader);
                loadBeanDefinitions(beanDefinitionReader);
            }

            protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
                String[] configLocations = getConfigLocations();
                if (configLocations != null) {
                    for (String configLocation : configLocations) {
                        reader.loadBeanDefinitions(configLocation);
                    }
                }
            }

            protected String[] getDefaultConfigLocations() {
                return new String[]{"classpath:/be/urpi/software/modular/ui/static/resources/static-resources-ctx.xml"};
            }
        };
        uiContext.setDisplayName("UI");
        servletContext.addListener(new ContextLoaderListener(uiContext));

        //Dispatcher servlet
        final ServletRegistration.Dynamic uiDispatcher = servletContext.addServlet("uiDispatcher", new DispatcherServlet(uiContext));
        uiDispatcher.setLoadOnStartup(1);
        uiDispatcher.addMapping("/*");
    }
}
