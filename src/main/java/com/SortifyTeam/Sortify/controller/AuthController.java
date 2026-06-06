package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.dto.RegisterDTO;
import com.SortifyTeam.Sortify.model.DropPoint;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.model.Warga;
import com.SortifyTeam.Sortify.repository.DropPointRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import com.SortifyTeam.Sortify.service.LogAktivitasService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepo;
    private final WargaRepository wargaRepo;
    private final PasswordEncoder passwordEncoder;
    private final LogAktivitasService logAktivitasService;
    private final DropPointRepository dropPointRepo;

    public AuthController(UserRepository userRepo, WargaRepository wargaRepo,
                          PasswordEncoder passwordEncoder,
                          LogAktivitasService logAktivitasService,
                          DropPointRepository dropPointRepo) {
        this.userRepo = userRepo;
        this.wargaRepo = wargaRepo;
        this.passwordEncoder = passwordEncoder;
        this.logAktivitasService = logAktivitasService;
        this.dropPointRepo = dropPointRepo;
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String role = authentication.getAuthorities().stream()
                    .findFirst().map(a -> a.getAuthority()).orElse("");
            if (role.contains("ADMIN")) return "redirect:/admin/dashboard";
            if (role.contains("PETUGAS")) return "redirect:/petugas/dashboard";
            if (role.contains("WARGA")) return "redirect:/warga/dashboard";
        }
        return "redirect:/kamus-sampah";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "suspended", required = false) String suspended,
                            @RequestParam(value = "banned", required = false) String banned,
                            Model model, HttpServletRequest request) {
        request.getSession();
        if (error != null) model.addAttribute("errorMsg", "Username atau password salah.");
        if (logout != null) model.addAttribute("logoutMsg", "Kamu berhasil keluar.");
        if (suspended != null) model.addAttribute("errorMsg", "Akun Anda telah dinonaktifkan. Silakan hubungi admin untuk pengajuan aktivasi kembali.");
        if (banned != null) model.addAttribute("errorMsg", "Akun Anda telah diblokir karena melanggar ketentuan. Tidak dapat login.");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpServletRequest request) {
        request.getSession();
        model.addAttribute("formData", new RegisterDTO());
        model.addAttribute("dropPointList", dropPointRepo.findByAktifTrueOrderByNamaAsc());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterDTO dto, Model model) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("errorMsg", "Password tidak cocok.");
            model.addAttribute("formData", dto);
            return "register";
        }

        if (userRepo.findByUsername(dto.getUsername()).isPresent()) {
            model.addAttribute("errorMsg", "Username sudah digunakan.");
            model.addAttribute("formData", dto);
            return "register";
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName((dto.getNamaDepan() + " " + (dto.getNamaBelakang() != null ? dto.getNamaBelakang() : "")).trim());
        user.setRole(User.Role.WARGA);
        user.setTotalPoints(0);
        userRepo.save(user);

        Warga warga = new Warga();
        warga.setNama(user.getFullName());
        warga.setUsername(user.getUsername());
        warga.setAlamat(dto.getAlamat() != null && !dto.getAlamat().isBlank() ? dto.getAlamat() : "-");
        warga.setNoHp(dto.getNoTelepon() != null && !dto.getNoTelepon().isBlank() ? dto.getNoTelepon() : "-");
        warga.setUser(user);
        if (dto.getDropPointId() != null) {
            dropPointRepo.findById(dto.getDropPointId()).ifPresent(warga::setDropPoint);
        }
        wargaRepo.save(warga);

        logAktivitasService.catatAktivitas(
            user.getUsername(),
            "WARGA",
            "REGISTRASI_WARGA",
            "Warga baru mendaftar: " + user.getFullName() + " (" + user.getUsername() + ")"
        );

        model.addAttribute("registerSuccess", "Akun berhasil dibuat. Silakan login.");
        return "login";
    }
}


