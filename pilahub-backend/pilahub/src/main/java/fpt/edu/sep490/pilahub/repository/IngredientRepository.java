package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    List<Ingredient> findByActiveTrue();

    Optional<Ingredient> findByIngredientIdAndActiveTrue(UUID ingredientId);

    Optional<Ingredient> findByName(String name);

    List<Ingredient> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByIngredientId(UUID ingredientId);
}
