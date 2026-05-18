package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemChangeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 物品变更记录数据访问层。
 * <p>提供物品变更记录实体的增删改查操作，继承 JpaRepository 使用基础 CRUD 方法。</p>
 */
@Repository
public interface BizItemChangeRecordRepository extends JpaRepository<BizItemChangeRecord, Long> {
}
