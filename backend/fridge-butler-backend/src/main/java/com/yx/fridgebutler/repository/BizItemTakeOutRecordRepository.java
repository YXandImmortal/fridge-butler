package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemTakeOutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BizItemTakeOutRecordRepository extends JpaRepository<BizItemTakeOutRecord, Long> {
}
