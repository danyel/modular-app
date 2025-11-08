package be.urpi.software.modular.core.properties;

import be.urpi.software.modular.core.watcher.WatchAbleException;
import org.springframework.util.StringUtils;

import java.util.Properties;

import static be.urpi.software.modular.core.properties.SpringContextType.*;

public class ModularProperties extends Properties {

    public String getName() {
        String property = getProperty("modular.name");
        if (!StringUtils.hasText(property)) {
            throw new WatchAbleException(new IllegalArgumentException("Property 'modular.name' cannot be empty"));
        }
        return property;
    }

    /**
     * If modular.type is not provided than JAVA will be returned
     * If provided that check if it is a valid value, if invalid return JAVA otherwise the value
     *
     * @return type of context
     */
    public SpringContextType getType() {
        String property = getProperty("modular.type");
        return !StringUtils.hasText(property) ?
                JAVA :
                (!property.equalsIgnoreCase(XML.name()) && !property.equalsIgnoreCase(JAVA.name()) ?
                        JAVA :
                        valueOf(property.toUpperCase()));
    }
}
