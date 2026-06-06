package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.*;
import com.SortifyTeam.Sortify.service.*;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    private final WargaRepository wargaRepo;
    private final KategoriSampahRepository kategoriRepo;
    private final LogAktivitasService logAktivitasService;
    private final WarningService warningService;

    public AdminController(UserRepository userRepo,
                           RewardService rewardService,
                           TransaksiRepository transaksiRepo,
                           TransaksiDetailRepository transaksiDetailRepo,
                           PenukaranRewardRepository penukaranRepo,
                           WargaRepository wargaRepo,
                           KategoriSampahRepository kategoriRepo,
                           LogAktivitasService logAktivitasService,
                           WarningService warningService) {
        this.userRepo = userRepo;
        this.rewardService = rewardService;
        this.transaksiRepo = transaksiRepo;
        this.transaksiDetailRepo = transaksiDetailRepo;
        this.penukaranRepo = penukaranRepo;
        this.wargaRepo = wargaRepo;
        this.kategoriRepo = kategoriRepo;
        this.logAktivitasService = logAktivitasService;
        this.warningService = warningService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Admin {} sedang membuka halaman Dashboard Admin", auth.getName());
        long transaksiSelesai = transaksiRepo.countByStatus(Transaksi.StatusTransaksi.SELESAI);
        long transaksiPending = transaksiRepo.countByStatus(Transaksi.StatusTransaksi.PENDING);
        double totalBerat = transaksiRepo.sumTotalBerat();

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
        model.addAttribute("daftarKategori", kategoriRepo.findByIsActiveTrue());

        return "admin-dashboard";
    }

    @GetMapping("/monitoring")
    public String monitoringReward(Model model,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String namaWarga,
                                    @RequestParam(required = false) String namaBarang) {
        log.info("[ACCESS] Admin membuka halaman Monitoring Tukar Reward (filter: status={}, warga={}, barang={})", status, namaWarga, namaBarang);
        boolean hasFilter = (status != null && !status.isBlank())
                         || (namaWarga != null && !namaWarga.isBlank())
                         || (namaBarang != null && !namaBarang.isBlank());

        List<PenukaranReward> semuaPenukaran;
        if (hasFilter) {
            PenukaranReward.StatusPenukaran statusEnum = (status != null && !status.isBlank())
                    ? PenukaranReward.StatusPenukaran.valueOf(status) : null;
            semuaPenukaran = rewardService.getPenukaranByFilter(statusEnum, namaWarga, namaBarang);
        } else {
            semuaPenukaran = penukaranRepo.findAllByOrderByTanggalPenukaranDesc();
        }
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
        model.addAttribute("filteredCount", semuaPenukaran.size());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedNamaWarga", namaWarga);
        model.addAttribute("selectedNamaBarang", namaBarang);
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
    public String halamanLog(Model model,
                              @RequestParam(required = false) String username,
                              @RequestParam(required = false) String role,
                              @RequestParam(required = false) String aksi) {
        log.info("[ACCESS] Admin membuka Log Aktivitas (filter: username={}, role={}, aksi={})", username, role, aksi);
        boolean hasFilter = (username != null && !username.isBlank())
                         || (role != null && !role.isBlank())
                         || (aksi != null && !aksi.isBlank());
        List<LogAktivitas> logList;
        if (hasFilter) {
            logList = logAktivitasService.getLogByFilter(username, role, aksi);
        } else {
            logList = logAktivitasService.getSemuaLog();
        }
        model.addAttribute("logList", logList);
        model.addAttribute("totalLog", logAktivitasService.countTotal());
        model.addAttribute("filteredCount", logList.size());
        model.addAttribute("daftarAksi", logAktivitasService.getDistinctAksi());
        model.addAttribute("daftarRole", logAktivitasService.getDistinctRole());
        model.addAttribute("daftarUsername", logAktivitasService.getDistinctUsername());
        model.addAttribute("selectedUsername", username);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedAksi", aksi);
        return "log-view";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        log.info("[ACCESS] Admin membuka Leaderboard Warga");
        List<User> wargaList = userRepo.findByRoleOrderByTotalPointsDesc(User.Role.WARGA);

        Map<Long, Double> totalBeratMap = new HashMap<>();
        for (User u : wargaList) {
            if (u.getWarga() != null) {
                double berat = transaksiRepo.sumTotalBeratByWarga(u.getWarga().getIdWarga());
                if (berat > 0) totalBeratMap.put(u.getWarga().getIdWarga(), berat);
            }
        }

        model.addAttribute("daftarLeaderboard", wargaList);
        model.addAttribute("totalBeratMap", totalBeratMap);
        return "leaderboard-view";
    }

    @GetMapping("/warnings")
    public String halamanWarnings(Model model) {
        log.info("[ACCESS] Admin membuka halaman warnings");
        List<Warning> semua = warningService.getSemuaWarnings();
        List<User> wargaList = userRepo.findByRole(User.Role.WARGA);
        model.addAttribute("warningList", semua);
        model.addAttribute("daftarWarga", wargaList);
        return "admin-warnings";
    }

    @GetMapping("/user/warnings/{id}")
    public String detailWarningsUser(@PathVariable Long id, Model model) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + id));
        List<Warning> warnings = warningService.getWarningsByUser(user);
        model.addAttribute("targetUser", user);
        model.addAttribute("warningList", warnings);
        return "admin-user-warnings";
    }

    @PostMapping("/user/ban/{id}")
    public String banUser(Authentication auth, @PathVariable Long id,
                           @RequestParam("alasan") String alasan,
                           RedirectAttributes redirectAttributes) {
        if (alasan == null || alasan.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Alasan ban harus diisi.");
            return "redirect:/admin/warnings";
        }
        try {
            User target = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + id));
            warningService.banUser(target, auth.getName(), alasan);
            redirectAttributes.addFlashAttribute("success",
                    "Akun " + target.getFullName() + " berhasil diblokir.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal: " + e.getMessage());
        }
        return "redirect:/admin/warnings";
    }

    @PostMapping("/user/activate/{id}")
    public String aktifkanUser(Authentication auth, @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            User target = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + id));
            warningService.activekanAkun(target, auth.getName());
            redirectAttributes.addFlashAttribute("success",
                    "Akun " + target.getFullName() + " berhasil diaktifkan kembali.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal: " + e.getMessage());
        }
        return "redirect:/admin/warnings";
    }
}
