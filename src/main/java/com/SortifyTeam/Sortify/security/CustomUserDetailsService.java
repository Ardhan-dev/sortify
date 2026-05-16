package com.SortifyTeam.Sortify.security;

import com.SortifyTeam.Sortify.model.Staff;
import com.SortifyTeam.Sortify.model.Warga;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;
    private final WargaRepository wargaRepository;

    public CustomUserDetailsService(StaffRepository staffRepository,
                                    WargaRepository wargaRepository) {
        this.staffRepository = staffRepository;
        this.wargaRepository = wargaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Cari di tabel staff dulu (role ADMIN)
        Optional<Staff> staff = staffRepository.findByUsername(username);
        if (staff.isPresent()) {
            return new User(
                    staff.get().getUsername(),
                    staff.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        // Kalau tidak ada, cari di tabel warga (role WARGA)
        Optional<Warga> warga = wargaRepository.findByUsername(username);
        if (warga.isPresent()) {
            return new User(
                    warga.get().getUsername(),
                    warga.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_WARGA"))
            );
        }

        throw new UsernameNotFoundException("User tidak ditemukan: " + username);
    }
}