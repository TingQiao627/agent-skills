package com.example.org.repository;

import com.example.org.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmployeeNo(String employeeNo);
    
    Optional<Employee> findByPhone(String phone);
    
    List<Employee> findByDeptIdAndStatus(Long deptId, Integer status);
    
    List<Employee> findByDeptId(Long deptId);
    
    @Query("SELECT e FROM Employee e WHERE e.status = 1 ORDER BY e.name")
    List<Employee> findAllActive();
    
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.employeeNo = :employeeNo AND e.id != :excludeId")
    boolean existsByEmployeeNoAndIdNot(@Param("employeeNo") String employeeNo, @Param("excludeId") Long excludeId);
    
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.phone = :phone AND e.id != :excludeId")
    boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("excludeId") Long excludeId);
    
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.employeeNo = :employeeNo")
    boolean existsByEmployeeNo(@Param("employeeNo") String employeeNo);
    
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.phone = :phone")
    boolean existsByPhone(@Param("phone") String phone);
}