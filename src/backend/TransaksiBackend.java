package backend;

import backend.TransaksiBackend.Transaksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

public class TransaksiBackend {

    // --- MODEL: Transaksi ---
    public static class Transaksi {
        public int idTransaksi, idPesanan, idMember;
        public String waktu, metode, nomor;
        public double totalBelanja, diskon, pajak, totalAkhir, nominalBayar, kembalian;
    }

    // --- MODEL: Pesanan ---
    public static class Pesanan {
        private int idOrder, noMeja;
        public int getIdOrder() { return idOrder; }
        public void setIdOrder(int id) { this.idOrder = id; }
        public int getNoMeja() { return noMeja; }
        public void setNoMeja(int no) { this.noMeja = no; }
    }

    // --- CONTROLLER: TransaksiController ---
    public static class TransaksiController {
        public static int getLastId() {
            int id = 0;
            String q = "SELECT MAX(id_transaksi) AS last_id FROM transaksi";
            try (ResultSet rs = dbHelper.selectQuery(q)) {
                if (rs != null && rs.next()) {
                    id = rs.getInt("last_id");
                }
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
            return id;
        }
        
        public static int insert(Transaksi t) {
        // 1. Tangani ID Member: Jika 0 (non-member), kirim kata NULL tanpa kutip
        String idMemberValue = (t.idMember > 0) ? String.valueOf(t.idMember) : "NULL";

        // 2. Tangani Waktu: Jika kosong, gunakan waktu sistem
        String waktuValue;
        if (t.waktu == null || t.waktu.trim().isEmpty() || t.waktu.equalsIgnoreCase("null")) {
            waktuValue = "CURRENT_TIMESTAMP";
        } else {
            waktuValue = "CAST('" + t.waktu + "' AS TIMESTAMP)";
        }

        // 3. Tangani Nomor Kartu/E-Wallet: Jika kosong, kirim NULL tanpa kutip
        // Ini sering jadi penyebab error jika kolom di DB bertipe Integer/BigInt
        String nomorValue = (t.nomor == null || t.nomor.trim().isEmpty() || t.nomor.equalsIgnoreCase("null")) 
                            ? "NULL" 
                            : "'" + t.nomor + "'";

        // 4. Susun Query (Perhatikan: %s digunakan untuk yang bisa bernilai NULL agar tidak ada tanda kutip manual)
        String query = String.format(Locale.US,
            "INSERT INTO transaksi (id_pesanan, id_member, waktu_transaksi, total_belanja, diskon, pajak, total_akhir, metode_pembayaran, nominal_bayar, kembalian, nomor_kartu_ewallet) " +
            "VALUES (%d, %s, %s, %.2f, %.2f, %.2f, %.2f, '%s', %.2f, %.2f, %s)",
            t.idPesanan, 
            idMemberValue, 
            waktuValue, 
            t.totalBelanja, t.diskon, t.pajak, t.totalAkhir, 
            t.metode, t.nominalBayar, t.kembalian, 
            nomorValue // Tidak pakai tanda kutip di sini karena sudah dihandle di atas
        );

        System.out.println("Executing Query: " + query); // Untuk debug di console
        return dbHelper.insertQueryGetId(query);
    }
    }

    // --- CONTROLLER: PesananController ---
    public static class PesananController {
        public static ArrayList<Pesanan> getNomorMejaTerakhir() {
            ArrayList<Pesanan> list = new ArrayList<>();
            // Gunakan spasi yang benar pada query
            String q = "SELECT id_pesanan, no_meja " +
               "FROM pesanan " +
               "WHERE status_bayar = 0 " +
               "ORDER BY waktu_pesan DESC";
            
            try (ResultSet rs = dbHelper.selectQuery(q)) {
                if (rs != null) {
                    while (rs.next()) {
                        Pesanan p = new Pesanan();
                        p.setIdOrder(rs.getInt("id_pesanan"));
                        p.setNoMeja(rs.getInt("no_meja"));
                        list.add(p);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        public static double getTotalBelanja(int idOrder) {
            double total = 0;
            // Gunakan alias 'total' dan pastikan nama tabel benar
            String q = "SELECT SUM(subtotal) AS total FROM pesanan_detail WHERE id_pesanan = " + idOrder;
            try (ResultSet rs = dbHelper.selectQuery(q)) {
                if (rs != null && rs.next()) {
                    total = rs.getDouble("total");
                }
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
            return total;
        }
        
        public static void updateStatusBayar(int idOrder) {
            String q = "UPDATE pesanan SET status_bayar = 1 WHERE id_pesanan = " + idOrder;
            dbHelper.executeQuery(q);
        }
    }

    // --- CONTROLLER: MemberController ---
    public static class MemberController {
        public static int getIdMemberByNama(String nama) {
            int id = 0;
            String q = "SELECT id_member FROM member WHERE nama_member ILIKE '" + nama + "'";
            try (ResultSet rs = dbHelper.selectQuery(q)) {
                if (rs != null && rs.next()) id = rs.getInt("id_member");
            } catch (Exception e) { e.printStackTrace(); }
            return id;
        }

        public static void updatePoinMember(int idMember, int tambahPoin) {
            try {
                // Query untuk menambah poin yang sudah ada
                String sql = "UPDATE member SET points = points + " + tambahPoin + " WHERE id_member = " + idMember;
                dbHelper.executeQuery(sql);
                System.out.println("Poin member berhasil bertambah!");
            } catch (Exception e) {
                System.out.println("Gagal update poin: " + e.getMessage());
            }
        }
    }
}