package io.osvaldas.backoffice.infra.configuration

import org.springframework.boot.SpringBootVersion
import org.springframework.boot.actuate.info.Info

import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.infra.configuration.actuator.ActuatorCustomInformation
import io.osvaldas.backoffice.infra.configuration.actuator.ActuatorReleaseNotesEndpoint

class ActuatorCustomInformationSpec extends AbstractSpec {

    void 'should report the running spring boot version'() {
        given:
            Info.Builder builder = new Info.Builder()
        when:
            new ActuatorCustomInformation().contribute(builder)
        then:
            builder.build().details.springBootVersion == SpringBootVersion.version
    }

    void 'should add, read and delete release notes'() {
        given:
            ActuatorReleaseNotesEndpoint endpoint = new ActuatorReleaseNotesEndpoint()
        when:
            endpoint.addReleaseNotes('version-2.0', 'first,second')
        then:
            endpoint.getNotesByVersion('version-2.0') == ['first', 'second']
            endpoint.releaseNotes.keySet() == ['version-1.0', 'version-2.0'] as Set
        when:
            endpoint.deleteNotes('version-2.0')
        then:
            endpoint.getNotesByVersion('version-2.0') == null
    }

}
