package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.SysImportantNotice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 重要通知模板数据访问层。
 * <p>提供重要通知模板实体的增删改查操作。</p>
 */
@Repository
public interface SysImportantNoticeRepository extends JpaRepository<SysImportantNotice, Long> {

    /**
     * 查询未删除的重要通知模板列表，按创建时间降序排列。
     *
     * @param pageable 分页参数
     * @return 重要通知模板列表
     */
    @Query("SELECT n FROM SysImportantNotice n WHERE n.isDeleted = 0 ORDER BY n.createTime DESC")
    List<SysImportantNotice> findAllActiveOrderByCreateTimeDesc(Pageable pageable);

    /**
     * 判断指定时间内是否存在相同标题的重要通知模板。
     * <p>用于幂等校验，防止短时间内重复发布相同标题的通知。</p>
     *
     * @param title 通知标题
     * @param since 时间阈值
     * @return true 表示存在
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM SysImportantNotice n WHERE n.title = :title AND n.createTime >= :since AND n.isDeleted = 0")
    boolean existsByTitleAndCreateTimeGreaterThanEqual(@Param("title") String title, @Param("since") java.time.Instant since);

    /**
     * 根据ID查询未删除的重要通知模板。
     *
     * @param id 模板ID
     * @param isDeleted 删除标记
     * @return 重要通知模板Optional对象
     */
    Optional<SysImportantNotice> findByIdAndIsDeleted(@Param("id") Long id, @Param("isDeleted") Byte isDeleted);

    /**
     * 判断指定模板ID在最近指定时间内是否已广播过。
     * <p>用于按ID广播的幂等校验，防止短时间内重复广播同一模板。</p>
     *
     * @param id 模板ID
     * @param since 时间阈值
     * @return true 表示在指定时间内已广播过
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM SysImportantNotice n WHERE n.id = :id AND n.broadcastTime >= :since AND n.isDeleted = 0")
    boolean existsBroadcastByIdAndTime(@Param("id") Long id, @Param("since") Instant since);
}
