package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.Staff;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff")
public class StaffWebController {

    @Autowired
    private StaffRepository staffRepo;

    // LIST
    @GetMapping
    public String halamanStaff(Model model) {
        model.addAttribute("daftarStaff", staffRepo.findAll());
        return "staff-view";
    }

    // FORM TAMBAH
    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("staff", new Staff());
        return "staff-form";
    }

    // SIMPAN TAMBAH
    @PostMapping("/tambah")
    public String simpanTambah(@ModelAttribute Staff staff) {
        staffRepo.save(staff);
        return "redirect:/staff";
    }

    // FORM EDIT
    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff tidak ditemukan: " + id));
        model.addAttribute("staff", staff);
        return "staff-form";
    }

    // SIMPAN EDIT
    @PostMapping("/edit/{id}")
    public String simpanEdit(@PathVariable Long id, @ModelAttribute Staff staff) {
        staff.setIdStaff(id);
        staffRepo.save(staff);
        return "redirect:/staff";
    }

    // HAPUS
    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id) {
        staffRepo.deleteById(id);
        return "redirect:/staff";
    }
}
