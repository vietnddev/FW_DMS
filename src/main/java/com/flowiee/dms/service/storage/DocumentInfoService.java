package com.flowiee.dms.service.storage;

import com.flowiee.dms.base.BaseService;
import com.flowiee.dms.entity.storage.Document;
import com.flowiee.dms.model.dto.DocumentDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DocumentInfoService extends BaseService<Document> {
    Page<DocumentDTO> findDocuments(Integer pageSize, Integer pageNum, Integer parentId);

    List<DocumentDTO> findFolderByParentId(Integer parentId);

    List<Document> findByDoctype(Integer docType);

    DocumentDTO save(DocumentDTO documentDTO);

    DocumentDTO update(DocumentDTO documentDTO, Integer documentId);

    List<DocumentDTO> findHierarchyOfDocument(Integer documentId, Integer parentId);

    List<DocumentDTO> generateFolderTree(Integer parentId);

    List<DocumentDTO> findSharedDocFromOthers(Integer accountId);

    List<DocumentDTO> findVersions(Integer documentId);
}