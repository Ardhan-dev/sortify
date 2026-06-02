package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.service.RewardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reward")
public class RewardItemWebController {

    private final RewardService rewardService;

    public RewardItemWebController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping
    public String halamanReward(Model model) {
        model.addAttribute("daftarReward", rewardService.getAllRewardItems());
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
}
