package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.*;
import com.SortifyTeam.Sortify.service.*;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepo;
    private final RewardService rewardService;
    private final TransaksiRepository transaksiRepo;
    private final PenukaranRewardRepository penukaranRepo;

    @Value("")
    private String uploadDir;

    public AdminController(UserRepository userRepo,
                           RewardService rewardService,
                           TransaksiRepository transaksiRepo,
                           PenukaranRewardRepository penukaranRepo) {
        this.userRepo = userRepo;
        this.rewardService = rewardService;
        this.transaksiRepo = transaksiRepo;
        this.penukaranRepo = penukaranRepo;
    }

    private String simpanFotoBukti(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir, "reward").toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + ext;

            Path targetPath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("[UPLOAD REWARD] File {} tersimpan sebagai {}", original, filename);
            return "reward/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file bukti: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Admin {} sedang membuka halaman Dashboard Admin", auth.getName());
        List<Transaksi> semuaTransaksi = transaksiRepo.findAll();
        long transaksiSelesai = semuaTransaksi.stream()
                .filter(t -> t.getStatus() == Transaksi.StatusTransaksi.SELESAI)
                .count();
        long transaksiPending = semuaTransaksi.stream()
                .filter(t -> t.getStatus() == Transaksi.StatusTransaksi.PENDING)
                .count();
        double totalBerat = semuaTransaksi.stream()
                .filter(t -> t.getTotalBerat() != null)
                .mapToDouble(Transaksi::getTotalBerat).sum();

        model.addAttribute("totalWarga", userRepo.countByRole(User.Role.WARGA));
        model.addAttribute("totalPetugas", userRepo.countByRole(User.Role.PETUGAS));
        model.addAttribute("transaksiSelesai", transaksiSelesai);
        model.addAttribute("transaksiPending", transaksiPending);
        model.addAttribute("totalBerat", totalBerat);
        model.addAttribute("rewardDitukar", rewardService.countTotalPenukaran());
        return "admin-dashboard";
    }

    @GetMapping("/monitoring")
    public String monitoringReward(Model model) {
        log.info("[ACCESS] Admin membuka halaman Monitoring Tukar Reward");
        List<PenukaranReward> semuaPenukaran = penukaranRepo.findAllByOrderByTanggalPenukaranDesc();
        model.addAttribute("penukaranList", semuaPenukaran);

        long totalPenukaran = semuaPenukaran.size();
        long menungguDiproses = semuaPenukaran.stream()
                .filter(p -> p.getStatus() == PenukaranReward.StatusPenukaran.PENDING)
                .count();
        long rewardKeluar = semuaPenukaran.stream()
                .filter(p -> p.getStatus() == PenukaranReward.StatusPenukaran.SUDAH_DIAMBIL)
                .count();

        model.addAttribute("totalPenukaran", totalPenukaran);
        model.addAttribute("menungguDiproses", menungguDiproses);
        model.addAttribute("rewardKeluar", rewardKeluar);
        return "admin-monitoring";
    }

    @Transactional
    @PostMapping("/reward/konfirmasi/{id}")
    public String konfirmasiReward(@PathVariable Long id,
                                    @RequestParam("fotoBukti") MultipartFile fotoBukti,
                                    RedirectAttributes redirectAttributes) {
        log.info("[PROSES] Admin mengkonfirmasi penukaran reward #{}", id);

        if (fotoBukti.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Foto bukti penyerahan harus diupload.");
            return "redirect:/admin/monitoring";
        }

        PenukaranReward penukaran = penukaranRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Penukaran reward tidak ditemukan: " + id));

        if (penukaran.getStatus() == PenukaranReward.StatusPenukaran.SUDAH_DIAMBIL) {
            redirectAttributes.addFlashAttribute("error", "Reward #" + id + " sudah diserahkan sebelumnya.");
            return "redirect:/admin/monitoring";
        }

        String namaFoto = simpanFotoBukti(fotoBukti);
        penukaran.setFotoBukti(namaFoto);
        penukaran.setStatus(PenukaranReward.StatusPenukaran.SUDAH_DIAMBIL);
        penukaranRepo.save(penukaran);
        redirectAttributes.addFlashAttribute("success", "Reward berhasil diserahkan ke warga.");
        return "redirect:/admin/monitoring";
    }

    @GetMapping("/transaksi/export")
    public void exportTransaksi(HttpServletResponse response) throws IOException {
        log.info("[EXPORT] Admin mengexport laporan transaksi SELESAI ke CSV");
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=laporan-transaksi-selesai.csv");

        List<Transaksi> daftar = transaksiRepo.findByStatus(Transaksi.StatusTransaksi.SELESAI);

        try (Writer writer = new OutputStreamWriter(response.getOutputStream())) {
            CSVWriter csvWriter = new CSVWriter(writer);

            String[] header = {"ID Transaksi", "Tanggal", "Nama Warga", "Jenis Sampah",
                    "Berat (Kg)", "Total Poin", "Nama Petugas"};
            csvWriter.writeNext(header);

            for (Transaksi t : daftar) {
                String[] row = {
                        String.valueOf(t.getIdTransaksi()),
                        t.getTanggalTransaksi() != null
                                ? t.getTanggalTransaksi().toString() : "-",
                        t.getWarga() != null ? t.getWarga().getNama() : "-",
                        t.getJenisSampah() != null ? t.getJenisSampah().name() : "-",
                        t.getTotalBerat() != null ? String.valueOf(t.getTotalBerat()) : "0",
                        t.getTotalPoin() != null ? String.valueOf(t.getTotalPoin().intValue()) : "0",
                        t.getStaff() != null ? t.getStaff().getNama() : "-"
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
        }
    }
}
