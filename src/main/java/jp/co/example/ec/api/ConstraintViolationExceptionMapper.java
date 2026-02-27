/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Bean Validation で発生した ConstraintViolationException を
 * HTTP 400 Bad Request に変換する例外マッパー
 */
@Provider
public class ConstraintViolationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    /**
     * ConstraintViolationException を HTTP 400 に変換する
     *
     * @param exception 発生した制約違反例外
     * @return HTTP 400 レスポンス
     */
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
