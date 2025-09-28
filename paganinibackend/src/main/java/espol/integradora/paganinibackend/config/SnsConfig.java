package espol.integradora.paganinibackend.config;

import com.amazonaws.services.sns.*;
import com.amazonaws.regions.Regions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class SnsConfig {
  @Bean
  public AmazonSNS amazonSNS(@Value("${aws.region}") String region) {
    return AmazonSNSClientBuilder.standard()
        .withRegion(Regions.fromName(region))
        .build();
  }
}
