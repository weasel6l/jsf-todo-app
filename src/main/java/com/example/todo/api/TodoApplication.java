/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

/**
 * JAX-RS アプリケーション定義クラス。
 * API のルートパスおよび OpenAPI メタ情報を定義する。
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
