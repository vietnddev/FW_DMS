package com.flowiee.dms.service.storage.impl;

import com.flowiee.dms.exception.DataInUseException;
import com.flowiee.dms.entity.storage.DocField;
import com.flowiee.dms.exception.ResourceNotFoundException;
import com.flowiee.dms.model.ACTION;
import com.flowiee.dms.model.MODULE;
import com.flowiee.dms.repository.storage.DocFieldRepository;
import com.flowiee.dms.service.BaseService;
import com.flowiee.dms.service.storage.DocDataService;
import com.flowiee.dms.service.storage.DocFieldService;
import com.flowiee.dms.utils.ChangeLog;
import com.flowiee.dms.utils.constants.ErrorCode;
import com.flowiee.dms.utils.constants.MasterObject;
import com.flowiee.dms.utils.constants.MessageCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DocFieldServiceImpl extends BaseService implements DocFieldService {
    DocDataService     docDataService;
    DocFieldRepository docFieldRepository;

    @Override
    public List<DocField> findAll() {
        return docFieldRepository.findAll();
    }

    @Override
    public Optional<DocField> findById(Integer id) {
        return docFieldRepository.findById(id);
    }

    @Override
    public List<DocField> findByDocTypeId(Integer doctypeId) {
        return docFieldRepository.findByDoctype(doctypeId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DocField save(DocField docField) {
        DocField docFieldSaved = docFieldRepository.save(docField);
        systemLogService.writeLogCreate(MODULE.STORAGE, ACTION.STG_DOC_DOCTYPE_CONFIG, MasterObject.DocField, "Thêm mới DocField", docFieldSaved.getName());
        logger.info("{}: Thêm mới doc_field id={}", DocumentInfoServiceImpl.class.getName(), docField.getId());
        return docFieldSaved;
    }

    @Override
    public DocField update(DocField pDocField, Integer docFieldId) {
        Optional<DocField> docFieldOpt = this.findById(docFieldId);
        if (docFieldOpt.isEmpty()) {
            throw new ResourceNotFoundException("DocField not found!", true);
        }
        DocField docFieldBefore = ObjectUtils.clone(docFieldOpt.get());

        pDocField.setId(docFieldId);
        DocField docFieldUpdated = docFieldRepository.save(pDocField);

        ChangeLog changeLog = new ChangeLog(docFieldBefore, docFieldUpdated);
        systemLogService.writeLogUpdate(MODULE.STORAGE, ACTION.STG_DOC_DOCTYPE_CONFIG, MasterObject.DocField, "Cập nhật DocField", changeLog);
        logger.info(DocumentInfoServiceImpl.class.getName() + ": Cập nhật doc_field " + docFieldId);
        return docFieldUpdated;
    }

    @Transactional
    @Override
    public String delete(Integer id) {
        Optional<DocField> docField = this.findById(id);
        if (docField.isEmpty()) {
            throw new ResourceNotFoundException("DocField not found!", true);
        }
        if (!docDataService.findByDocField(id).isEmpty()) {
            throw new DataInUseException(ErrorCode.DATA_LOCKED_ERROR.getDescription());
        }
        docFieldRepository.deleteById(id);
        systemLogService.writeLogDelete(MODULE.STORAGE, ACTION.STG_DOC_DOCTYPE_CONFIG, MasterObject.DocField, "Xóa DocField", docField.get().getName());
        logger.info(DocumentInfoServiceImpl.class.getName() + ": Xóa DocField id=" + id);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }
}