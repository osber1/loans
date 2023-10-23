package io.osvaldas.backoffice.infra.configuration.actuator;

import static java.util.List.of;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Endpoint(id = "release-notes")
public class ActuatorReleaseNotesEndpoint {

    Map<String, List<String>> releaseNotesMap = new LinkedHashMap<>();

    @PostConstruct
    public void initNotes() {
        releaseNotesMap.put("version-1.0", of("Risk evaluation added", "Using PostgreSQL for storage"));
    }

    @ReadOperation
    public Map<String, List<String>> getReleaseNotes() {
        return releaseNotesMap;
    }

    @ReadOperation
    public List<String> getNotesByVersion(@Selector String version) {
        return releaseNotesMap.get(version);
    }

    @WriteOperation
    public void addReleaseNotes(@Selector String version, String releaseNotes) {
        releaseNotesMap.put(version, Arrays.stream(releaseNotes.split(",")).toList());
    }

    @DeleteOperation
    public void deleteNotes(@Selector String version) {
        releaseNotesMap.remove(version);
    }
}
