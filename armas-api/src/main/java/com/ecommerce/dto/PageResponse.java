package com.ecommerce.dto;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> items, int page, int pageSize, long totalItems) {

    public static <S, T> PageResponse<T> of(
            List<S> items, int page, int pageSize, long totalItems, Function<S, T> mapper) {
        return new PageResponse<>(items.stream().map(mapper).toList(), page, pageSize, totalItems);
    }
}
