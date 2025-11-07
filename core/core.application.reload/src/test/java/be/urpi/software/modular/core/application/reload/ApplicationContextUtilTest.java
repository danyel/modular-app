package be.urpi.software.modular.core.application.reload;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        File file = new ClassPathResource("module-x.jar").getFile();
        File coreRest = new ClassPathResource("core-rest.jar").getFile();
        File jarDirectory = new File(coreRest.getParentFile(), "jar");
        FileUtils.copyFile(file, new File(jarDirectory, "module-x.jar"));
        FileUtils.copyFile(coreRest, new File(jarDirectory, "core-rest.jar"));
        Assertions.assertTrue(file.exists());
        ApplicationContextUtil.refresh(applicationContext, coreRest);
        ApplicationContextUtil.refresh(applicationContext, file);
        Assertions.assertTrue(applicationContext.containsBean("helloService"));
    }
}