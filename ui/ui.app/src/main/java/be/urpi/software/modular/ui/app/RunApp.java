package be.urpi.software.modular.ui.app;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RunApp {
//    public void onStartup(final ServletContext servletContext) throws ServletException {
//        final AnnotationConfigWebApplicationContext uiContext = new AnnotationConfigWebApplicationContext();
//        uiContext.register(ApplicationSpringConfig.class);
//        uiContext.setDisplayName("UI");
//        servletContext.addListener(new ContextLoaderListener(uiContext));
//
//        //Dispatcher servlet
//        final ServletRegistration.Dynamic uiDispatcher = servletContext.addServlet("uiDispatcher", new DispatcherServlet(uiContext));
//        uiDispatcher.setLoadOnStartup(1);
//        uiDispatcher.addMapping("/*");
//    }

    public static void main(String[] args) {
        SpringApplication.run(RunApp.class, args);
    }
}
