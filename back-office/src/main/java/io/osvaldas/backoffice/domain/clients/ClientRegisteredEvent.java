package io.osvaldas.backoffice.domain.clients;

public record ClientRegisteredEvent(String clientId, String fullName, String email) {

}
