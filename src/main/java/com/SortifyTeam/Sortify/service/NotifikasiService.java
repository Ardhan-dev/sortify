package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.Notifikasi;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.repository.NotifikasiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotifikasiService {

    private final NotifikasiRepository notifRepo;

    public NotifikasiService(NotifikasiRepository notifRepo) {
        this.notifRepo = notifRepo;
    }

    @Transactional
    public void buatNotifikasi(User user, String pesan) {
        Notifikasi notif = new Notifikasi();
        notif.setUser(user);
        notif.setPesan(pesan);
        notif.setRead(false);
        notifRepo.save(notif);
    }

    public List<Notifikasi> getNotifikasiBelumDibaca(User user) {
        return notifRepo.findByUserAndIsReadOrderByCreatedAtDesc(user, false)
                .stream()
                .limit(10)
                .toList();
    }

    public List<Notifikasi> getSemuaNotifikasi(User user) {
        return notifRepo.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void tandaiDibaca(Long id) {
        Notifikasi notif = notifRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notifikasi tidak ditemukan: " + id));
        notif.setRead(true);
        notifRepo.save(notif);
    }

    @Transactional
    public void tandaiDibaca(List<Notifikasi> daftar) {
        for (Notifikasi n : daftar) {
            n.setRead(true);
        }
        notifRepo.saveAll(daftar);
    }

    @Transactional
    public void tandaiSemuaDibaca(User user) {
        List<Notifikasi> daftar = notifRepo.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
        for (Notifikasi n : daftar) {
            n.setRead(true);
        }
        notifRepo.saveAll(daftar);
    }

    public long countBelumDibaca(User user) {
        return notifRepo.countByUserAndIsRead(user, false);
    }
}
