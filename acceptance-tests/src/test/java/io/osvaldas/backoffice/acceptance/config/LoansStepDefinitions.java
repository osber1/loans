package io.osvaldas.backoffice.acceptance.config;

import static io.osvaldas.backoffice.acceptance.config.Operations.buildLoanRequest;
import static io.osvaldas.backoffice.acceptance.config.Operations.buildRegisterClientRequest;
import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.osvaldas.api.clients.ClientRegisterRequest;
import io.osvaldas.api.clients.ClientResponse;
import io.osvaldas.api.loans.LoanResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class LoansStepDefinitions {

    private static final String BASE_URI = configured("acceptance.baseUri", "ACCEPTANCE_BASE_URI", "http://localhost:8080");

    private static final String MAILHOG_URI = configured("acceptance.mailhogUri", "ACCEPTANCE_MAILHOG_URI", "http://localhost:8025");

    private static final int EMAIL_ATTEMPTS = 20;

    private static final long EMAIL_POLL_MILLIS = 500;

    private String clientId;

    private String clientEmail;

    private long loanId;

    @Given("client is registered")
    public void clientIsRegistered() {
        ClientRegisterRequest registerRequest = buildRegisterClientRequest();
        Response response = request()
            .body(registerRequest)
            .post("/api/v1/clients");

        responseSuccess(response);

        clientId = response.as(ClientResponse.class).id();
        clientEmail = registerRequest.email();
    }

    @Given("client is activated")
    public void clientIsActivated() {
        request()
            .queryParam("token", activationTokenFromEmail())
            .post("/api/v1/clients/{clientId}/activation", clientId)
            .then().assertThat().statusCode(200);
    }

    @When("loan is taken with amount {int}")
    public void loanIsTakenWithAmount(int amount) {
        request()
            .queryParam("clientId", clientId)
            .body(buildLoanRequest(valueOf(amount)))
            .post("/api/v1/loans")
            .then().assertThat().statusCode(200);
    }

    @Then("loan is given")
    public void loanIsGiven() {
        Response response = request()
            .queryParam("clientId", clientId)
            .get("/api/v1/loans");

        responseSuccess(response);

        LoanResponse loanResponse = firstLoan(response);

        loanId = loanResponse.id();
        assertThat(loanResponse.amount()).isNotEqualTo(ZERO);
    }

    @When("extension is taken")
    public void extensionIsTaken() {
        request()
            .queryParam("loanId", loanId)
            .post("/api/v1/loans/extensions")
            .then().assertThat().statusCode(200);
    }

    @Then("extension is given")
    public void extensionIsGiven() {
        Response response = request()
            .queryParam("clientId", clientId)
            .get("/api/v1/loans");

        responseSuccess(response);

        assertThat(firstLoan(response).loanPostpones()).isNotEmpty();
    }

    private LoanResponse firstLoan(Response response) {
        LoanResponse[] loans = response.as(LoanResponse[].class);
        assertThat(loans).as("loans of client %s", clientId).isNotEmpty();
        return loans[0];
    }

    private String activationTokenFromEmail() {
        Pattern link = Pattern.compile("/clients/" + Pattern.quote(clientId) + "/activation\\?token=([\\w-]+)");
        for (int attempt = 0; attempt < EMAIL_ATTEMPTS; attempt++) {
            List<String> bodies = given()
                .baseUri(MAILHOG_URI)
                .queryParam("kind", "to")
                .queryParam("query", clientEmail)
                .get("/api/v2/search")
                .jsonPath()
                .getList("items.Content.Body", String.class);
            for (String body : bodies) {
                Matcher matcher = link.matcher(body);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            sleep();
        }
        throw new AssertionError("No activation email for %s found in %s".formatted(clientEmail, MAILHOG_URI));
    }

    private static void sleep() {
        try {
            Thread.sleep(EMAIL_POLL_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static String configured(String property, String environmentVariable, String defaultValue) {
        String fromProperty = System.getProperty(property);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String fromEnvironment = System.getenv(environmentVariable);
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        return defaultValue;
    }

    private static RequestSpecification request() {
        return given()
            .header("Content-Type", "application/json")
            .baseUri(BASE_URI);
    }

    private static void responseSuccess(Response response) {
        response.then()
            .assertThat().statusCode(200);
    }
}
