package com.flowiee.dms.service.storage.impl;

import com.flowiee.dms.exception.AppException;
import com.flowiee.dms.entity.storage.DocShare;
import com.flowiee.dms.entity.storage.Document;
import com.flowiee.dms.entity.system.Account;
import com.flowiee.dms.exception.BadRequestException;
import com.flowiee.dms.exception.ResourceNotFoundException;
import com.flowiee.dms.model.ACTION;
import com.flowiee.dms.model.MODULE;
import com.flowiee.dms.model.dto.DocumentDTO;
import com.flowiee.dms.repository.storage.DocShareRepository;
import com.flowiee.dms.repository.storage.DocumentRepository;
import com.flowiee.dms.service.BaseService;
import com.flowiee.dms.service.storage.DocShareService;
import com.flowiee.dms.service.storage.DocumentInfoService;
import com.flowiee.dms.service.storage.FileStorageService;
import com.flowiee.dms.utils.AppConstants;
import com.flowiee.dms.utils.ChangeLog;
import com.flowiee.dms.utils.CommonUtils;
import com.flowiee.dms.utils.constants.DocRight;
import com.flowiee.dms.utils.constants.ErrorCode;
import com.flowiee.dms.utils.constants.MasterObject;
import com.flowiee.dms.utils.constants.MessageCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DocumentInfoServiceImpl extends BaseService implements DocumentInfoService {
    EntityManager      entityManager;
    DocShareService    docShareService;
    FileStorageService fileStorageService;
    DocShareRepository docShareRepository;
    DocumentRepository documentRepository;

    @Override
    public Page<DocumentDTO> findDocuments(Integer pageSize, Integer pageNum, Integer parentId, List<Integer> listId, String isFolder, String pTxtSearch) {
        Pageable pageable = Pageable.unpaged();
        if (pageSize >= 0 && pageNum >= 0) {
            pageable = PageRequest.of(pageNum, pageSize, Sort.by("isFolder", "createdAt").descending());
        }
        Account currentAccount = CommonUtils.getUserPrincipal();
        boolean isAdmin = AppConstants.ADMINISTRATOR.equals(currentAccount.getUsername());
        Page<Document> documents = documentRepository.findAll(pTxtSearch, parentId, currentAccount.getId(), isAdmin, CommonUtils.getUserPrincipal().getId(), null, isFolder, listId, pageable);
        List<DocumentDTO> documentDTOs = DocumentDTO.fromDocuments(documents.getContent());
        //Check the currently logged in account has update (U), delete (D), move (M) or share (S) rights?
        for (DocumentDTO d : documentDTOs) {
            List<DocShare> sharesOfDoc = docShareRepository.findByDocAndAccount(d.getId(), CommonUtils.getUserPrincipal().getId(), null);
            for (DocShare ds : sharesOfDoc) {
                if (DocRight.UPDATE.getValue().equals(ds.getRole())) d.setThisAccCanUpdate(true);
                if (DocRight.DELETE.getValue().equals(ds.getRole())) d.setThisAccCanDelete(true);
                if (DocRight.MOVE.getValue().equals(ds.getRole())) d.setThisAccCanMove(true);
                if (DocRight.SHARE.getValue().equals(ds.getRole())) d.setThisAccCanShare(true);
            }
            if (AppConstants.ADMINISTRATOR.equals(CommonUtils.getUserPrincipal().getUsername())) {
                d.setThisAccCanUpdate(true);
                d.setThisAccCanDelete(true);
                d.setThisAccCanMove(true);
                d.setThisAccCanShare(true);
            }
        }
        return new PageImpl<>(documentDTOs, pageable, documents.getTotalElements());
    }

    @Override
    public List<DocumentDTO> findFoldersByParent(Integer parentId) {
        List<DocumentDTO> docDTOs = this.findDocuments(-1, -1, parentId, null, "Y", null).getContent();
        for (DocumentDTO docDTO : docDTOs) {
            boolean existsSubDocument = documentRepository.existsSubDocument(docDTO.getId(), docDTO.getIsFolder());
            docDTO.setHasSubFolder(existsSubDocument ? "Y" : "N");
        }
        return docDTOs;
    }

    @Override
    public Optional<DocumentDTO> findById(Integer id) {
        Optional<Document> document = documentRepository.findById(id);
        return document.map(DocumentDTO::fromDocument);
    }

    @Override
    public DocumentDTO update(DocumentDTO data, Integer documentId) {
        Optional<Document> document = documentRepository.findById(documentId);
        if (document.isEmpty()) {
            throw new ResourceNotFoundException("Document not found!", false);
        }
        if (!docShareService.isShared(documentId, DocRight.UPDATE.getValue())) {
            throw new BadRequestException(ErrorCode.FORBIDDEN_ERROR.getDescription());
        }
        Document documentBefore = ObjectUtils.clone(document.get());

        document.get().setName(data.getName());
        document.get().setDescription(data.getDescription());
        Document documentUpdated = documentRepository.save(document.get());

        ChangeLog changeLog = new ChangeLog(documentBefore, documentUpdated);
        systemLogService.writeLogUpdate(MODULE.STORAGE, ACTION.STG_DOC_UPDATE, MasterObject.Document, "Update document " + document.get().getName(), changeLog);
        logger.info("{}: Update document docId={}", DocumentInfoServiceImpl.class.getName(), documentId);

        return DocumentDTO.fromDocument(documentRepository.save(document.get()));
    }

    @Transactional
    @Override
    public String delete(Integer documentId) {
        Optional<DocumentDTO> document = this.findById(documentId);
        if (document.isEmpty()) {
            throw new ResourceNotFoundException("Document not found!", false);
        }
        if (!docShareService.isShared(documentId, DocRight.DELETE.getValue())) {
            throw new BadRequestException(ErrorCode.FORBIDDEN_ERROR.getDescription());
        }
        docShareService.deleteByDocument(documentId);
        documentRepository.deleteById(documentId);
        systemLogService.writeLogDelete(MODULE.STORAGE, ACTION.STG_DOC_DELETE, MasterObject.Document, "Xóa tài liệu", document.get().getName());
        logger.info("{}: Delete document docId={}", DocumentInfoServiceImpl.class.getName(), documentId);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }

    @Override
    public List<Document> findByDoctype(Integer docTypeId) {
        return documentRepository.findAll(null, null, null, true, null, null, null, null, Pageable.unpaged()).getContent();
    }

    @Override
    public DocumentDTO save(DocumentDTO documentDTO) {
        try {
            Document document = Document.fromDocumentDTO(documentDTO);
            document.setAsName(CommonUtils.generateAliasName(document.getName()));
            if (ObjectUtils.isEmpty(document.getParentId())) {
                document.setParentId(0);
            }
            Document documentSaved = documentRepository.save(document);
            if ("N".equals(document.getIsFolder()) && documentDTO.getFileUpload() != null) {
                fileStorageService.saveFileOfDocument(documentDTO.getFileUpload(), documentSaved.getId());
            }
            List<DocShare> roleSharesOfDocument = docShareRepository.findByDocument(documentSaved.getParentId());
            for (DocShare docShare : roleSharesOfDocument) {
                DocShare roleNew = new DocShare();
                roleNew.setDocument(new Document(documentSaved.getId()));
                roleNew.setAccount(new Account(docShare.getAccount().getId()));
                roleNew.setRole(docShare.getRole());
                docShareService.save(roleNew);
            }
            //docShareService.save();
            systemLogService.writeLogCreate(MODULE.STORAGE, ACTION.STG_DOC_CREATE, MasterObject.Document, "Thêm mới tài liệu", documentSaved.getName());
            logger.info("{}: Thêm mới tài liệu {}", DocumentInfoServiceImpl.class.getName(), DocumentDTO.fromDocument(documentSaved));
            return DocumentDTO.fromDocument(documentSaved);
        } catch (RuntimeException | IOException ex) {
            throw new AppException(String.format(MessageCode.CREATE_SUCCESS.getDescription(), "document"), ex);
        }
    }

    @Override
    public List<DocumentDTO> findHierarchyOfDocument(Integer documentId, Integer parentId) {
        List<DocumentDTO> hierarchy = new ArrayList<>();
        String strSQL = "WITH DocumentHierarchy(ID, NAME, AS_NAME, PARENT_ID, H_LEVEL) AS ( " +
                        "    SELECT ID, NAME, AS_NAME, PARENT_ID, 1 " +
                        "    FROM DOCUMENT " +
                        "    WHERE id = ? " +
                        "    UNION ALL " +
                        "    SELECT d.ID, d.NAME, d.AS_NAME ,d.PARENT_ID, dh.H_LEVEL + 1 " +
                        "    FROM DOCUMENT d " +
                        "    INNER JOIN DocumentHierarchy dh ON dh.PARENT_ID = d.id " +
                        "), " +
                        "DocumentToFindParent(ID, NAME, AS_NAME, PARENT_ID, H_LEVEL) AS ( " +
                        "    SELECT ID, NAME, AS_NAME, PARENT_ID, NULL AS H_LEVEL " +
                        "    FROM DOCUMENT " +
                        "    WHERE ID = ? " +
                        ") " +
                        "SELECT ID, NAME, CONCAT(CONCAT(AS_NAME, '-'), ID) AS AS_NAME, PARENT_ID, H_LEVEL " +
                        "FROM DocumentHierarchy " +
                        "UNION ALL " +
                        "SELECT ID, NAME, CONCAT(CONCAT(AS_NAME, '-'), ID) AS AS_NAME, PARENT_ID, H_LEVEL " +
                        "FROM DocumentToFindParent " +
                        "START WITH ID = ? " +
                        "CONNECT BY PRIOR PARENT_ID = ID " +
                        "ORDER BY H_LEVEL DESC";
        logger.info("Load hierarchy of document (breadcrumb)");
        Query query = entityManager.createNativeQuery(strSQL);
        query.setParameter(1, documentId);
        query.setParameter(2, documentId);
        query.setParameter(3, parentId);
        @SuppressWarnings("unchecked")
        List<Object[]> list = query.getResultList();
        DocumentDTO rootHierarchy = new DocumentDTO();
        rootHierarchy.setId(null);
        rootHierarchy.setName("Home");
        rootHierarchy.setAsName("");
        hierarchy.add(rootHierarchy);
        for (Object[] doc : list) {
            DocumentDTO docDTO = new DocumentDTO();
            docDTO.setId(Integer.parseInt(String.valueOf(doc[0])));
            docDTO.setName(String.valueOf(doc[1]));
            docDTO.setAsName(String.valueOf(doc[2]));
            docDTO.setParentId(Integer.parseInt(String.valueOf(doc[3])));
            hierarchy.add(docDTO);
        }
        return hierarchy;
    }

    @Override
    public List<DocumentDTO> findSharedDocFromOthers(Integer accountId) {
        return DocumentDTO.fromDocuments(documentRepository.findWasSharedDoc(accountId));
    }

    @Override
    public List<DocumentDTO> findVersions(Integer documentId) {
        return null;
    }
}