package be.urpi.software.modular.core.application.reload;

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
        Assertions.assertTrue(file.exists());
        ClassPathUtil.setOnClassPath(file);
        Assertions.assertTrue(System.getProperty(ClassPathUtil.JAVA_CLASS_PATH).contains("module-x.jar"));
    }
}