package com.flowiee.dms.repository.storage;

import com.flowiee.dms.entity.storage.DocShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocShareRepository extends JpaRepository<DocShare, Integer> {
    @Query("from DocShare d " +
           "where 1=1 " +
           "and d.document.id=:documentId " +
           "and d.account.id=:accountId " +
           "and (:role is null or d.role = :role)")
    List<DocShare> findByDocAndAccount(@Param("documentId") Integer documentId, @Param("accountId") Integer accountId, @Param("role") String role);

    @Query("from DocShare d where d.document.id=:documentId")
    List<DocShare> findByDocument(@Param("documentId") Integer documentId);

    @Modifying
    @Query("delete DocShare d where d.account.id=:accountId")
    void deleteAllByAccount(@Param("accountId") Integer accountId);

    @Modifying
    @Query("delete DocShare d where d.document.id=:documentId")
    void deleteAllByDocument(@Param("documentId") Integer documentId);
}