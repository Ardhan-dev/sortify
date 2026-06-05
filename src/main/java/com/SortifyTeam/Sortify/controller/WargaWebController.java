package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.Transaksi;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.model.Warga;
import com.SortifyTeam.Sortify.repository.TransaksiRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/warga")
public class WargaWebController {

    private final UserRepository userRepo;
    private final WargaRepository wargaRepo;
    private final PasswordEncoder passwordEncoder;
    private final TransaksiRepository transaksiRepo;

    public WargaWebController(UserRepository userRepo, WargaRepository wargaRepo,
                              PasswordEncoder passwordEncoder, TransaksiRepository transaksiRepo) {
        this.userRepo = userRepo;
        this.wargaRepo = wargaRepo;
        this.passwordEncoder = passwordEncoder;
        this.transaksiRepo = transaksiRepo;
    }

    @GetMapping
    public String halamanWarga(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        Page<User> wargaPage = userRepo.findByRole(User.Role.WARGA,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        model.addAttribute("daftarWarga", wargaPage.getContent());
        model.addAttribute("currentPage", wargaPage.getNumber());
        model.addAttribute("totalPages", wargaPage.getTotalPages());
        model.addAttribute("totalElements", wargaPage.getTotalElements());
        model.addAttribute("pageSize", size);
        return "warga-view";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("warga", new Warga());
        model.addAttribute("isNew", true);
        return "warga-form";
    }

    @PostMapping("/tambah")
    public String simpanTambah(@ModelAttribute Warga warga,
                               @RequestParam("password") String password,
                               RedirectAttributes redirectAttributes) {
        if (userRepo.findByUsername(warga.getUsername()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username '" + warga.getUsername() + "' sudah digunakan.");
            return "redirect:/admin/warga/tambah";
        }
        User user = new User();
        user.setUsername(warga.getUsername());
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(warga.getNama());
        user.setRole(User.Role.WARGA);
        user.setTotalPoints(0);
        userRepo.save(user);

        warga.setUser(user);
        wargaRepo.save(warga);
        return "redirect:/admin/warga";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + id));
        Warga warga = wargaRepo.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Data warga tidak ditemukan untuk user: " + id));
        List<Transaksi> transaksiList = transaksiRepo.findByWargaOrderByTanggalTransaksiDesc(warga);
        model.addAttribute("user", user);
        model.addAttribute("warga", warga);
        model.addAttribute("transaksiList", transaksiList);
        return "warga-detail";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + id));
        Warga warga = wargaRepo.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Data warga tidak ditemukan untuk user: " + id));
        model.addAttribute("warga", warga);
        model.addAttribute("userId", id);
        model.addAttribute("isNew", false);
        return "warga-form";
    }

    @PostMapping("/edit/{id}")
    public String simpanEdit(@PathVariable Long id,
                             @ModelAttribute Warga warga,
                             @RequestParam(value = "password", required = false) String password) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + id));
        Warga existing = wargaRepo.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Data warga tidak ditemukan untuk user: " + id));

        existing.setNama(warga.getNama());
        existing.setAlamat(warga.getAlamat());
        existing.setNoHp(warga.getNoHp());

        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(warga.getNama());
            userRepo.save(user);
        }

        wargaRepo.save(existing);
        return "redirect:/admin/warga";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepo.findById(id).orElse(null);
        if (user != null) {
            Warga warga = wargaRepo.findByUser(user).orElse(null);
            if (warga != null) {
                if (!transaksiRepo.findByWargaOrderByTanggalTransaksiDesc(warga).isEmpty()) {
                    redirectAttributes.addFlashAttribute("error",
                            "Warga '" + warga.getNama() + "' memiliki riwayat transaksi dan tidak dapat dihapus.");
                    return "redirect:/admin/warga";
                }
                wargaRepo.delete(warga);
                userRepo.delete(user);
            }
        }
        return "redirect:/admin/warga";
    }
}
