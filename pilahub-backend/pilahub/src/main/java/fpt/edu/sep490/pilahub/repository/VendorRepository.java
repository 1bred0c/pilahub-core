package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    List<Vendor> findByVerifiedTrue();

    List<Vendor> findByBusinessNameContainingIgnoreCase(String businessName);

    List<Vendor> findByCity(String city);

    List<Vendor> findByCountry(String country);
}
