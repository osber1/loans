package io.osvaldas.backoffice.infra.rest.clients;

import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.backoffice.domain.clients.ClientService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1")
public class ClientActivationController {

    static final String CONFIRMATION_PAGE = """
        <!doctype html>
        <html lang="en">
        <head><meta charset="utf-8"><title>Activate your account</title></head>
        <body>
        <h1>Activate your account</h1>
        <form method="post"><button type="submit">Activate</button></form>
        </body>
        </html>
        """;

    static final String ACTIVATED_PAGE = """
        <!doctype html>
        <html lang="en">
        <head><meta charset="utf-8"><title>Account activated</title></head>
        <body><h1>Your account is activated</h1></body>
        </html>
        """;

    private final ClientService service;

    @GetMapping(value = "clients/{id}/activation", produces = TEXT_HTML_VALUE)
    public String confirmActivation(@PathVariable String id, @RequestParam String token) {
        return CONFIRMATION_PAGE;
    }

    @PostMapping(value = "clients/{id}/activation", produces = TEXT_HTML_VALUE)
    public String activateClient(@PathVariable String id, @RequestParam String token) {
        service.activateClient(id, token);
        return ACTIVATED_PAGE;
    }

}
