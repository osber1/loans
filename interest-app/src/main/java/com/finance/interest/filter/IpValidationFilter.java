package com.finance.interest.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.stereotype.Component;

import com.finance.interest.interfaces.IpValidationRule;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IpValidationFilter implements Filter {

    private final IpValidationRule ipValidationRule;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ipValidationRule.validate(request.getRemoteAddr());
        chain.doFilter(request, response);
    }
}
