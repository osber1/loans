package io.osvaldas.backoffice.acceptance

import static io.cucumber.groovy.EN.Given
import static io.cucumber.groovy.EN.Then
import static io.cucumber.groovy.EN.When

import io.cucumber.groovy.PendingException

Given(~/^client is created$/) { ->
    'client is created'
}

Then(~/^client is active$/) { ->
    'client is active'
}

When(~/^loan is taken with amount (\d+)$/) { int amount ->
    "loan taken with amount ${amount}"
}

Then(~/^loan is given$/) { ->
    'loan is given'
    throw new PendingException()
}
