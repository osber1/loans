package io.osvaldas.backoffice.acceptance

import static io.cucumber.groovy.EN.Given
import static io.cucumber.groovy.EN.Then
import static io.cucumber.groovy.EN.When
import static io.cucumber.groovy.Hooks.Before
import static io.osvaldas.backoffice.acceptance.config.StepdefDependencyInjector.injectMembersTo
import static io.restassured.RestAssured.baseURI
import static io.restassured.RestAssured.given

import com.google.inject.Inject

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.Field
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

@Inject @Field Operations operations
JsonSlurper jsonSlurper = new JsonSlurper()

String clientId
String loanId

Before {
    injectMembersTo this
}

baseURI = 'http://localhost:8080'

Given(~/^client is registered$/) { ->
    Response response = request()
        .body(new JsonBuilder(operations.buildRegisterClientRequest()) as String)
        .post('/api/v1/clients')

    assert responseSuccess(response)

    String jsonString = response.asString()
    clientId = jsonSlurper.parseText(jsonString)['id']
}

Then(~/^client is activated$/) { ->
    Response response = request()
        .get("/api/v1/clients/${clientId}/active")

    assert responseSuccess(response)
}

When(~/^loan is taken with amount (\d+)$/) { BigDecimal amount ->
    Response response = request()
        .queryParam('clientId', clientId)
        .body(new JsonBuilder(operations.buildLoanRequest(amount)) as String)
        .post('/api/v1/loans')

    assert responseSuccess(response)
}

Then(~/^loan is given$/) { ->
    Response response = request()
        .queryParam('clientId', clientId)
        .get('/api/v1/loans')

    assert responseSuccess(response)

    String jsonString = response.asString()

    assert jsonSlurper.parseText(jsonString)[0]['amount'] != 0

    loanId = jsonSlurper.parseText(jsonString)[0]['id']
}

When(~/^extension is taken$/) { ->
    Response response = request()
        .queryParam('loanId', loanId)
        .post('api/v1/loans/extensions')

    assert responseSuccess(response)
}

Then(~/^extension is given$/) { ->
    Response response = request()
        .queryParam('clientId', clientId)
        .get('/api/v1/loans')

    assert responseSuccess(response)

    String jsonString = response.asString()

    assert (jsonSlurper.parseText(jsonString)[0]['loanPostpones'] as List).size() != 0
}

private static RequestSpecification request() {
    given()
        .header('Content-Type', 'application/json')
}

private static boolean responseSuccess(Response response) {
    response.statusCode == 200
}
