package com.SortifyTeam.Sortify.dto;

public class RegisterDTO {

    private String username;
    private String password;
    private String confirmPassword;
    private String namaDepan;
    private String namaBelakang;
    private String email;
    private String noTelepon;
    private String alamat;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getNamaDepan() { return namaDepan; }
    public void setNamaDepan(String namaDepan) { this.namaDepan = namaDepan; }
    public String getNamaBelakang() { return namaBelakang; }
    public void setNamaBelakang(String namaBelakang) { this.namaBelakang = namaBelakang; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
}

