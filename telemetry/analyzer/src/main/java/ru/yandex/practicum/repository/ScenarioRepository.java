package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.Scenario;

import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    Optional<Scenario> findByHubIdAndName(String hubId, String name);
}
