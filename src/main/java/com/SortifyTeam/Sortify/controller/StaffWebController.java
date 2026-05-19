package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.Staff;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/staff")
public class StaffWebController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private StaffRepository staffRepo;

    @GetMapping
    public String halamanStaff(Model model) {
        List<User> users = userRepo.findByRole(User.Role.PETUGAS);
        model.addAttribute("daftarStaff", users);
        return "staff-view";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("staff", new Staff());
        return "staff-form";
    }

    @PostMapping("/tambah")
    public String simpanTambah(@ModelAttribute Staff staff) {
        staffRepo.save(staff);
        return "redirect:/admin/staff";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff tidak ditemukan: " + id));
        model.addAttribute("staff", staff);
        return "staff-form";
    }

    @PostMapping("/edit/{id}")
    public String simpanEdit(@PathVariable Long id, @ModelAttribute Staff staff) {
        staff.setIdStaff(id);
        staffRepo.save(staff);
        return "redirect:/admin/staff";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id) {
        staffRepo.deleteById(id);
        return "redirect:/admin/staff";
    }
}
