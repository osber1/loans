package com.finance.loans.infra.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.finance.loans.domain.loans.rules.IpValidationRule;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IpValidationFilter implements Filter {

    private final IpValidationRule ipValidationRule;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (getCurrentUrlFromRequest((HttpServletRequest) request).equals("/api/client/loans")) {
            ipValidationRule.validate(request.getRemoteAddr());
        }
        chain.doFilter(request, response);
    }

    private String getCurrentUrlFromRequest(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
