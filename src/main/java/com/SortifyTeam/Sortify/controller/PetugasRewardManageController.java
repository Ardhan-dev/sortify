package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.RewardItem;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.service.LogAktivitasService;
import com.SortifyTeam.Sortify.service.RewardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/petugas/reward/manage")
public class PetugasRewardManageController {

    private final RewardService rewardService;
    private final LogAktivitasService logAktivitasService;
    private final UserRepository userRepo;

    public PetugasRewardManageController(RewardService rewardService,
                                          LogAktivitasService logAktivitasService,
                                          UserRepository userRepo) {
        this.rewardService = rewardService;
        this.logAktivitasService = logAktivitasService;
        this.userRepo = userRepo;
    }

    @GetMapping
    public String halamanReward(Model model) {
        List<RewardItem> items = rewardService.getAllRewardItems();
        model.addAttribute("daftarReward", items);
        model.addAttribute("totalStock", items.stream().mapToInt(RewardItem::getStock).sum());
        model.addAttribute("outOfStock", items.stream().filter(i -> i.getStock() <= 0).count());
        return "petugas-reward-manage";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("isEdit", false);
        return "petugas-reward-form";
    }

    @PostMapping("/tambah")
    public String simpanTambah(Authentication auth,
                               @RequestParam String namaBarang,
                               @RequestParam int pointNeeded,
                               @RequestParam int stock,
                               RedirectAttributes redirectAttributes) {
        try {
            rewardService.simpanReward(namaBarang, pointNeeded, stock);
            User user = userRepo.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                logAktivitasService.catatAktivitas(user.getUsername(), user.getRole().name(),
                        "TAMBAH_REWARD", "Menambah reward " + namaBarang + " (" + pointNeeded + " poin, stok " + stock + ")");
            }
            redirectAttributes.addFlashAttribute("success", "Reward " + namaBarang + " berhasil ditambahkan.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/petugas/reward/manage/tambah";
        }
        return "redirect:/petugas/reward/manage";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        model.addAttribute("reward", rewardService.getRewardById(id));
        model.addAttribute("isEdit", true);
        return "petugas-reward-form";
    }

    @PostMapping("/edit/{id}")
    public String simpanEdit(Authentication auth,
                             @PathVariable Long id,
                             @RequestParam String namaBarang,
                             @RequestParam int pointNeeded,
                             @RequestParam int stock,
                             RedirectAttributes redirectAttributes) {
        try {
            RewardItem sebelum = rewardService.getRewardById(id);
            rewardService.updateReward(id, namaBarang, pointNeeded, stock);
            User user = userRepo.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                logAktivitasService.catatAktivitas(user.getUsername(), user.getRole().name(),
                        "EDIT_REWARD", "Mengubah reward " + sebelum.getNamaBarang() + " → " + namaBarang
                        + " (poin: " + sebelum.getPointNeeded() + "→" + pointNeeded
                        + ", stok: " + sebelum.getStock() + "→" + stock + ")");
            }
            redirectAttributes.addFlashAttribute("success", "Reward " + namaBarang + " berhasil diperbarui.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/petugas/reward/manage/edit/" + id;
        }
        return "redirect:/petugas/reward/manage";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(Authentication auth, @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        try {
            RewardItem item = rewardService.getRewardById(id);
            rewardService.hapusReward(id);
            User user = userRepo.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                logAktivitasService.catatAktivitas(user.getUsername(), user.getRole().name(),
                        "HAPUS_REWARD", "Menghapus reward " + item.getNamaBarang() + " (" + item.getPointNeeded() + " poin)");
            }
            redirectAttributes.addFlashAttribute("success", "Reward " + item.getNamaBarang() + " berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/petugas/reward/manage";
    }
}
