package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.service.FileStorageService;
import com.SortifyTeam.Sortify.service.RewardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reward")
public class RewardItemWebController {

    private final RewardService rewardService;
    private final FileStorageService fileStorageService;

    public RewardItemWebController(RewardService rewardService, FileStorageService fileStorageService) {
        this.rewardService = rewardService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String halamanReward(Model model) {
        java.util.List<com.SortifyTeam.Sortify.model.RewardItem> items = rewardService.getAllRewardItems();
        model.addAttribute("daftarReward", items);
        model.addAttribute("totalStock", items.stream().mapToInt(com.SortifyTeam.Sortify.model.RewardItem::getStock).sum());
        model.addAttribute("outOfStock", items.stream().filter(i -> i.getStock() <= 0).count());
        model.addAttribute("penukaranList", rewardService.getAllPenukaran());
        model.addAttribute("totalPenukaran", rewardService.countTotalPenukaran());
        model.addAttribute("penukaranPending", rewardService.getAllPenukaran().stream().filter(
                p -> p.getStatus() == com.SortifyTeam.Sortify.model.PenukaranReward.StatusPenukaran.PENDING).count());
        return "admin-reward";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("isEdit", false);
        return "admin-reward-form";
    }

    @PostMapping("/tambah")
    public String simpanTambah(@RequestParam String namaBarang,
                               @RequestParam int pointNeeded,
                               @RequestParam int stock,
                               RedirectAttributes redirectAttributes) {
        try {
            rewardService.simpanReward(namaBarang, pointNeeded, stock);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/reward/tambah";
        }
        return "redirect:/admin/reward";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        model.addAttribute("reward", rewardService.getRewardById(id));
        model.addAttribute("isEdit", true);
        return "admin-reward-form";
    }

    @PostMapping("/edit/{id}")
    public String simpanEdit(@PathVariable Long id,
                             @RequestParam String namaBarang,
                             @RequestParam int pointNeeded,
                             @RequestParam int stock,
                             RedirectAttributes redirectAttributes) {
        try {
            rewardService.updateReward(id, namaBarang, pointNeeded, stock);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/reward/edit/" + id;
        }
        return "redirect:/admin/reward";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rewardService.hapusReward(id);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reward";
    }

    @PostMapping("/penukaran/selesai/{id}")
    public String selesaikanPenukaran(@PathVariable Long id,
                                      @RequestParam("fotoBukti") MultipartFile fotoBukti,
                                      RedirectAttributes redirectAttributes) {
        try {
            if (fotoBukti.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Foto bukti pengambilan harus diupload.");
                return "redirect:/admin/reward";
            }
            String namaFoto = fileStorageService.storeFile(fotoBukti, null);
            rewardService.selesaikanPenukaran(id, namaFoto);
            redirectAttributes.addFlashAttribute("success", "Penukaran reward #" + id + " telah dikonfirmasi!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal: " + e.getMessage());
        }
        return "redirect:/admin/reward";
    }
}
