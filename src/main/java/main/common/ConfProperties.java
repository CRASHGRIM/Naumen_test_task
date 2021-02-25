package main.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@PropertySource("file:src/main/resources/application.properties")
@ConfigurationProperties
public class ConfProperties {

    @Value("${fragmentSize}")
    public int fragmentSize;

    @Value("${indexFragmentSize}")
    public int indexFragmentSize;

    @Value("${titleSize}")
    public int titleSize;

}
