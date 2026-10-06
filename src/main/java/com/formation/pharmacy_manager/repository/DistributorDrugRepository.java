package com.formation.pharmacy_manager.repository;

import com.formation.pharmacy_manager.entities.DistributorDrug;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DistributorDrugRepository extends JpaRepository<DistributorDrug, Long> {
    
    boolean existsByDrug_DrugId(long drugId); 
    @Query("select distinct dis from DistributorDrug dis where dis.drug.drugName = :drugName and dis.distributor.userName = :userName")
    DistributorDrug getByUserNameAndDrugName(@Param("drugName") String drugName, @Param("userName") String userName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dd FROM DistributorDrug dd WHERE dd.distributor.userName = :userName AND dd.drug.drugName = :drugName")
    Optional<DistributorDrug> findByUserNameAndDrugNameForUpdate(
            @Param("userName") String userName,
            @Param("drugName") String drugName
    );
    
}