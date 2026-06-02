package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.*;
import com.SortifyTeam.Sortify.service.*;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepo;
    private final RewardService rewardService;
    private final TransaksiRepository transaksiRepo;
    private final TransaksiDetailRepository transaksiDetailRepo;
    private final PenukaranRewardRepository penukaranRepo;
    private final FileStorageService fileStorageService;
    private final WargaRepository wargaRepo;
    private final KategoriSampahRepository kategoriRepo;
    private final LogAktivitasService logAktivitasService;

    public AdminController(UserRepository userRepo,
                           RewardService rewardService,
                           TransaksiRepository transaksiRepo,
                           TransaksiDetailRepository transaksiDetailRepo,
                           PenukaranRewardRepository penukaranRepo,
                           FileStorageService fileStorageService,
                           WargaRepository wargaRepo,
                           KategoriSampahRepository kategoriRepo,
                           LogAktivitasService logAktivitasService) {
        this.userRepo = userRepo;
        this.rewardService = rewardService;
        this.transaksiRepo = transaksiRepo;
        this.transaksiDetailRepo = transaksiDetailRepo;
        this.penukaranRepo = penukaranRepo;
        this.fileStorageService = fileStorageService;
        this.wargaRepo = wargaRepo;
        this.kategoriRepo = kategoriRepo;
        this.logAktivitasService = logAktivitasService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Admin {} sedang membuka halaman Dashboard Admin", auth.getName());
        List<Transaksi> semuaTransaksi = transaksiRepo.findAll();
        long transaksiSelesai = semuaTransaksi.stream()
                .filter(t -> Transaksi.StatusTransaksi.SELESAI.equals(t.getStatus()))
                .count();
        long transaksiPending = semuaTransaksi.stream()
                .filter(t -> Transaksi.StatusTransaksi.PENDING.equals(t.getStatus()))
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

        // ── Chart data: Berat per Bulan ──
        int currentYear = Year.now().getValue();
        List<Object[]> monthlyData = transaksiRepo.getMonthlyBerat(currentYear);
        String[] labelBulan = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                               "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
        double[] dataBeratPerBulan = new double[12];
        for (Object[] row : monthlyData) {
            int month = ((Number) row[0]).intValue();
            double berat = ((Number) row[1]).doubleValue();
            dataBeratPerBulan[month - 1] = berat;
        }
        model.addAttribute("labelBulan", labelBulan);
        model.addAttribute("dataBeratPerBulan", dataBeratPerBulan);

        // ── Chart data: Berat per Kategori ──
        List<Object[]> kategoriData = transaksiDetailRepo.getBeratPerKategori();
        List<String> labelKategori = new ArrayList<>();
        List<Double> dataKategori = new ArrayList<>();
        for (Object[] row : kategoriData) {
            labelKategori.add((String) row[0]);
            dataKategori.add(((Number) row[1]).doubleValue());
        }
        model.addAttribute("labelKategori", labelKategori);
        model.addAttribute("dataKategori", dataKategori);

        // ── Data untuk filter dropdown ──
        model.addAttribute("daftarWarga", wargaRepo.findAll());
        model.addAttribute("daftarKategori", kategoriRepo.findAll());

        return "admin-dashboard";
    }

    @GetMapping("/monitoring")
    public String monitoringReward(Model model) {
        log.info("[ACCESS] Admin membuka halaman Monitoring Tukar Reward");
        List<PenukaranReward> semuaPenukaran = penukaranRepo.findAllByOrderByTanggalPenukaranDesc();
        model.addAttribute("penukaranList", semuaPenukaran);

        long totalPenukaran = semuaPenukaran.size();
        long menungguDiproses = semuaPenukaran.stream()
                .filter(p -> PenukaranReward.StatusPenukaran.PENDING.equals(p.getStatus()))
                .count();
        long rewardKeluar = semuaPenukaran.stream()
                .filter(p -> PenukaranReward.StatusPenukaran.SUDAH_DIAMBIL.equals(p.getStatus()))
                .count();

        model.addAttribute("totalPenukaran", totalPenukaran);
        model.addAttribute("menungguDiproses", menungguDiproses);
        model.addAttribute("rewardKeluar", rewardKeluar);
        return "admin-monitoring";
    }

    @GetMapping("/transaksi/export")
    public void exportTransaksi(HttpServletResponse response) throws IOException {
        log.info("[EXPORT] Admin mengexport laporan transaksi SELESAI ke CSV");
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=laporan-transaksi-selesai.csv");

        List<Transaksi> daftar = transaksiRepo.findByStatus(Transaksi.StatusTransaksi.SELESAI);

        try (Writer writer = new OutputStreamWriter(response.getOutputStream());
             CSVWriter csvWriter = new CSVWriter(writer)) {

            String[] header = {"ID Transaksi", "Tanggal", "Nama Warga", "Detail Sampah",
                    "Berat (Kg)", "Total Poin", "Nama Petugas"};
            csvWriter.writeNext(header);

            for (Transaksi t : daftar) {
                String detailSampah = "-";
                if (t.getDetails() != null && !t.getDetails().isEmpty()) {
                    detailSampah = t.getDetails().stream()
                            .filter(d -> d.getKategoriSampah() != null)
                            .map(d -> d.getKategoriSampah().getNamaKategori()
                                    + " " + (d.getBeratFinal() != null ? d.getBeratFinal() : d.getBeratEstimasi()) + "kg")
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("-");
                }
                String[] row = {
                        String.valueOf(t.getIdTransaksi()),
                        t.getTanggalTransaksi() != null
                                ? t.getTanggalTransaksi().toString() : "-",
                        t.getWarga() != null ? t.getWarga().getNama() : "-",
                        detailSampah,
                        t.getTotalBerat() != null ? String.valueOf(t.getTotalBerat()) : "0",
                        t.getTotalPoin() != null ? String.valueOf(t.getTotalPoin().intValue()) : "0",
                        t.getStaff() != null ? t.getStaff().getNama() : "-"
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
        }
    }

    @GetMapping("/logs")
    public String halamanLog(Model model) {
        log.info("[ACCESS] Admin membuka Log Aktivitas");
        model.addAttribute("logList", logAktivitasService.getSemuaLog());
        model.addAttribute("totalLog", logAktivitasService.countTotal());
        return "log-view";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        log.info("[ACCESS] Admin membuka Leaderboard Warga");
        List<User> wargaList = userRepo.findByRoleOrderByTotalPointsDesc(User.Role.WARGA);

        Map<Long, Double> totalBeratMap = new HashMap<>();
        List<Transaksi> semuaTransaksi = transaksiRepo.findAll();
        for (Transaksi t : semuaTransaksi) {
            if (t.getWarga() != null && t.getTotalBerat() != null) {
                Long wargaId = t.getWarga().getIdWarga();
                totalBeratMap.put(wargaId, totalBeratMap.getOrDefault(wargaId, 0.0) + t.getTotalBerat());
            }
        }

        model.addAttribute("daftarLeaderboard", wargaList);
        model.addAttribute("totalBeratMap", totalBeratMap);
        return "leaderboard-view";
    }
}
