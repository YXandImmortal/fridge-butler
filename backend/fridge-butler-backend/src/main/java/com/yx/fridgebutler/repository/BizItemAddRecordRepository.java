package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.BizItemAddRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 物品添加记录数据访问层。
 * <p>提供物品添加记录实体的增删改查操作，继承 JpaRepository 使用基础 CRUD 方法。</p>
 */
@Repository
public interface BizItemAddRecordRepository extends JpaRepository<BizItemAddRecord, Long> {
}
