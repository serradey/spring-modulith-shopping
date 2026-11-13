package com.example.shopping.cart.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit test for the item-popularity delta accumulation from item-popularity.feature. */
class ItemPopularityTest {

    @Test
    void accumulatesNetCountAcrossEvents() {
        ItemPopularity popularity = new ItemPopularity("socks", 0L);

        popularity.changeBy(3); // ItemAdded(3)
        popularity.changeBy(5); // ItemAdded(5)
        popularity.changeBy(2 - 5); // ItemQuantityAdjusted(5 -> 2)
        popularity.changeBy(-3); // ItemRemoved(3)

        assertThat(popularity.count()).isEqualTo(2);
    }

    @Test
    void startsFromZero() {
        ItemPopularity popularity = new ItemPopularity("hat", 0L);
        assertThat(popularity.count()).isZero();
    }
}
