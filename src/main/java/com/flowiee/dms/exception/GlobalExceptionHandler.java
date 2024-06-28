package com.flowiee.dms.exception;

import com.flowiee.dms.base.BaseController;
import com.flowiee.dms.model.ApiResponse;
import com.flowiee.dms.utils.PagesUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler extends BaseController {
    @ExceptionHandler
    public ModelAndView exceptionHandler(AuthenticationException ex) {
        logger.error(ex.getMessage(), ex);
        return new ModelAndView(PagesUtils.SYS_LOGIN);
    }

    @ExceptionHandler
    public Object exceptionHandler(ResourceNotFoundException ex) {
        logger.error(ex.getMessage(), ex);
        if (ex.isRedirectErrorUI()) {
            ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
            ModelAndView modelAndView = new ModelAndView(PagesUtils.SYS_ERROR);
            modelAndView.addObject("error", error);
            return baseView(modelAndView);
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage(), ex, HttpStatus.NOT_FOUND));
        }
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Object>> exceptionHandler(BadRequestException ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage(), ex, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Object>> exceptionHandler(DataExistsException ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage(), ex, HttpStatus.CONFLICT));
    }

    @ExceptionHandler
    public ModelAndView exceptionHandler(ForbiddenException ex) {
        logger.error(ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        ModelAndView modelAndView = new ModelAndView(PagesUtils.SYS_ERROR);
        modelAndView.addObject("error", error);
        return baseView(modelAndView);
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Object>> exceptionHandler(DataInUseException ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage(), ex, HttpStatus.LOCKED));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Object>> exceptionHandler(AppException ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage(), ex, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Object>> exceptionHandler(RuntimeException ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage(), ex, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Object>> exceptionHandler(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage(), ex, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}