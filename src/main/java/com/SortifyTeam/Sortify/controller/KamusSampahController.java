package com.SortifyTeam.Sortify.controller; // sesuaikan package kamu

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KamusSampahController {

    @GetMapping("/kamus-sampah")
    public String kamusSampah() {
        return "kamus-sampah"; // nama file HTML di folder templates/
    }
}