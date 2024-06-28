package com.flowiee.dms.service.storage.impl;

import com.flowiee.dms.entity.storage.Document;
import com.flowiee.dms.entity.storage.FileStorage;
import com.flowiee.dms.exception.BadRequestException;
import com.flowiee.dms.model.MODULE;
import com.flowiee.dms.model.dto.DocumentDTO;
import com.flowiee.dms.repository.storage.FileStorageRepository;
import com.flowiee.dms.service.BaseService;
import com.flowiee.dms.service.storage.DocumentInfoService;
import com.flowiee.dms.service.storage.FileStorageService;
import com.flowiee.dms.utils.CommonUtils;
import com.flowiee.dms.utils.constants.ErrorCode;
import com.flowiee.dms.utils.constants.MessageCode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileStorageServiceImpl extends BaseService implements FileStorageService {
    DocumentInfoService   documentInfoService;
    FileStorageRepository fileRepository;

    public FileStorageServiceImpl(@Lazy DocumentInfoService documentInfoService, FileStorageRepository fileRepository) {
        this.documentInfoService = documentInfoService;
        this.fileRepository = fileRepository;
    }

    @Override
    public Optional<FileStorage> findById(Integer fileId) {
        return fileRepository.findById(fileId);
    }

    @Override
    public FileStorage save(FileStorage entity) {
        return fileRepository.save(entity);
    }

    @Override
    public FileStorage update(FileStorage entity, Integer entityId) {
        entity.setId(entityId);
        return fileRepository.save(entity);
    }

    @Override
    public Optional<FileStorage> findFileIsActiveOfDocument(Integer documentId) {
        List<FileStorage> listFiles = fileRepository.findFileOfDocument(documentId, true);
        if (listFiles != null && !listFiles.isEmpty()) {
            return Optional.of(listFiles.get(0));
        }
        return Optional.empty();
    }

    @Override
    public List<FileStorage> findFilesOfDocument(Integer documentId) {
        return fileRepository.findFileOfDocument(documentId, null);
    }

    @Override
    public FileStorage saveFileOfDocument(MultipartFile fileUpload, Integer documentId) throws IOException {
        FileStorage fileInfo = new FileStorage(fileUpload, MODULE.STORAGE.name());
        fileInfo.setCustomizeName(fileUpload.getOriginalFilename());
        fileInfo.setDocument(new Document(documentId));
        fileInfo.setActive(true);
        FileStorage fileSaved = this.save(fileInfo);

        Path path = Paths.get(CommonUtils.getPathDirectory(MODULE.STORAGE.name()) + "/" + fileSaved.getStorageName());
        fileUpload.transferTo(path);

        return fileSaved;
    }

    @Override
    public String saveFileOfImport(MultipartFile fileImport, FileStorage fileInfo) throws IOException {
        fileRepository.save(fileInfo);
        fileInfo.setStorageName("I_" + fileInfo.getStorageName());
        fileImport.transferTo(Paths.get(CommonUtils.getPathDirectory(fileInfo.getModule()) + "/" + fileInfo.getStorageName()));
        return "OK";
    }

    @Override
    public String changFileOfDocument(MultipartFile fileUpload, Integer documentId) throws IOException {
        Optional<DocumentDTO> document = documentInfoService.findById(documentId);
        if (document.isEmpty()) {
            throw new BadRequestException();
        }
        //Set inactive cho các version cũ
        List<FileStorage> listDocFile = document.get().getListDocFile();
        for (FileStorage docFile : listDocFile) {
            docFile.setActive(false);
            this.update(docFile, docFile.getId());
        }
        //Save file mới vào hệ thống
        FileStorage fileInfo = new FileStorage(fileUpload, MODULE.STORAGE.name());
        fileInfo.setCustomizeName(fileUpload.getOriginalFilename());
        fileInfo.setDocument(new Document(documentId));
        fileInfo.setActive(true);
        FileStorage fileSaved = this.save(fileInfo);

        Path path = Paths.get(CommonUtils.getPathDirectory(MODULE.STORAGE.name()) + "/" + fileSaved.getStorageName());
        fileUpload.transferTo(path);

        return "OK";
    }

    @Override
    public String delete(Integer fileId) {
        FileStorage fileStorage = fileRepository.findById(fileId).orElse(null);
        fileRepository.deleteById(fileId);
        File file = new File(CommonUtils.rootPath + "/" + fileStorage.getDirectoryPath() + "/" + fileStorage.getStorageName());
        if (file.exists() && file.delete()) {
            return MessageCode.DELETE_SUCCESS.getDescription();
        }
        return String.format(ErrorCode.DELETE_ERROR.getDescription(), "file");
    }
}