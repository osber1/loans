package io.osvaldas.backoffice.acceptance.config;

import static io.osvaldas.backoffice.acceptance.config.Operations.buildLoanRequest;
import static io.osvaldas.backoffice.acceptance.config.Operations.buildRegisterClientRequest;
import static io.restassured.RestAssured.given;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.osvaldas.api.clients.ClientResponse;
import io.osvaldas.api.loans.LoanResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class LoansStepDefinitions {

    private static final String BASE_URI = baseUri();

    private String clientId;

    private long loanId;

    @Given("client is registered")
    public void clientIsRegistered() {
        Response response = request()
            .body(buildRegisterClientRequest())
            .post("/api/v1/clients");

        responseSuccess(response);

        clientId = response.as(ClientResponse.class).id();
    }

    @Given("client is activated")
    public void clientIsActivated() {
        request()
            .get("/api/v1/clients/{clientId}/active", clientId)
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

    private static String baseUri() {
        String fromProperty = System.getProperty("acceptance.baseUri");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String fromEnvironment = System.getenv("ACCEPTANCE_BASE_URI");
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        return "http://localhost:8080";
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
