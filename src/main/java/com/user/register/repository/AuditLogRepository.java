package com.user.register.repository;


import com.user.register.entity.AuditLog;
import com.user.register.entity.User; // ✅ correct
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    // Optional: Add custom queries if needed
    void deleteByUser(User user);

}
