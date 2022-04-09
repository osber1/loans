package io.osvaldas.backoffice.infra.configuration.actuator;

import java.util.Collections;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class ActuatorCustomInformation implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("Contribution", Collections.singletonMap("Creator", "Osvaldas Bernatavičius"));
    }
}
