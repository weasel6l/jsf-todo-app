/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

/**
 * EC ストアアプリケーションの JAX-RS アプリケーション定義
 *
 * API 全体のメタ情報を定義する
 */
@OpenAPIDefinition(
    info = @Info(
        title = "EC Store API",
        version = "1.0.0",
        description = "JSF EC アプリのマイグレーション REST API"
    )
)
@ApplicationScoped
@ApplicationPath("/api")
public class EcApplication extends Application {
}
