/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.cart;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Negative;
import org.junit.jupiter.api.DisplayName;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AddToCartRequest バリデーションのプロパティベーステスト
 *
 * quantity および productId の境界値をプロパティで網羅する
 */
@DisplayName("AddToCartRequest プロパティテスト")
class AddToCartRequestProperties {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 任意の正整数（1 以上）は quantity バリデーションを通過すること
     *
     * @param quantity ランダムな 1 以上の整数
     */
    @Property(tries = 200)
    void positiveQuantityAlwaysPasses(
            @ForAll @IntRange(min = 1, max = Integer.MAX_VALUE) int quantity) {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(quantity);

        // When
        Set<ConstraintViolation<AddToCartRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty(),
                "quantity=" + quantity + " は 1 以上なのでバリデーションを通過すること");
    }

    /**
     * quantity=0 は常にバリデーションが失敗すること
     */
    @Property(tries = 1)
    void zeroQuantityAlwaysFails(@ForAll @IntRange(min = 0, max = 0) int quantity) {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(quantity);

        // When
        Set<ConstraintViolation<AddToCartRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "quantity=0 はバリデーションエラーになること");
    }

    /**
     * 任意の負数は quantity バリデーションが失敗すること
     *
     * @param quantity ランダムな負の整数
     */
    @Property(tries = 200)
    void negativeQuantityAlwaysFails(@ForAll @Negative int quantity) {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(quantity);

        // When
        Set<ConstraintViolation<AddToCartRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(),
                "quantity=" + quantity + " は負数なのでバリデーションエラーになること");
    }

    /**
     * productId が null の場合はバリデーションが失敗すること
     */
    @Property(tries = 1)
    void nullProductIdAlwaysFails(@ForAll @IntRange(min = 1, max = 1) int dummy) {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(null);
        request.setQuantity(1);

        // When
        Set<ConstraintViolation<AddToCartRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "productId=null はバリデーションエラーになること");
    }

    /**
     * quantity が null の場合はバリデーションが失敗すること
     */
    @Property(tries = 1)
    void nullQuantityAlwaysFails(@ForAll @IntRange(min = 1, max = 1) int dummy) {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(null);

        // When
        Set<ConstraintViolation<AddToCartRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "quantity=null はバリデーションエラーになること");
    }
}
