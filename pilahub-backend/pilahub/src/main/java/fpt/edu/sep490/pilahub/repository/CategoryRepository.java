package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByActiveTrue();

    List<Category> findByNameContainingIgnoreCase(String name);

    List<Category> findByParentCategory_CategoryId(UUID parentCategoryId);

    List<Category> findByParentCategoryIsNull();

    List<Category> findByActiveTrueAndParentCategoryIsNull();
}
