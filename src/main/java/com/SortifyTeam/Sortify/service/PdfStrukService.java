package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.Transaksi;
import com.SortifyTeam.Sortify.model.TransaksiDetail;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
public class PdfStrukService {

    public byte[] generateStruk(Transaksi transaksi) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A6, 20, 20, 20, 20);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0x2d, 0x6b, 0x2d));
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
            Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
            Font monoFont = new Font(Font.COURIER, 9, Font.NORMAL, Color.BLACK);
            Font smallFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);

            doc.add(new Paragraph("SORTIFY", titleFont));
            doc.add(new Paragraph("Bank Sampah Digital", smallFont));
            doc.add(new Paragraph("STRUK TRANSAKSI", headerFont));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("No. Transaksi: #" + transaksi.getIdTransaksi(), monoFont));
            String tgl = transaksi.getTanggalTransaksi() != null
                    ? transaksi.getTanggalTransaksi().toString() : "-";
            doc.add(new Paragraph("Tanggal: " + tgl, normalFont));

            String namaWarga = transaksi.getWarga() != null && transaksi.getWarga().getUser() != null
                    ? transaksi.getWarga().getUser().getFullName() : "-";
            doc.add(new Paragraph("Warga: " + namaWarga, normalFont));

            String namaPetugas = transaksi.getStaff() != null && transaksi.getStaff().getUser() != null
                    ? transaksi.getStaff().getUser().getFullName() : "-";
            if (transaksi.getStaff() != null) {
                doc.add(new Paragraph("Petugas: " + namaPetugas, normalFont));
            }
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 1});

            table.addCell(new Phrase("Jenis Sampah", headerFont));
            table.addCell(new Phrase("Berat", headerFont));
            table.addCell(new Phrase("Poin", headerFont));

            double totalBerat = 0;
            double totalPoin = 0;

            for (TransaksiDetail td : transaksi.getDetails()) {
                String nama = td.getKategoriSampah() != null ? td.getKategoriSampah().getNamaKategori() : "-";
                double berat = td.getBeratFinal() != null ? td.getBeratFinal() : (td.getBeratEstimasi() != null ? td.getBeratEstimasi() : 0);
                double poin = td.getSubTotalPoin() != null ? td.getSubTotalPoin() : 0;

                table.addCell(new Phrase(nama, normalFont));
                table.addCell(new Phrase(String.format("%.1f kg", berat), normalFont));
                table.addCell(new Phrase(String.format("%.0f", poin), normalFont));

                totalBerat += berat;
                totalPoin += poin;
            }

            doc.add(table);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Total Berat: " + String.format("%.1f kg", totalBerat), headerFont));
            doc.add(new Paragraph("Total Poin: " + String.format("%.0f", totalPoin), new Font(Font.HELVETICA, 11, Font.BOLD, new Color(0x2d, 0x6b, 0x2d))));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Terima kasih telah menjaga lingkungan!", smallFont));

        } catch (DocumentException e) {
            throw new RuntimeException("Gagal generate PDF: " + e.getMessage(), e);
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }
}
