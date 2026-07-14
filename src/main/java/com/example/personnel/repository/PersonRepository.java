package com.example.personnel.repository;

import com.example.personnel.entity.Person;
import com.example.personnel.entity.PersonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 人员信息数据访问层
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByEmployeeNoAndDeletedFalse(String employeeNo);

    Optional<Person> findByIdCardAndDeletedFalse(String idCard);

    Page<Person> findByOrganizationIdAndDeletedFalse(Long organizationId, Pageable pageable);

    Page<Person> findByStatusAndDeletedFalse(PersonStatus status, Pageable pageable);

    @Query("SELECT p FROM Person p WHERE p.organizationId = :orgId AND p.status = :status AND p.deleted = false")
    Page<Person> findByOrganizationAndStatus(
            @Param("orgId") Long organizationId,
            @Param("status") PersonStatus status,
            Pageable pageable);

    @Query("SELECT p FROM Person p WHERE p.deleted = false")
    Page<Person> findAllActive(Pageable pageable);

    List<Person> findByOrganizationIdAndDeletedFalse(Long organizationId);

    boolean existsByEmployeeNoAndDeletedFalse(String employeeNo);

    boolean existsByIdCardAndDeletedFalse(String idCard);
}