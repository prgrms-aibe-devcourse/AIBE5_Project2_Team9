package com.pickkasso.pickkasso.global.config;

import org.springframework.boot.web.error.ErrorPage;
import org.springframework.boot.web.error.ErrorPageRegistrar;
import org.springframework.boot.web.error.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ErrorConfig implements ErrorPageRegistrar {

    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        ErrorPage errorPage403 = new ErrorPage(HttpStatus.FORBIDDEN, "/403");
        ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/404");
        ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500");
        ErrorPage errorPage400 = new ErrorPage(HttpStatus.BAD_REQUEST, "/400");
        ErrorPage errorPageGlobal = new ErrorPage("/error-default");

        registry.addErrorPages(errorPage403, errorPage404, errorPage500, errorPage400, errorPageGlobal);
    }
}