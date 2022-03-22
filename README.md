# Task for 4finance

## Swagger

http://localhost:8080/swagger-ui/

# Feedback

### THE GOOD:

- [x] Swagger documentation added.

- [x] Separate module for api.

- [x] Use of ZonedDateTime.

- [x] Use of lombok.

- [x] Validations for api classes.

- [x] Use of scoped configuration objects. (?)

- [x] Use of final fields.

- [x] Use of request/response objects (not a domain ones).

- [x] Mapper/builder utils used.

- [x] Persistence cascading seems to be correctly used.

- [x] Controller advice used.

- [x] Exception hierarchy for api error handling.

- [x] Names are short and easy to understand.

- [x] Test methods in IpLogsRepositoryTest looks perfect -> just 3 lines of code in each. Easy to read.

### IRRELEVANT (Just me being a douche)

- [x] DTO class naming... DTO in class name is just a noise, would argue that using Request/Response would provide a more concrete context and scope where those objects could be
  used.

- [x] Naming with noise. e.g. existingClientOptional. No need to define that its optional (this can be read from type).

- [x] If using zoned date, might be good idea to make zone configurable for service and not take default from os... (?) How to use config in static method

### DEBATABLE:

- [x] Use of List for collections which seem to be holding unique objects. Would use Set in this case.

- [x] Top lvl package naming is based on architectural layering of application. (?) How it should be named?

- [x] Not sure if it's a good idea to use same object for request and response (ClientDTO), thinking about possible evolution of api.

### THE BAD:

- [x] Inconsistent use of primitives vs primitive wrappers. (e.g., Integer, int).

- [x] Double is used for currency amount (which might become THE UGLY in some cases as more precises operations may lead to inconsistencies). Would suggest using either BigDecimal,
  or long and int to represent e.g Eur and cents...

- [x] Configuration object props are not validated (e.g., incorrect configuration of maxAmount, interest rate). ForbiddenHourTo, forbiddenHourFrom, could lead to funky business,
  eg. forbiddenHourTo < forbiddenHourTo , since it is not defined if configuration accepts value 0 < n < 24. (?) How to check max amount and interest rate?

- [x] No validations on persistence model.

- [x] Some unused methods.

- [x] Rest url construction needs some work as it seems to be contradicting the controllers (in which those urls are defined) name, has verbs...

- [x] InterestApplicationSpec seems to be testing nothing.

- [x] Unmanaged database changes, they are generated by hibernate. Please consider using Liquibase or Flyway.

- [x] Spring security dependency only on test scope. So no security on main application code.

- [x] Missing SOLID and Design patterns adaptation on ClientService. Validations should be separated from actual loan issuing action.

- [x] Tests may break if ran after working hours.

- [x] Use separate classes for client request and client response. Using one class already introduced confusion like @ApiModelProperty(hidden = true) private String id. In future
  those classes tend to grow and become more and more complex.

- [x] Not sure what to expect after calling "clients/{id}/history". Sounds like it could be client events, client personal data updates, loans etc.

- [x] Test methods like com.finance.task.interest.controller.ClientControllerTest#testTakeLoan_whenPersonalCodeContainsLetters looks too big. You don't want to flood reader with
  data, which is not important for test. Please consider moving request building logic to separate method. (Clean code)

- [x] Packaging could be by functional zone like 'crm', 'risk' instead of layered structure. (?) Where to split classes

- [x] Anemic domain model. Client, Loan are only DTO's and don't have any logic. (Domain driven design).

### THE UGLY:

- [x] Use of int primitive as id. Will soon run into limitation of number of records that can be identified with this primitive...

- [x] Service class is bloated (could be split into several) in along will become hard to manage. (?)

- [x] @Transactional on class level (this will put wrap all methods in transactional state, and some of them do not need to be wrapped, e.g., getClientHistory).

- [x] (int) client.getLoan().getAmount() > config.getMaxAmount() there is an error somewhere in here related to casting double to int.

TODO:

- [x] Start using Redis for ip validation.

- [x] Enable Testcontainers.

- [x] Migrate from FlyWay to Liquidbase.

- [x] Use ip validation in filter, not in service.

- [x] Add full CRUD for client and loans.

- [ ] Add Spring Security with JWT tokens on every request (okta or keycloak).

- [ ] Users receive email on registration and have to confirm email.

- [ ] FE.