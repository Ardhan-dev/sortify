package com.SortifyTeam.Sortify.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model, HttpServletRequest request) {
        log.warn("[400] IllegalArgumentException di {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMsg", ex.getMessage());
        model.addAttribute("statusCode", 400);
        model.addAttribute("statusPhrase", "Bad Request");
        model.addAttribute("path", request.getRequestURI());
        return "error/custom-error";
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntime(RuntimeException ex, Model model, HttpServletRequest request) {
        log.error("[500] RuntimeException di {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMsg", ex.getMessage());
        model.addAttribute("statusCode", 500);
        model.addAttribute("statusPhrase", "Internal Server Error");
        model.addAttribute("path", request.getRequestURI());
        return "error/custom-error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model, HttpServletRequest request) {
        log.error("[500] Unexpected error di {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMsg", "Terjadi kesalahan yang tidak terduga. Silakan coba lagi.");
        model.addAttribute("statusCode", 500);
        model.addAttribute("statusPhrase", "Internal Server Error");
        model.addAttribute("path", request.getRequestURI());
        return "error/custom-error";
    }
}
