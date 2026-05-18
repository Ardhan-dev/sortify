package com.SortifyTeam.Sortify.dto;

import com.SortifyTeam.Sortify.model.LaporanSampah;

public class LaporanSampahDTO {

    private Long id;
    private String namaWarga;
    private String jenisSampah;
    private double berat;
    private String alamatLengkap;
    private String catatan;
    private String status;
    private String createdAt;

    public static LaporanSampahDTO fromEntity(LaporanSampah l) {
        LaporanSampahDTO dto = new LaporanSampahDTO();
        dto.setId(l.getId());
        dto.setNamaWarga(l.getWarga().getFullName());
        dto.setJenisSampah(l.getJenisSampah().name());
        dto.setBerat(l.getBerat());
        dto.setAlamatLengkap(l.getAlamatLengkap());
        dto.setCatatan(l.getCatatan());
        dto.setStatus(l.getStatus().name());
        dto.setCreatedAt(l.getCreatedAt().toString());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNamaWarga() { return namaWarga; }
    public void setNamaWarga(String namaWarga) { this.namaWarga = namaWarga; }
    public String getJenisSampah() { return jenisSampah; }
    public void setJenisSampah(String jenisSampah) { this.jenisSampah = jenisSampah; }
    public double getBerat() { return berat; }
    public void setBerat(double berat) { this.berat = berat; }
    public String getAlamatLengkap() { return alamatLengkap; }
    public void setAlamatLengkap(String alamatLengkap) { this.alamatLengkap = alamatLengkap; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}


