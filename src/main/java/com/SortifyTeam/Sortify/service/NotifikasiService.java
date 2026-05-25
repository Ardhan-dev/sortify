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
    public void buatNotifikasi(User warga, String pesan) {
        Notifikasi notif = new Notifikasi();
        notif.setWarga(warga);
        notif.setPesan(pesan);
        notif.setRead(false);
        notifRepo.save(notif);
    }

    public List<Notifikasi> getNotifikasiBelumDibaca(User warga) {
        return notifRepo.findByWargaAndIsReadOrderByCreatedAtDesc(warga, false);
    }

    public List<Notifikasi> getSemuaNotifikasi(User warga) {
        return notifRepo.findByWargaOrderByCreatedAtDesc(warga);
    }

    @Transactional
    public void tandaiDibaca(Long id) {
        Notifikasi notif = notifRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notifikasi tidak ditemukan: " + id));
        notif.setRead(true);
        notifRepo.save(notif);
    }

    @Transactional
    public void tandaiSemuaDibaca(User warga) {
        List<Notifikasi> daftar = notifRepo.findByWargaAndIsReadOrderByCreatedAtDesc(warga, false);
        for (Notifikasi n : daftar) {
            n.setRead(true);
        }
        notifRepo.saveAll(daftar);
    }

    public long countBelumDibaca(User warga) {
        return notifRepo.countByWargaAndIsRead(warga, false);
    }
}
