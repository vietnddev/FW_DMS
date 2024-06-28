package com.flowiee.dms.service.storage.impl;

import com.flowiee.dms.entity.storage.DocData;
import com.flowiee.dms.entity.storage.DocField;
import com.flowiee.dms.entity.storage.Document;
import com.flowiee.dms.exception.AppException;
import com.flowiee.dms.exception.ResourceNotFoundException;
import com.flowiee.dms.model.ACTION;
import com.flowiee.dms.model.DocMetaModel;
import com.flowiee.dms.model.MODULE;
import com.flowiee.dms.repository.storage.DocumentRepository;
import com.flowiee.dms.service.BaseService;
import com.flowiee.dms.service.storage.DocDataService;
import com.flowiee.dms.service.storage.DocMetadataService;
import com.flowiee.dms.utils.constants.ErrorCode;
import com.flowiee.dms.utils.constants.MasterObject;
import com.flowiee.dms.utils.constants.MessageCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DocMetadataServiceImpl extends BaseService implements DocMetadataService {
    DocDataService     docDataService;
    DocumentRepository documentRepository;

    @Override
    public List<DocMetaModel> findMetadata(Integer documentId) {
        List<DocMetaModel> listReturn = new ArrayList<>();
        try {
            List<Object[]> listData = documentRepository.findMetadata(documentId);
            if (!listData.isEmpty()) {
                for (Object[] data : listData) {
                    DocMetaModel metadata = new DocMetaModel();
                    metadata.setFieldId(Integer.parseInt(String.valueOf(data[0])));
                    metadata.setFieldName(String.valueOf(data[1]));
                    metadata.setDataId(ObjectUtils.isNotEmpty(data[2]) ? Integer.parseInt(String.valueOf(data[2])) : 0);
                    metadata.setDataValue(ObjectUtils.isNotEmpty(data[3]) ? String.valueOf(data[3]) : null);
                    metadata.setFieldType(String.valueOf(data[4]));
                    metadata.setFieldRequired(String.valueOf(data[5]).equals("1"));
                    listReturn.add(metadata);
                }
            }
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.SEARCH_ERROR.getDescription(), "metadata of document"), ex);
        }
        return listReturn;
    }

    @Override
    public String updateMetadata(List<DocMetaModel> metaDTOs, Integer documentId) {
        Optional<Document> document = documentRepository.findById(documentId);
        if (document.isEmpty()) {
            throw new ResourceNotFoundException("Document not found!", true);
        }

        for (DocMetaModel metaDTO : metaDTOs) {
            DocData docData = docDataService.findByFieldIdAndDocId(metaDTO.getFieldId(), documentId);
            if (docData != null) {
                docDataService.update(metaDTO.getDataValue(), docData.getId());
            } else {
                docData = new DocData();
                docData.setDocField(new DocField(metaDTO.getFieldId()));
                docData.setDocument(new Document(documentId));
                docData.setValue(metaDTO.getDataValue());
                docDataService.save(docData);
            }
        }

        systemLogService.writeLogUpdate(MODULE.STORAGE, ACTION.STG_DOC_UPDATE, MasterObject.Document, "Update metadata of " + document, "-", "-");
        logger.info(DocumentInfoServiceImpl.class.getName() + ": Update metadata docId=" + documentId);

        return MessageCode.UPDATE_SUCCESS.getDescription();
    }
}
