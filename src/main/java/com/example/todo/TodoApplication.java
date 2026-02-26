/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS アプリケーションクラス
 */
@OpenAPIDefinition(
    info = @Info(
        title = "Todo API",
        version = "1.0.0",
        description = "JSF Todo アプリのマイグレーション API"
    )
)
@ApplicationScoped
@ApplicationPath("/api")
public class TodoApplication extends Application {
}
