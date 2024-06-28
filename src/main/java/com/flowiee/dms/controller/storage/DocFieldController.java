package com.flowiee.dms.controller.storage;

import com.flowiee.dms.entity.storage.DocField;
import com.flowiee.dms.exception.ResourceNotFoundException;
import com.flowiee.dms.model.ApiResponse;
import com.flowiee.dms.service.storage.DocFieldService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/stg")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DocFieldController {
    DocFieldService docFieldService;

    @PostMapping("/doc/doc-field/create")
    @PreAuthorize("@vldModuleStorage.updateDoc(true)")
    public ModelAndView createDocField(DocField docField, HttpServletRequest request) {
        docField.setRequired(docField.getRequired() != null ? docField.getRequired() : false);
        docField.setStatus(false);
        docFieldService.save(docField);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @PostMapping(value = "/doc/doc-field/update/{id}", params = "update")
    @PreAuthorize("@vldModuleStorage.updateDoc(true)")
    public ModelAndView updateDocField(HttpServletRequest request, @ModelAttribute("docField") DocField docField, @PathVariable("id") Integer docFieldId) {
        if (docFieldId <= 0 || docFieldService.findById(docFieldId).isEmpty()) {
            throw new ResourceNotFoundException("DocField not found!", false);
        }
        docField.setRequired(docField.getRequired() != null ? docField.getRequired() : false);
        docFieldService.update(docField, docFieldId);
        return new ModelAndView("redirect:" + request.getHeader("referer"));
    }

    @Operation(summary = "Delete field")
    @DeleteMapping("/doc/doc-field/delete/{id}")
    @PreAuthorize("@vldModuleStorage.deleteDoc(true)")
    public ApiResponse<String> deleteDocField(@PathVariable("id") Integer docFiledId) {
        if (docFieldService.findById(docFiledId).isEmpty()) {
            throw new ResourceNotFoundException("DocField not found!", false);
        }
        return ApiResponse.ok(docFieldService.delete(docFiledId));
    }
}