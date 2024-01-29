package net.apple70cents.chattools.utils;

import java.util.Objects;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
    void accept(A var1, B var2, C var3, D var4);

    default QuadConsumer<A, B, C, D> andThen(QuadConsumer<? super A, ? super B, ? super C, ? super D> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d) -> {
            this.accept(a, b, c, d);
            after.accept(a, b, c, d);
        };
    }
}