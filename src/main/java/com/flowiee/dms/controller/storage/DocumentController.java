package com.flowiee.dms.controller.storage;

import com.flowiee.dms.base.BaseController;
import com.flowiee.dms.exception.AppException;
import com.flowiee.dms.model.ApiResponse;
import com.flowiee.dms.model.dto.DocumentDTO;
import com.flowiee.dms.service.storage.DocActionService;
import com.flowiee.dms.service.storage.DocumentInfoService;
import com.flowiee.dms.utils.constants.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("${app.api.prefix}/stg")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DocumentController extends BaseController {
    DocActionService    docActionService;
    DocumentInfoService documentInfoService;

    @Operation(summary = "Find all documents")
    @GetMapping("/doc/all")
    @PreAuthorize("@vldModuleStorage.readDoc(true)")
    public ApiResponse<List<DocumentDTO>> getAllDocuments(@RequestParam("pageSize") Integer pageSize,
                                                          @RequestParam("pageNum") Integer pageNum,
                                                          @RequestParam("parentId") Integer parentId,
                                                          @RequestParam(value = "txtSearch", required = false) String txtSearch) {
        try {
            Page<DocumentDTO> documents = documentInfoService.findDocuments(pageSize, pageNum - 1, parentId, null, null, txtSearch);
            return ApiResponse.ok(documents.getContent(), pageNum, pageSize, documents.getTotalPages(), documents.getTotalElements());
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.SEARCH_ERROR.getDescription(), "documents"), ex);
        }
    }

    @Operation(summary = "Create new document")
    @PostMapping("/doc/create")
    @PreAuthorize("@vldModuleStorage.insertDoc(true)")
    public ApiResponse<DocumentDTO> insertNewDoc(@RequestParam(value = "fileUpload", required = false) MultipartFile fileUpload,
                                                 @RequestParam(value = "docTypeId", required = false) Integer docTypeId,
                                                 @RequestParam(value = "name") String name,
                                                 @RequestParam(value = "description", required = false) String description,
                                                 @RequestParam(value = "isFolder") String isFolder,
                                                 @RequestParam(value = "parentId") Integer parentId) {
        try {
            DocumentDTO document = new DocumentDTO();
            document.setParentId(parentId);
            document.setName(name);
            document.setDescription(description);
            document.setIsFolder(isFolder);
            document.setDocTypeId(docTypeId);
            document.setFileUpload(fileUpload);
            return ApiResponse.ok(documentInfoService.save(document));
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.CREATE_ERROR.getDescription(), "document"), ex);
        }
    }

    @Operation(summary = "Find all folders")
    @GetMapping("/doc/folders")
    @PreAuthorize("@vldModuleStorage.readDoc(true)")
    public ApiResponse<List<DocumentDTO>> getAllFolders(@RequestParam(value = "parentId", required = false) Integer parentId) {
        try {
            return ApiResponse.ok(documentInfoService.findFoldersByParent(parentId));
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.SEARCH_ERROR.getDescription(), "folders"), ex);
        }
    }

    @Operation(summary = "Update document")
    @PutMapping("/doc/update/{id}")
    @PreAuthorize("@vldModuleStorage.updateDoc(true)")
    public ApiResponse<DocumentDTO> updateDoc(@PathVariable("id") Integer docId, @ModelAttribute DocumentDTO documentDTO) {
        return ApiResponse.ok(documentInfoService.update(documentDTO, docId));
    }

    @Operation(summary = "Delete document")
    @DeleteMapping("/doc/delete/{id}")
    @PreAuthorize("@vldModuleStorage.deleteDoc(true)")
    public ApiResponse<String> deleteDoc(@PathVariable("id") Integer docId) {
        return ApiResponse.ok(documentInfoService.delete(docId));
    }

    @Operation(summary = "Copy document")
    @PostMapping("/doc/copy/{id}")
    @PreAuthorize("@vldModuleStorage.copyDoc(true)")
    public ApiResponse<DocumentDTO> copyDoc(@PathVariable("id") Integer docId, @RequestParam("nameCopy") String nameCopy) {
        return ApiResponse.ok(docActionService.copyDoc(docId, null, nameCopy));
    }

    @Operation(summary = "Move document")
    @PutMapping("/doc/move/{id}")
    @PreAuthorize("@vldModuleStorage.moveDoc(true)")
    public ApiResponse<String> moveDoc(@PathVariable("id") Integer docId, @RequestParam("destinationId") Integer destinationId) {
        return ApiResponse.ok(docActionService.moveDoc(docId, destinationId));
    }

    @Operation(summary = "Download document")
    @GetMapping("/doc/download/{id}")
    @PreAuthorize("@vldModuleStorage.readDoc(true)")
    public ResponseEntity<InputStreamResource> downloadDoc(@PathVariable("id") Integer documentId) throws FileNotFoundException {
        return docActionService.downloadDoc(documentId);
    }
}