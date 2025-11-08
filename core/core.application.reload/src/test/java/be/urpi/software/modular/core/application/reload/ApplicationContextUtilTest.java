package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.properties.ModularProperties;
import be.urpi.software.modular.core.properties.SpringContextType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;

@ExtendWith(SpringExtension.class)
class ApplicationContextUtilTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void setOnClassPath() throws IOException {
        ModularProperties modularProperties = Mockito.mock(ModularProperties.class);
        Mockito.when(modularProperties.getType()).thenReturn(SpringContextType.JAVA);
        File file = new ClassPathResource("module-x.jar").getFile();
        File coreRest = new ClassPathResource("core-rest.jar").getFile();
        File jarDirectory = new File(coreRest.getParentFile(), "jar");
        FileUtils.copyFile(file, new File(jarDirectory, "module-x.jar"));
        FileUtils.copyFile(coreRest, new File(jarDirectory, "core-rest.jar"));
        Assertions.assertTrue(file.exists());
        ApplicationContextUtil.refresh(modularProperties, applicationContext, coreRest);
        ApplicationContextUtil.refresh(modularProperties, applicationContext, file);
        Assertions.assertTrue(applicationContext.containsBean("helloService"));
    }
}