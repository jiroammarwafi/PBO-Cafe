/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package backend;

/**
 *
 * @author Admin
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TransaksiBackend {

    // ============================================
    // CLASS MODEL TRANSAKSI
    // ============================================
    public static class Transaksi {
        public int idTransaksi;
        public int idPesanan;
        public int idMember;
        public String waktu;
        public double totalBelanja;
        public double diskon;
        public double pajak;
        public double totalAkhir;
        public String metode;
        public double nominalBayar;
        public double kembalian;
        public String nomor;
    }

    // ============================================
    // CONTROLLER TRANSAKSI (CRUD)
    // ============================================
    public static class TransaksiController {

        public static int insert(Transaksi t) {

            String q = "INSERT INTO transaksi (id_pesanan, id_member, waktu_transaksi, total_belanja, diskon, pajak, total_akhir, metode_pembayaran, nominal_bayar, kembalian, nomor) " +
                        "VALUES (" +
                        t.idPesanan + ", " +
                        (t.idMember == 0 ? "NULL" : t.idMember) + ", " +
                        "'" + t.waktu + "', " +
                        t.totalBelanja + ", " +
                        t.diskon + ", " +
                        t.pajak + ", " +
                        t.totalAkhir + ", " +
                        "'" + t.metode + "', " +
                        t.nominalBayar + ", " +
                        t.kembalian + ", " +
                        (t.nomor == null ? "NULL" : "'" + t.nomor + "'") +
                        ")";

            return dbHelper.insertQueryGetId(q);
        }
    }

    // ============================================
    // PESANAN (untuk mengambil total belanja)
    // ============================================
    public static class PesananController {

        public static double getTotalBelanja(int idPesanan) {

            double hasil = 0;

            String q = "SELECT total_bayar FROM pesanan WHERE id_pesanan = " + idPesanan;

            try {
                ResultSet rs = dbHelper.selectQuery(q);
                if (rs.next()) {
                    hasil = rs.getDouble("total_bayar");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return hasil;
        }

        public static ArrayList<Integer> getAllID() {

            ArrayList<Integer> list = new ArrayList<>();
            String q = "SELECT id_pesanan FROM pesanan";

            try {
                ResultSet rs = dbHelper.selectQuery(q);
                while (rs.next()) {
                    list.add(rs.getInt("id_pesanan"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return list;
        }
    }

    // ============================================
    // MEMBER (jika ingin input id_member via nama)
    // ============================================
    public static class MemberController {

        public static int getIdMemberByNama(String nama) {

            String q = "SELECT id_member FROM member WHERE nama_member = '" + nama + "'";

            try {
                ResultSet rs = dbHelper.selectQuery(q);
                if (rs.next()) {
                    return rs.getInt("id_member");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0; // jika tidak ada, dianggap non-member
        }
    }
}

