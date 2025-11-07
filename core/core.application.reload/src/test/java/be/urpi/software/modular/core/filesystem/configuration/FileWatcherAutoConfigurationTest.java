package be.urpi.software.modular.core.filesystem.configuration;

import be.urpi.software.modular.core.watcher.directory.DirectoryWatchAble;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = FileWatcherAutoConfiguration.class)
class FileWatcherAutoConfigurationTest {
    @Autowired
    private ApplicationContext context;

    @Test
    void shouldCreateBean() {
        assertThat(context.getBean("classPathReload")).isInstanceOf(DirectoryWatchAble.class);
        assertThat(context.getBean("classPathReload")).isSameAs(context.getBean(DirectoryWatchAble.class));
    }
}