package com.flowiee.dms.repository;

import com.flowiee.dms.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer>{
    @Query("from Account a where a.username=:username")
    Account findByUsername(@Param("username") String username);
}