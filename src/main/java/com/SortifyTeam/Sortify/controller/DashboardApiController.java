package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.dto.DashboardChartData;
import com.SortifyTeam.Sortify.repository.TransaksiDetailRepository;
import com.SortifyTeam.Sortify.repository.TransaksiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final TransaksiRepository transaksiRepo;
    private final TransaksiDetailRepository transaksiDetailRepo;

    public DashboardApiController(TransaksiRepository transaksiRepo,
                                   TransaksiDetailRepository transaksiDetailRepo) {
        this.transaksiRepo = transaksiRepo;
        this.transaksiDetailRepo = transaksiDetailRepo;
    }

    @GetMapping("/chart-data")
    public DashboardChartData getChartData(
            @RequestParam(required = false) Long wargaId,
            @RequestParam(required = false) Long kategoriId) {

        log.info("[API] Chart data requested — wargaId={}, kategoriId={}", wargaId, kategoriId);

        int currentYear = Year.now().getValue();
        List<String> labelBulan = Arrays.asList(
                "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agu", "Sep", "Okt", "Nov", "Des");

        boolean hasFilter = wargaId != null || kategoriId != null;

        // ── Monthly Bar Data ──
        List<Double> dataBeratPerBulan = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) dataBeratPerBulan.add(0.0);

        List<Object[]> monthlyData;
        if (hasFilter) {
            monthlyData = transaksiRepo.getMonthlyBeratFiltered(currentYear, wargaId, kategoriId);
        } else {
            monthlyData = transaksiRepo.getMonthlyBerat(currentYear);
        }
        for (Object[] row : monthlyData) {
            int month = ((Number) row[0]).intValue();
            double berat = ((Number) row[1]).doubleValue();
            dataBeratPerBulan.set(month - 1, berat);
        }

        // ── Category Pie Data ──
        List<String> labelKategori = new ArrayList<>();
        List<Double> dataKategori = new ArrayList<>();

        List<Object[]> kategoriData;
        if (hasFilter) {
            kategoriData = transaksiDetailRepo.getBeratPerKategoriFiltered(wargaId, kategoriId);
        } else {
            kategoriData = transaksiDetailRepo.getBeratPerKategori();
        }
        for (Object[] row : kategoriData) {
            labelKategori.add((String) row[0]);
            dataKategori.add(((Number) row[1]).doubleValue());
        }

        DashboardChartData result = new DashboardChartData();
        result.setLabelBulan(labelBulan);
        result.setDataBeratPerBulan(dataBeratPerBulan);
        result.setLabelKategori(labelKategori);
        result.setDataKategori(dataKategori);

        log.debug("[API] Response — monthly={}, categories={}",
                dataBeratPerBulan.stream().mapToDouble(Double::doubleValue).sum(),
                dataKategori.stream().mapToDouble(Double::doubleValue).sum());

        return result;
    }
}
