package io.osvaldas.backoffice.infra.configuration.feign;

import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Response;
import feign.codec.ErrorDecoder;
import io.osvaldas.api.exceptions.ApiRequestException.ApiExceptionResource;
import io.osvaldas.api.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    private final ObjectMapper mapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        try (InputStream bodyIs = response.body().asInputStream()) {
            ApiExceptionResource exceptionResource = mapper.readValue(bodyIs, ApiExceptionResource.class);
            if (response.status() == 400) {
                return new BadRequestException(ofNullable(exceptionResource.getMessage()).orElse("Bad Request"));
            }
        } catch (IOException e) {
            log.error("Error decoding error response", e);
        }
        return errorDecoder.decode(methodKey, response);
    }
}
