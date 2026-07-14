package com.example.org.repository;

import com.example.org.entity.TransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransferRecordRepository extends JpaRepository<TransferRecord, Long> {
    
    List<TransferRecord> findByEmployeeIdOrderByTransferDateDesc(Long employeeId);
    
    List<TransferRecord> findByFromDeptIdOrToDeptIdOrderByTransferDateDesc(Long fromDeptId, Long toDeptId);
}