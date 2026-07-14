package com.example.org.repository;

import com.example.org.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    List<Department> findByParentIdOrderBySortOrderAsc(Long parentId);
    
    List<Department> findByParentIdIsNullOrderBySortOrderAsc();
    
    @Query("SELECT d FROM Department d WHERE d.path LIKE :pathPrefix ORDER BY d.level ASC, d.sortOrder ASC")
    List<Department> findByPathStartingWith(@Param("pathPrefix") String pathPrefix);
    
    @Query("SELECT d FROM Department d WHERE d.status = 1 ORDER BY d.level ASC, d.sortOrder ASC")
    List<Department> findAllActive();
    
    boolean existsByNameAndParentId(String name, Long parentId);
}