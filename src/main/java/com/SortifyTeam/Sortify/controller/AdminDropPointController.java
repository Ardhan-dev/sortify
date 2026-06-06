package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.DropPoint;
import com.SortifyTeam.Sortify.service.DropPointService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/drop-point")
public class AdminDropPointController {

    private final DropPointService dropPointService;

    public AdminDropPointController(DropPointService dropPointService) {
        this.dropPointService = dropPointService;
    }

    @GetMapping
    public String halaman(Model model) {
        List<DropPoint> list = dropPointService.getAllWithInactive();
        model.addAttribute("daftarDropPoint", list);
        model.addAttribute("totalAktif", list.stream().filter(DropPoint::isAktif).count());
        return "admin-drop-point";
    }

    @PostMapping("/simpan")
    public String simpan(@RequestParam String nama,
                         @RequestParam(required = false) String alamat,
                         @RequestParam(required = false) Double latitude,
                         @RequestParam(required = false) Double longitude,
                         RedirectAttributes ra) {
        try {
            dropPointService.simpan(nama, alamat, latitude, longitude);
            ra.addFlashAttribute("success", "Drop point " + nama + " berhasil ditambahkan.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/drop-point";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @RequestParam String nama,
                       @RequestParam(required = false) String alamat,
                       @RequestParam(required = false) Double latitude,
                       @RequestParam(required = false) Double longitude,
                       @RequestParam(required = false) Boolean aktif,
                       RedirectAttributes ra) {
        try {
            dropPointService.update(id, nama, alamat, latitude, longitude, aktif);
            ra.addFlashAttribute("success", "Drop point berhasil diperbarui.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/drop-point";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, RedirectAttributes ra) {
        try {
            DropPoint dp = dropPointService.getById(id);
            dropPointService.hapus(id);
            ra.addFlashAttribute("success", "Drop point " + dp.getNama() + " dinonaktifkan.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/drop-point";
    }
}
