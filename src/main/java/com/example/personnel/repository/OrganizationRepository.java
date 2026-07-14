package com.example.personnel.repository;

import com.example.personnel.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 组织架构数据访问层
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByCode(String code);

    List<Organization> findByParentIdIsNullAndDeletedFalse();

    List<Organization> findByParentIdAndDeletedFalse(Long parentId);

    @Query("SELECT o FROM Organization o WHERE o.deleted = false ORDER BY o.level, o.name")
    List<Organization> findAllActive();

    @Query("SELECT o FROM Organization o WHERE o.parentId = :parentId AND o.deleted = false")
    List<Organization> findChildrenByParentId(@Param("parentId") Long parentId);

    boolean existsByCodeAndDeletedFalse(String code);
}