package com.example.analytics;

import org.springframework.data.jpa.repository.JpaRepository;

interface EventCountRepository extends JpaRepository<EventCount, String> {}
