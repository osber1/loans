package io.osvaldas.backoffice.domain.clients;

/**
 * Published when a new client is registered. Consumed after the registering transaction commits,
 * so a rolled back registration never results in a notification being sent.
 */
public record ClientRegisteredEvent(String clientId, String fullName, String email) {

}
