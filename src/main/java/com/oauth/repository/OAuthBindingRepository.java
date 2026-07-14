package com.oauth.repository;

import com.oauth.entity.OAuthBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OAuth绑定数据访问层
 */
@Repository
public interface OAuthBindingRepository extends JpaRepository<OAuthBinding, Long> {

    /**
     * 根据用户ID和提供商查询绑定关系
     *
     * @param userId   用户ID
     * @param provider 提供商
     * @return 绑定关系
     */
    Optional<OAuthBinding> findByUserIdAndProvider(Long userId, String provider);

    /**
     * 根据提供商和第三方用户ID查询绑定关系
     *
     * @param provider       提供商
     * @param providerUserId 第三方用户ID
     * @return 绑定关系
     */
    Optional<OAuthBinding> findByProviderAndProviderUserId(String provider, String providerUserId);

    /**
     * 查询用户的所有有效绑定
     *
     * @param userId 用户ID
     * @return 绑定列表
     */
    @Query("SELECT ob FROM OAuthBinding ob WHERE ob.userId = :userId AND ob.status = 1")
    List<OAuthBinding> findActiveBindingsByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已绑定指定提供商
     *
     * @param userId   用户ID
     * @param provider 提供商
     * @return 是否已绑定
     */
    @Query("SELECT CASE WHEN COUNT(ob) > 0 THEN true ELSE false END FROM OAuthBinding ob " +
           "WHERE ob.userId = :userId AND ob.provider = :provider AND ob.status = 1")
    boolean existsByUserIdAndProviderAndStatus(@Param("userId") Long userId, @Param("provider") String provider);

    /**
     * 解绑账号(软删除)
     *
     * @param userId   用户ID
     * @param provider 提供商
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE OAuthBinding ob SET ob.status = 0, ob.updateTime = CURRENT_TIMESTAMP " +
           "WHERE ob.userId = :userId AND ob.provider = :provider AND ob.status = 1")
    int unbindByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") String provider);

    /**
     * 更新最后登录时间
     *
     * @param userId   用户ID
     * @param provider 提供商
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE OAuthBinding ob SET ob.lastLoginTime = CURRENT_TIMESTAMP, ob.updateTime = CURRENT_TIMESTAMP " +
           "WHERE ob.userId = :userId AND ob.provider = :provider")
    int updateLastLoginTime(@Param("userId") Long userId, @Param("provider") String provider);

    /**
     * 更新访问令牌
     *
     * @param userId       用户ID
     * @param provider     提供商
     * @param accessToken  新的访问令牌
     * @param refreshToken 新的刷新令牌
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE OAuthBinding ob SET ob.accessToken = :accessToken, " +
           "ob.refreshToken = :refreshToken, ob.updateTime = CURRENT_TIMESTAMP " +
           "WHERE ob.userId = :userId AND ob.provider = :provider")
    int updateToken(@Param("userId") Long userId, @Param("provider") String provider,
                    @Param("accessToken") String accessToken, @Param("refreshToken") String refreshToken);
}