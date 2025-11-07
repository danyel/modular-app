package be.urpi.software.modular.core.application.reload;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

class ClassPathUtilTest {

    @Test
    void setOnClassPath() throws IOException {
        Assertions.assertFalse(System.getProperty(ClassPathUtil.JAVA_CLASS_PATH).contains("module-x.jar"));
        File file = new ClassPathResource("module-x.jar").getFile();
        System.out.println(file.getParentFile().getAbsolutePath());
        File jarDirectory = new File(file.getParentFile().getAbsolutePath(), "/jar");
        FileUtils.forceMkdir(jarDirectory);
        File sourceJar = new File(jarDirectory.getAbsolutePath(), "module-x.jar");
        Assertions.assertTrue(file.exists());
        FileUtils.copyFile(file, sourceJar);
        ClassPathUtil.setOnClassPath(sourceJar);
        Assertions.assertTrue(System.getProperty(ClassPathUtil.JAVA_CLASS_PATH).contains("module-x.jar"));
    }
}