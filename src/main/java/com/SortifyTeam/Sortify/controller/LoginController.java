package com.SortifyTeam.Sortify.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "Username atau password salah.");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "Kamu berhasil keluar.");
        }

        return "login"; // → templates/login.html
    }

    // ⚠️ TEMPORARY - hapus setelah selesai update password di DB
    @GetMapping("/dev/hash")
    @ResponseBody
    public String generateHash(@RequestParam String pw) {
        return passwordEncoder.encode(pw);
    }
}