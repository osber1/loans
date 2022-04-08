package io.osvaldas.backoffice.infra.filter;

import static java.util.Optional.of;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import io.osvaldas.backoffice.domain.loans.rules.IpValidationRule;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class IpValidationFilter implements Filter {

    private final IpValidationRule ipValidationRule;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        of(httpServletRequest)
            .filter(r -> r.getRequestURI().contains("/api/v1/client"))
            .filter(r -> r.getRequestURI().contains("/loan"))
            .filter(r -> r.getMethod().equals("POST"))
            .ifPresent(r -> ipValidationRule.validate(request.getRemoteAddr()));
        chain.doFilter(request, response);
    }
}
