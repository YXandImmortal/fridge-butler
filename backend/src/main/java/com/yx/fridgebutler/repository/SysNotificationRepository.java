package com.yx.fridgebutler.repository;

import com.yx.fridgebutler.entity.SysNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 消息通知数据访问层。
 * <p>提供消息通知实体的增删改查操作，以及按用户、类型、状态等条件查询的自定义方法。</p>
 */
@Repository
public interface SysNotificationRepository extends JpaRepository<SysNotification, Long> {

    /**
     * 根据用户ID查询未删除的消息列表，支持按类型筛选。
     *
     * @param userId   用户ID
     * @param type     消息类型，为空或空字符串时不筛选类型
     * @param pageable 分页参数
     * @return 消息通知列表，按创建时间降序排列
     */
    @Query("""
            SELECT n FROM SysNotification n
            WHERE n.userId = :userId
              AND n.isDeleted = 0
              AND (:type IS NULL OR :type = '' OR n.type = :type)
              AND (:status IS NULL OR n.status = :status)
            ORDER BY n.createTime DESC
            """)
    List<SysNotification> findByUserIdAndTypeAndStatus(@Param("userId") Long userId, @Param("type") String type, @Param("status") Integer status, Pageable pageable);

    /**
     * 统计指定用户的未读消息数量。
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @Query("SELECT COUNT(n) FROM SysNotification n WHERE n.userId = :userId AND n.status = 0 AND n.isDeleted = 0")
    long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 统计指定用户指定类型的未读消息数量。
     *
     * @param userId 用户ID
     * @param type   消息类型
     * @return 未读消息数量
     */
    @Query("SELECT COUNT(n) FROM SysNotification n WHERE n.userId = :userId AND n.type = :type AND n.status = 0 AND n.isDeleted = 0")
    long countUnreadByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    /**
     * 判断指定冰箱是否存在指定类型的未读消息。
     * <p>用于容量预警去重，避免同一冰箱重复生成容量预警。</p>
     *
     * @param fridgeId 冰箱ID
     * @param type     消息类型
     * @return true 表示存在未读消息
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM SysNotification n WHERE n.fridgeId = :fridgeId AND n.type = :type AND n.status = 0 AND n.isDeleted = 0")
    boolean existsUnreadByFridgeIdAndType(@Param("fridgeId") Long fridgeId, @Param("type") String type);

    /**
     * 判断指定用户指定物品是否存在指定类型的未读消息。
     * <p>用于临期提醒去重，避免同一物品重复生成同类型提醒。</p>
     *
     * @param userId 用户ID
     * @param itemId 物品ID
     * @param type   消息类型
     * @return true 表示存在未读消息
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM SysNotification n WHERE n.userId = :userId AND n.itemId = :itemId AND n.type = :type AND n.status = 0 AND n.isDeleted = 0")
    boolean existsUnreadByUserIdAndItemIdAndType(@Param("userId") Long userId, @Param("itemId") Long itemId, @Param("type") String type);

    /**
     * 根据消息ID和用户ID查询未删除的消息。
     *
     * @param id     消息ID
     * @param userId 用户ID
     * @return 消息通知Optional对象
     */
    @Query("SELECT n FROM SysNotification n WHERE n.id = :id AND n.userId = :userId AND n.isDeleted = 0")
    Optional<SysNotification> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 将指定用户的所有未读消息标记为已读。
     *
     * @param userId 用户ID
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE SysNotification n SET n.status = 1 WHERE n.userId = :userId AND n.status = 0 AND n.isDeleted = 0")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    /**
     * 软删除指定用户的消息通知。
     *
     * @param id     消息ID
     * @param userId 用户ID
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE SysNotification n SET n.isDeleted = 1 WHERE n.id = :id AND n.userId = :userId AND n.isDeleted = 0")
    int softDeleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 将指定冰箱的容量预警消息标记为已读。
     * <p>用于冰箱容量利用率恢复正常后，自动清除对应的容量预警。</p>
     *
     * @param fridgeId 冰箱ID
     * @param type     消息类型（容量预警）
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE SysNotification n SET n.status = 1 WHERE n.fridgeId = :fridgeId AND n.type = :type AND n.status = 0 AND n.isDeleted = 0")
    int markCapacityWarningAsReadByFridgeId(@Param("fridgeId") Long fridgeId, @Param("type") String type);

    /**
     * 判断指定时间内是否存在指定类型和标题的通知。
     * <p>用于重要广播幂等校验，防止短时间内重复发送相同标题的通知。</p>
     *
     * @param type     消息类型
     * @param title    消息标题
     * @param since    时间阈值
     * @return true 表示存在
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM SysNotification n WHERE n.type = :type AND n.title = :title AND n.createTime >= :since")
    boolean existsByTypeAndTitleAndCreateTimeGreaterThanEqual(@Param("type") String type, @Param("title") String title, @Param("since") Instant since);

    /**
     * 查询指定用户指定类型的最新未读消息。
     * <p>按创建时间降序排列，用于获取最新的单条重要通知。</p>
     *
     * @param userId 用户ID
     * @param type   消息类型
     * @param pageable 分页参数（通常取第一条）
     * @return 消息通知列表
     */
    @Query("""
            SELECT n FROM SysNotification n
            WHERE n.userId = :userId
              AND n.type = :type
              AND n.status = 0
              AND n.isDeleted = 0
            ORDER BY n.createTime DESC
            """)
    List<SysNotification> findUnreadByUserIdAndType(@Param("userId") Long userId, @Param("type") String type, Pageable pageable);

    /**
     * 判断指定用户在指定时间之后是否存在指定类型的通知。
     * <p>用于绑定邮箱提醒的当天去重，避免同一天内重复发送。</p>
     *
     * @param userId 用户ID
     * @param type   消息类型
     * @param since  时间阈值
     * @return true 表示存在
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM SysNotification n WHERE n.userId = :userId AND n.type = :type AND n.createTime >= :since")
    boolean existsByUserIdAndTypeAndCreateTimeGreaterThanEqual(@Param("userId") Long userId, @Param("type") String type, @Param("since") Instant since);

    /**
     * 将指定用户指定类型的未读消息标记为已读。
     * <p>用于用户绑定邮箱后，自动清除对应的绑定邮箱提醒。</p>
     *
     * @param userId 用户ID
     * @param type   消息类型
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE SysNotification n SET n.status = 1, n.readTime = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.type = :type AND n.status = 0 AND n.isDeleted = 0")
    int markAsReadByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    /**
     * 将指定ID列表的未读消息标记为已读。
     * <p>用于获取最新重要通知时，自动将同类型的旧未读通知一并标为已读，避免连续弹窗。</p>
     *
     * @param ids 消息ID列表
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE SysNotification n SET n.status = 1, n.readTime = CURRENT_TIMESTAMP WHERE n.id IN :ids AND n.status = 0 AND n.isDeleted = 0")
    int markAsReadByIds(@Param("ids") List<Long> ids);
}
