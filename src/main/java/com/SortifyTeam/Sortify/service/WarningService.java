package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.model.Warning;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.repository.WarningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WarningService {

    private final WarningRepository warningRepo;
    private final UserRepository userRepo;
    private final LogAktivitasService logAktivitasService;
    private final NotifikasiService notifikasiService;

    public WarningService(WarningRepository warningRepo,
                          UserRepository userRepo,
                          LogAktivitasService logAktivitasService,
                          NotifikasiService notifikasiService) {
        this.warningRepo = warningRepo;
        this.userRepo = userRepo;
        this.logAktivitasService = logAktivitasService;
        this.notifikasiService = notifikasiService;
    }

    @Transactional
    public void beriWarning(User targetUser, String issuedByUsername, String reason) {
        Warning warning = new Warning();
        warning.setTargetUser(targetUser);
        warning.setIssuedBy(issuedByUsername);
        warning.setReason(reason);
        warning.setType(Warning.WarningType.WARNING);
        warningRepo.save(warning);

        int warningCount = (int) warningRepo.countByTargetUser(targetUser);
        targetUser.setWarningCount(warningCount);
        userRepo.save(targetUser);

        logAktivitasService.catatAktivitas(issuedByUsername, "PETUGAS",
                "WARNING_USER",
                "Memberi peringatan ke " + targetUser.getUsername() + " — " + reason);

        notifikasiService.buatNotifikasi(targetUser,
                "Anda mendapat peringatan dari petugas. Alasan: " + reason
                + " (Peringatan ke-" + warningCount + "/3)");

        if (warningCount >= 3) {
            suspendUser(targetUser, issuedByUsername,
                    "Akun otomatis disuspend setelah 3 peringatan.");
        }
    }

    @Transactional
    public void suspendUser(User targetUser, String issuedByUsername, String reason) {
        targetUser.setAccountStatus(User.AccountStatus.SUSPENDED);
        userRepo.save(targetUser);

        Warning warning = new Warning();
        warning.setTargetUser(targetUser);
        warning.setIssuedBy(issuedByUsername);
        warning.setReason(reason);
        warning.setType(Warning.WarningType.SUSPEND);
        warningRepo.save(warning);

        logAktivitasService.catatAktivitas(issuedByUsername,
                targetUser.getAccountStatus() == User.AccountStatus.SUSPENDED ? "PETUGAS" : "ADMIN",
                "SUSPEND_USER",
                "Menonaktifkan akun " + targetUser.getUsername() + " — " + reason);

        notifikasiService.buatNotifikasi(targetUser,
                "Akun Anda telah dinonaktifkan. Alasan: " + reason
                + ". Silakan hubungi admin untuk pengajuan aktivasi kembali.");
    }

    @Transactional
    public void banUser(User targetUser, String issuedByUsername, String reason) {
        targetUser.setAccountStatus(User.AccountStatus.BANNED);
        userRepo.save(targetUser);

        Warning warning = new Warning();
        warning.setTargetUser(targetUser);
        warning.setIssuedBy(issuedByUsername);
        warning.setReason(reason);
        warning.setType(Warning.WarningType.BAN);
        warningRepo.save(warning);

        logAktivitasService.catatAktivitas(issuedByUsername, "ADMIN",
                "BAN_USER",
                "Memban akun " + targetUser.getUsername() + " — " + reason);
    }

    @Transactional
    public void activekanAkun(User targetUser, String issuedByUsername) {
        targetUser.setAccountStatus(User.AccountStatus.ACTIVE);
        targetUser.setWarningCount(0);
        userRepo.save(targetUser);

        logAktivitasService.catatAktivitas(issuedByUsername, "ADMIN",
                "ACTIVATE_USER",
                "Mengaktifkan kembali akun " + targetUser.getUsername());

        notifikasiService.buatNotifikasi(targetUser,
                "Akun Anda telah diaktifkan kembali oleh admin. Silakan login.");
    }

    public List<Warning> getWarningsByUser(User user) {
        return warningRepo.findByTargetUserOrderByCreatedAtDesc(user);
    }

    public List<Warning> getSemuaWarnings() {
        return warningRepo.findAllByOrderByCreatedAtDesc();
    }

    public long getWarningCount(User user) {
        return warningRepo.countByTargetUser(user);
    }
}
