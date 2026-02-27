/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ConstraintViolationExceptionMapper の単体テスト
 *
 * Bean Validation 例外が HTTP 400 Bad Request に変換されることを検証する
 */
@DisplayName("ConstraintViolationExceptionMapper")
class ConstraintViolationExceptionMapperTest {

    /**
     * 前提条件: ConstraintViolationException が発生している
     * 期待する事後条件: HTTP 400 Bad Request が返ること
     */
    @Test
    @DisplayName("toResponse - ConstraintViolationException を HTTP 400 に変換できること")
    void toResponseReturns400() {
        // Given
        ConstraintViolationExceptionMapper mapper = new ConstraintViolationExceptionMapper();
        ConstraintViolationException ex = new ConstraintViolationException(Collections.emptySet());

        // When
        Response response = mapper.toResponse(ex);

        // Then
        assertEquals(400, response.getStatus(), "HTTP 400 が返ること");
    }
}
