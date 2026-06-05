package com.SortifyTeam.Sortify.config;

import com.SortifyTeam.Sortify.service.PengumumanService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final PengumumanService pengumumanService;

    public GlobalControllerAdvice(PengumumanService pengumumanService) {
        this.pengumumanService = pengumumanService;
    }

    @ModelAttribute("pengumumanAktif")
    public java.util.List<com.SortifyTeam.Sortify.model.Pengumuman> pengumumanAktif() {
        return pengumumanService.getAktif();
    }
}
