package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PackageRepository extends JpaRepository<Package, UUID> {

    /**
     * Find all active packages
     * @return list of active packages
     */
    List<Package> findByIsActiveTrue();

    /**
     * Check if package with name exists
     * @param packageName the package name
     * @return true if exists
     */
    boolean existsByPackageName(String packageName);
}
