package com.SortifyTeam.Sortify.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RealtimeService {

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void kirimKeAdmin(String tujuan, Object payload) {
        messagingTemplate.convertAndSend("/topic/admin/" + tujuan, payload);
    }

    public void kirimKePetugas(String tujuan, Object payload) {
        messagingTemplate.convertAndSend("/topic/petugas/" + tujuan, payload);
    }

    public void kirimKeUser(String username, String queue, Object payload) {
        messagingTemplate.convertAndSendToUser(username, "/queue/" + queue, payload);
    }

    public void kirimNotifikasi(String username, String pesan) {
        kirimKeUser(username, "notifikasi", Map.of("pesan", pesan));
    }

    public void kirimPoinUpdate(String username, int totalPoin) {
        kirimKeUser(username, "poin", Map.of("totalPoin", totalPoin));
    }

    public void refreshAdminDashboard() {
        kirimKeAdmin("dashboard", Map.of("type", "REFRESH"));
    }

    public void notifikasiPetugasBaru(String pesan) {
        kirimKePetugas("notifikasi", Map.of("pesan", pesan));
    }

    public void refreshTransaksiPetugas() {
        kirimKePetugas("transaksi", Map.of("type", "REFRESH"));
    }
}
