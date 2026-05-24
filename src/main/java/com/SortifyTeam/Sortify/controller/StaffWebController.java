package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.Staff;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/staff")
public class StaffWebController {

    private final UserRepository userRepo;
    private final StaffRepository staffRepo;
    private final PasswordEncoder passwordEncoder;

    public StaffWebController(UserRepository userRepo, StaffRepository staffRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.staffRepo = staffRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String halamanStaff(Model model) {
        List<User> users = userRepo.findByRole(User.Role.PETUGAS);
        model.addAttribute("daftarStaff", users);
        return "staff-view";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("staff", new Staff());
        model.addAttribute("isNew", true);
        return "staff-form";
    }

    @PostMapping("/tambah")
    public String simpanTambah(@ModelAttribute Staff staff) {
        User user = new User();
        user.setUsername(staff.getUsername());
        user.setPassword(passwordEncoder.encode(staff.getPassword()));
        user.setFullName(staff.getNama());
        user.setRole(User.Role.PETUGAS);
        user.setTotalPoints(0);
        userRepo.save(user);

        staff.setUser(user);
        staff.setPassword(user.getPassword());
        staffRepo.save(staff);
        return "redirect:/admin/staff";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff tidak ditemukan: " + id));
        model.addAttribute("staff", staff);
        model.addAttribute("isNew", false);
        return "staff-form";
    }

    @PostMapping("/edit/{id}")
    public String simpanEdit(@PathVariable Long id, @ModelAttribute Staff staff) {
        Staff existing = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff tidak ditemukan: " + id));

        existing.setNama(staff.getNama());
        existing.setNip(staff.getNip());

        if (staff.getPassword() != null && !staff.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(staff.getPassword()));

            User user = existing.getUser();
            if (user != null) {
                user.setPassword(existing.getPassword());
                user.setFullName(staff.getNama());
                userRepo.save(user);
            }
        }

        staffRepo.save(existing);
        return "redirect:/admin/staff";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id) {
        Staff staff = staffRepo.findById(id).orElse(null);
        if (staff != null) {
            User user = staff.getUser();
            staffRepo.deleteById(id);
            if (user != null) {
                userRepo.delete(user);
            }
        }
        return "redirect:/admin/staff";
    }
}
