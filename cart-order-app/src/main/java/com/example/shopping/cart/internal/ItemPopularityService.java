package com.example.shopping.cart.internal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Query and maintenance API for the item-popularity read model (cart module internal). */
@Service
public class ItemPopularityService {

    private final ItemPopularityRepository repository;

    ItemPopularityService(ItemPopularityRepository repository) {
        this.repository = repository;
    }

    /** Current popularity of an item; an item that has never been seen has a count of 0. */
    @Transactional(readOnly = true)
    public long popularityOf(String itemId) {
        return repository.findById(itemId).map(ItemPopularity::count).orElse(0L);
    }

    @Transactional
    void changeBy(String itemId, long delta) {
        ItemPopularity popularity =
                repository.findById(itemId).orElseGet(() -> new ItemPopularity(itemId, 0L));
        popularity.changeBy(delta);
        repository.save(popularity);
    }
}
