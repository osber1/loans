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

@Component
@Endpoint(id = "release-notes")
public class ActuatorReleaseNotesEndpoint {

    private final Map<String, List<String>> releaseNotesMap = new LinkedHashMap<>(Map.of("version-1.0", of("Risk evaluation added", "Using PostgreSQL for storage")));

    @ReadOperation
    public synchronized Map<String, List<String>> getReleaseNotes() {
        return new LinkedHashMap<>(releaseNotesMap);
    }

    @ReadOperation
    public synchronized List<String> getNotesByVersion(@Selector String version) {
        return releaseNotesMap.get(version);
    }

    @WriteOperation
    public synchronized void addReleaseNotes(@Selector String version, String releaseNotes) {
        releaseNotesMap.put(version, Arrays.asList(releaseNotes.split(",")));
    }

    @DeleteOperation
    public synchronized void deleteNotes(@Selector String version) {
        releaseNotesMap.remove(version);
    }
}
