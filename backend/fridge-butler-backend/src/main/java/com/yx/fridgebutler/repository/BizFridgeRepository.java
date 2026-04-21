package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizFridge;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BizFridgeRepository extends JpaRepository<BizFridge, Long> {

    List<BizFridge> findByOwnerIdAndIsDeletedFalse(Long ownerId, Sort sort);

    Optional<BizFridge> findByIdAndOwnerIdAndIsDeletedFalse(Long id, Long ownerId);

    boolean existsByFridgeNameAndOwnerIdAndIsDeletedFalse(String fridgeName, Long ownerId);

    @Modifying
    @Query("UPDATE BizFridge f SET f.isDefault = false WHERE f.ownerId = ?1 AND f.isDeleted = false AND f.isDefault = true")
    void unsetDefaultByOwnerId(Long ownerId);
}
