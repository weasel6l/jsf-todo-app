/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * javax.validation.Validator を CDI Bean として提供するプロデューサー
 *
 * Bean Validation のデフォルト実装を生成する
 */
@ApplicationScoped
public class ValidatorProducer {

    /**
     * Validator インスタンスを生成して CDI に提供する
     *
     * @return javax.validation.Validator インスタンス
     */
    @Produces
    @ApplicationScoped
    public Validator produceValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
}
