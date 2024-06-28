package com.flowiee.dms.controller.storage;

import com.flowiee.dms.base.BaseController;
import com.flowiee.dms.entity.category.Category;
import com.flowiee.dms.entity.storage.FileStorage;
import com.flowiee.dms.exception.AppException;
import com.flowiee.dms.exception.ForbiddenException;
import com.flowiee.dms.exception.ResourceNotFoundException;
import com.flowiee.dms.model.DocMetaModel;
import com.flowiee.dms.model.dto.DocumentDTO;
import com.flowiee.dms.model.dto.FileDTO;
import com.flowiee.dms.service.category.CategoryService;
import com.flowiee.dms.service.storage.*;
import com.flowiee.dms.utils.*;
import com.flowiee.dms.utils.constants.CategoryType;
import com.flowiee.dms.utils.constants.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/stg")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DocumentControllerView extends BaseController {
    CategoryService     categoryService;
    DocShareService     docShareService;
    FileStorageService  fileStorageService;
    DocMetadataService  docMetadataService;
    DocumentInfoService documentInfoService;

    @GetMapping("/dashboard")
    @PreAuthorize("@vldModuleStorage.dashboard(true)")
    public ModelAndView showDashboardOfSTG() {
        ModelAndView modelAndView = new ModelAndView(PagesUtils.STG_DASHBOARD);
        //Loại tài liệu
        List<Category> listLoaiTaiLieu = categoryService.findSubCategory(CategoryType.DOCUMENT_TYPE.getName(), null, null, -1, -1).getContent();
        List<String> listTenOfDocType = new ArrayList<>();
        List<Integer> listSoLuongOfDocType = new ArrayList<>();
        for (Category docType : listLoaiTaiLieu) {
            listTenOfDocType.add(docType.getName());
            listSoLuongOfDocType.add(docType.getListDocument() != null ? docType.getListDocument().size() : 0);
        }
        modelAndView.addObject("reportOfDocType_listTen", listTenOfDocType);
        modelAndView.addObject("reportOfDocType_listSoLuong", listSoLuongOfDocType);
        return baseView(modelAndView);
    }

    @GetMapping("/doc")
    @PreAuthorize("@vldModuleStorage.readDoc(true)")
    public ModelAndView viewRootDocuments() {
        ModelAndView modelAndView = new ModelAndView(PagesUtils.STG_DOCUMENT);
        modelAndView.addObject("parentId", 0);
        modelAndView.addObject("folderTree", documentInfoService.findFoldersByParent(0));
        return baseView(modelAndView);
    }

    @GetMapping("/doc/{aliasPath}")
    @PreAuthorize("@vldModuleStorage.readDoc(true)")
    public ModelAndView viewSubDocuments(@PathVariable("aliasPath") String aliasPath) {
        String aliasName = aliasPath.substring(0, aliasPath.lastIndexOf("-"));
        int documentId = Integer.parseInt(aliasPath.substring(aliasPath.lastIndexOf("-") + 1));
        Optional<DocumentDTO> documentOptional = documentInfoService.findById(documentId);
        if (documentOptional.isEmpty() || !(aliasName + "-" + documentId).equals(documentOptional.get().getAsName() + "-" + documentOptional.get().getId())) {
            throw new ResourceNotFoundException("Document not found!", true);
        }
        if (!docShareService.isShared(documentId, null)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN_ERROR.getDescription());
        }
        DocumentDTO document = documentOptional.get();
        try {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("docBreadcrumb", documentInfoService.findHierarchyOfDocument(document.getId(), document.getParentId()));
            modelAndView.addObject("folderTree", documentInfoService.findFoldersByParent(0));
            modelAndView.addObject("documentParentName", document.getName());
            if (document.getIsFolder().equals("Y")) {
                modelAndView.setViewName(PagesUtils.STG_DOCUMENT);
                modelAndView.addObject("parentId", document.getId());
            }
            if (document.getIsFolder().equals("N")) {
                DocumentDTO docDTO = DocumentDTO.fromDocument(document);

                Optional<FileStorage> fileStorage = fileStorageService.findFileIsActiveOfDocument(document.getId());
                if (fileStorage.isPresent()) {
                    docDTO.setFile(FileDTO.fromFileStorage(fileStorage.get()));
                } else {
                    docDTO.setFile(new FileDTO());
                }

                modelAndView.setViewName(PagesUtils.STG_DOCUMENT_DETAIL);
                modelAndView.addObject("docDetail", docDTO);
                modelAndView.addObject("docMeta", docMetadataService.findMetadata(document.getId()));
                modelAndView.addObject("documentId", document.getId());
                //modelAndView.addObject("listFileOfDocument", fileStorageService.getFileOfDocument(documentId));
            }
            return baseView(modelAndView);
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.SEARCH_ERROR.getDescription(), "subDocs/ docDetail"), ex);
        }
    }

    @PostMapping("/doc/change-file/{id}")
    @PreAuthorize("@vldModuleStorage.updateDoc(true)")
    public ModelAndView changeFile(@RequestParam("file") MultipartFile file,
                                   @PathVariable("id") Integer documentId,
                                   HttpServletRequest request) throws IOException {
        if (documentId <= 0 || documentInfoService.findById(documentId).isEmpty()) {
            throw new ResourceNotFoundException("Document not found!", true);
        }
        fileStorageService.changFileOfDocument(file, documentId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping("/document/update/{id}")
    @PreAuthorize("@vldModuleStorage.updateDoc(true)")
    public ModelAndView update(@ModelAttribute("document") DocumentDTO document, @PathVariable("id") Integer documentId, HttpServletRequest request) {
        if (document == null || documentId <= 0 || documentInfoService.findById(documentId).isEmpty()) {
            throw new ResourceNotFoundException("Document not found!", true);
        }
        documentInfoService.update(document, documentId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @GetMapping("/doc/update-metadata/{id}")
    @PreAuthorize("@vldModuleStorage.updateDoc(true)")
    public ModelAndView updateMetadata(HttpServletRequest request,
                                       @PathVariable("id") Integer documentId,
                                       @RequestParam(value = "fieldId", required = false) Integer[] fieldIds,
                                       @RequestParam(value = "dataId", required = false) Integer[] dataIds,
                                       @RequestParam(value = "dataValue", required = false) String[] dataValues) {
        if (documentId <= 0 || documentInfoService.findById(documentId).isEmpty()) {
            throw new ResourceNotFoundException("Document not found!", true);
        }
        if (ObjectUtils.isNotEmpty(fieldIds) && ObjectUtils.isNotEmpty(dataIds) && ObjectUtils.isNotEmpty(dataValues)) {
            List<DocMetaModel> metaDTOs = new ArrayList<>();
            for (int i = 0; i <fieldIds.length; i++) {
                int fieldId = fieldIds[i];
                int dataId = dataIds[i];
                String dataValue = dataValues[i];
                metaDTOs.add(new DocMetaModel(fieldId, null, dataId, dataValue, null, null, documentId));
            }
            docMetadataService.updateMetadata(metaDTOs, documentId);
        }
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping("/document/delete/{id}")
    @PreAuthorize("@vldModuleStorage.deleteDoc(true)")
    public ModelAndView deleteDocument(@PathVariable("id") Integer documentId, HttpServletRequest request) {
        if (documentId <= 0 || documentInfoService.findById(documentId).isEmpty()) {
            throw new ResourceNotFoundException("Document not found!", true);
        }
        documentInfoService.delete(documentId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping("/document/move/{id}")
    @PreAuthorize("@vldModuleStorage.moveDoc(true)")
    public ModelAndView moveDocument(@PathVariable("id") Integer documentId, HttpServletRequest request) {
        if (documentId <= 0 || documentInfoService.findById(documentId).isEmpty()) {
            throw new ResourceNotFoundException("Document not found!", true);
        }
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping("/document/share/{id}")
    @PreAuthorize("@vldModuleStorage.shareDoc(true)")
    public ModelAndView share(@PathVariable("id") Integer documentId, HttpServletRequest request) {
        if (documentId <= 0 || documentInfoService.findById(documentId).isEmpty()) {
            throw new ResourceNotFoundException("Document not found!", true);
        }
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }
}