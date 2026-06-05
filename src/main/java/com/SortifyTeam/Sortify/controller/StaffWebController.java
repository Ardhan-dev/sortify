package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.Staff;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import com.SortifyTeam.Sortify.repository.TransaksiRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/staff")
public class StaffWebController {

    private final UserRepository userRepo;
    private final StaffRepository staffRepo;
    private final PasswordEncoder passwordEncoder;
    private final TransaksiRepository transaksiRepo;

    public StaffWebController(UserRepository userRepo, StaffRepository staffRepo,
                              PasswordEncoder passwordEncoder, TransaksiRepository transaksiRepo) {
        this.userRepo = userRepo;
        this.staffRepo = staffRepo;
        this.passwordEncoder = passwordEncoder;
        this.transaksiRepo = transaksiRepo;
    }

    @GetMapping
    public String halamanStaff(Model model) {
        List<User> daftarStaff = userRepo.findByRole(User.Role.PETUGAS);
        model.addAttribute("daftarStaff", daftarStaff);
        return "staff-view";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("staff", new Staff());
        model.addAttribute("isNew", true);
        return "staff-form";
    }

    @PostMapping("/tambah")
    public String simpanTambah(@ModelAttribute Staff staff,
                               @RequestParam("password") String password,
                               RedirectAttributes redirectAttributes) {
        if (userRepo.findByUsername(staff.getUsername()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username '" + staff.getUsername() + "' sudah digunakan.");
            return "redirect:/admin/staff/tambah";
        }
        User user = new User();
        user.setUsername(staff.getUsername());
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(staff.getNama());
        user.setRole(User.Role.PETUGAS);
        user.setTotalPoints(0);
        userRepo.save(user);

        staff.setUser(user);
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
    public String simpanEdit(@PathVariable Long id,
                             @ModelAttribute Staff staff,
                             @RequestParam(value = "password", required = false) String password) {
        Staff existing = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff tidak ditemukan: " + id));

        existing.setNama(staff.getNama());
        existing.setNip(staff.getNip());
        existing.setJabatan(staff.getJabatan());
        existing.setUsername(staff.getUsername());

        if (password != null && !password.isEmpty()) {
            User user = existing.getUser();
            if (user != null) {
                user.setPassword(passwordEncoder.encode(password));
                user.setFullName(staff.getNama());
                userRepo.save(user);
            }
        }

        staffRepo.save(existing);
        return "redirect:/admin/staff";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Staff staff = staffRepo.findById(id).orElse(null);
        if (staff != null) {
            if (!transaksiRepo.findByStaff(staff).isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Staff '" + staff.getNama() + "' memiliki riwayat transaksi dan tidak dapat dihapus.");
                return "redirect:/admin/staff";
            }
            User user = staff.getUser();
            staffRepo.deleteById(id);
            if (user != null) {
                userRepo.delete(user);
            }
        }
        return "redirect:/admin/staff";
    }
}
