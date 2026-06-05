package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import com.SortifyTeam.Sortify.repository.TransaksiRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import com.SortifyTeam.Sortify.service.NotifikasiService;
import com.SortifyTeam.Sortify.service.PointService;
import com.SortifyTeam.Sortify.service.TransaksiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/transaksi")
public class TransaksiWebController {

    private final TransaksiRepository transaksiRepo;
    private final TransaksiService transaksiService;
    private final WargaRepository wargaRepo;
    private final StaffRepository staffRepo;
    private final KategoriSampahRepository kategoriRepo;
    private final UserRepository userRepo;
    private final PointService pointService;
    private final NotifikasiService notifikasiService;

    public TransaksiWebController(TransaksiRepository transaksiRepo,
                                   TransaksiService transaksiService,
                                   WargaRepository wargaRepo,
                                   StaffRepository staffRepo,
                                   KategoriSampahRepository kategoriRepo,
                                   UserRepository userRepo,
                                   PointService pointService,
                                   NotifikasiService notifikasiService) {
        this.transaksiRepo = transaksiRepo;
        this.transaksiService = transaksiService;
        this.wargaRepo = wargaRepo;
        this.staffRepo = staffRepo;
        this.kategoriRepo = kategoriRepo;
        this.userRepo = userRepo;
        this.pointService = pointService;
        this.notifikasiService = notifikasiService;
    }

    private Staff getCurrentStaff(Authentication auth) {
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return staffRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Staff tidak ditemukan untuk user: " + auth.getName()));
    }

    @GetMapping
    public String halamanTransaksi(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model) {
        Page<Transaksi> transaksiPage = transaksiRepo.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "idTransaksi")));
        List<Transaksi> semua = transaksiRepo.findAll();
        double totalBerat = semua.stream()
                .filter(t -> t.getTotalBerat() != null)
                .mapToDouble(Transaksi::getTotalBerat).sum();
        double totalPoin = semua.stream()
                .filter(t -> t.getTotalPoin() != null)
                .mapToDouble(Transaksi::getTotalPoin).sum();
        model.addAttribute("daftarTransaksi", transaksiPage.getContent());
        model.addAttribute("currentPage", transaksiPage.getNumber());
        model.addAttribute("totalPages", transaksiPage.getTotalPages());
        model.addAttribute("totalElements", transaksiPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalBerat", totalBerat);
        model.addAttribute("totalPoin", totalPoin);
        return "transaksi-view";
    }

    @GetMapping("/tambah")
    public String formTambah(Authentication auth, Model model) {
        model.addAttribute("transaksi", new Transaksi());
        model.addAttribute("daftarWarga", wargaRepo.findAll());
        model.addAttribute("daftarStaff", staffRepo.findAll());
        model.addAttribute("daftarKategori", kategoriRepo.findByIsActiveTrue());
        try {
            model.addAttribute("currentStaff", getCurrentStaff(auth));
        } catch (Exception e) {
            model.addAttribute("currentStaff", null);
        }
        return "transaksi-form";
    }

    @PostMapping("/tambah")
    @Transactional
    public String simpanTambah(Authentication auth,
                                @RequestParam Long idWarga,
                                @RequestParam(required = false) List<Long> idKategori,
                                @RequestParam(required = false) List<Double> beratKategori,
                                @RequestParam(required = false, defaultValue = "") String detail) {
        Transaksi transaksi = new Transaksi();
        transaksi.setWarga(wargaRepo.findById(idWarga)
                .orElseThrow(() -> new IllegalArgumentException("Warga tidak ditemukan")));
        transaksi.setStaff(getCurrentStaff(auth));
        transaksi.setTanggalTransaksi(LocalDateTime.now());
        transaksi.setStatus(Transaksi.StatusTransaksi.PENDING);
        transaksi.setDetail(detail);
        transaksiRepo.save(transaksi);

        double totalBerat = 0;
        double totalPoin = 0;
        List<TransaksiDetail> details = new ArrayList<>();

        if (idKategori != null) {
            for (int i = 0; i < idKategori.size(); i++) {
                Double b = beratKategori.get(i);
                if (b == null || b <= 0 || b > TransaksiService.MAX_WEIGHT_KG) continue;
                KategoriSampah kategori = kategoriRepo.findById(idKategori.get(i))
                        .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan"));
                double berat = b;
                int subtotal = (int) (berat * kategori.getPoinPerKg());

                TransaksiDetail td = new TransaksiDetail();
                td.setTransaksi(transaksi);
                td.setKategoriSampah(kategori);
                td.setBeratEstimasi(berat);
                td.setBeratFinal(berat);
                td.setSubTotalPoin((double) subtotal);
                details.add(td);

                totalBerat += berat;
                totalPoin += subtotal;
            }
        }
        transaksi.setDetails(details);
        transaksi.setTotalBerat(totalBerat);
        transaksi.setTotalPoin(totalPoin);
        transaksiRepo.save(transaksi);

        return "redirect:/admin/transaksi";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(Authentication auth, @PathVariable Long id, Model model) {
        Transaksi transaksi = transaksiService.getTransaksiById(id);
        model.addAttribute("transaksi", transaksi);
        model.addAttribute("daftarWarga", wargaRepo.findAll());
        model.addAttribute("daftarStaff", staffRepo.findAll());
        model.addAttribute("daftarKategori", kategoriRepo.findByIsActiveTrue());
        try {
            model.addAttribute("currentStaff", getCurrentStaff(auth));
        } catch (Exception e) {
            model.addAttribute("currentStaff", null);
        }
        return "transaksi-form";
    }

    @PostMapping("/edit/{id}")
    @Transactional
    public String simpanEdit(Authentication auth, @PathVariable Long id,
                              @RequestParam Long idWarga,
                              @RequestParam(required = false, defaultValue = "") String detail,
                              @RequestParam Transaksi.StatusTransaksi status,
                              @RequestParam(required = false) List<Long> detailId,
                              @RequestParam(required = false) List<Double> beratFinal,
                              RedirectAttributes redirectAttributes) {
        Transaksi transaksi = transaksiService.getTransaksiById(id);
        Transaksi.StatusTransaksi oldStatus = transaksi.getStatus();

        if (!isValidTransition(oldStatus, status)) {
            redirectAttributes.addFlashAttribute("error",
                    "Transaksi #" + id + " tidak bisa berubah dari " + oldStatus + " ke " + status + ".");
            return "redirect:/admin/transaksi";
        }

        transaksi.setWarga(wargaRepo.findById(idWarga)
                .orElseThrow(() -> new IllegalArgumentException("Warga tidak ditemukan")));
        transaksi.setStaff(getCurrentStaff(auth));
        transaksi.setDetail(detail);
        transaksi.setStatus(status);

        double totalBerat = 0;
        double totalPoin = 0;

        for (int i = 0; i < transaksi.getDetails().size(); i++) {
            TransaksiDetail td = transaksi.getDetails().get(i);
            Double bf = (beratFinal != null && i < beratFinal.size() && beratFinal.get(i) != null && beratFinal.get(i) > 0 && beratFinal.get(i) <= TransaksiService.MAX_WEIGHT_KG)
                    ? beratFinal.get(i) : td.getBeratEstimasi();
            td.setBeratFinal(bf);

            int ppk = (td.getKategoriSampah() != null && td.getKategoriSampah().getPoinPerKg() != null)
                    ? td.getKategoriSampah().getPoinPerKg() : 100;
            double subPoin = bf * ppk;
            td.setSubTotalPoin(subPoin);

            totalBerat += bf;
            totalPoin += subPoin;
        }

        transaksi.setTotalBerat(totalBerat);
        transaksi.setTotalPoin(totalPoin);
        transaksiRepo.save(transaksi);

        if (Transaksi.StatusTransaksi.SELESAI.equals(status) && !Transaksi.StatusTransaksi.SELESAI.equals(oldStatus)) {
            int poinBulat = (int) Math.round(totalPoin);
            Warga entitasWarga = transaksi.getWarga();
            if (entitasWarga != null && entitasWarga.getUser() != null) {
                pointService.tambahPoint(entitasWarga.getUser(), poinBulat,
                        "Poin transaksi #" + id + " via admin");
                notifikasiService.buatNotifikasi(entitasWarga.getUser(),
                        "Transaksi #" + id + " telah selesai! " + poinBulat + " poin ditambahkan.");
            }
        }

        redirectAttributes.addFlashAttribute("success", "Transaksi #" + id + " berhasil diperbarui.");
        return "redirect:/admin/transaksi";
    }

    @GetMapping("/hapus/{id}")
    @Transactional
    public String hapus(@PathVariable Long id) {
        transaksiRepo.deleteById(id);
        return "redirect:/admin/transaksi";
    }

    private boolean isValidTransition(Transaksi.StatusTransaksi oldStatus, Transaksi.StatusTransaksi newStatus) {
        if (oldStatus == newStatus) return true;
        return switch (oldStatus) {
            case PENDING -> newStatus == Transaksi.StatusTransaksi.DIPROSES || newStatus == Transaksi.StatusTransaksi.DITOLAK || newStatus == Transaksi.StatusTransaksi.DIBATALKAN;
            case DIPROSES -> newStatus == Transaksi.StatusTransaksi.SELESAI || newStatus == Transaksi.StatusTransaksi.PENDING;
            case SELESAI, DITOLAK, DIBATALKAN -> false;
        };
    }
}
