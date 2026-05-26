package com.SortifyTeam.Sortify.dto;

import java.util.List;

public class DashboardChartData {

    private List<String> labelBulan;
    private List<Double> dataBeratPerBulan;
    private List<String> labelKategori;
    private List<Double> dataKategori;

    public List<String> getLabelBulan() { return labelBulan; }
    public void setLabelBulan(List<String> labelBulan) { this.labelBulan = labelBulan; }
    public List<Double> getDataBeratPerBulan() { return dataBeratPerBulan; }
    public void setDataBeratPerBulan(List<Double> dataBeratPerBulan) { this.dataBeratPerBulan = dataBeratPerBulan; }
    public List<String> getLabelKategori() { return labelKategori; }
    public void setLabelKategori(List<String> labelKategori) { this.labelKategori = labelKategori; }
    public List<Double> getDataKategori() { return dataKategori; }
    public void setDataKategori(List<Double> dataKategori) { this.dataKategori = dataKategori; }
}
