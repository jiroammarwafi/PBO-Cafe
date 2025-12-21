package backend;

import backend.TransaksiBackend.Transaksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

public class TransaksiBackend {
    public static class Transaksi {
        public int idTransaksi, idPesanan, idMember;
        public String waktu, metode, nomor;
        public double totalBelanja, diskon, pajak, totalAkhir, nominalBayar, kembalian;
    }

    public static class Pesanan {
        private int idOrder, noMeja;
        public int getIdOrder() { 
            return idOrder; 
        }
        public void setIdOrder(int id) { 
            this.idOrder = id; 
        }
        public int getNoMeja() { 
            return noMeja; 
        }
        public void setNoMeja(int no) { 
            this.noMeja = no; 
        }
    }

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

            String idMemberValue = (t.idMember > 0) ? String.valueOf(t.idMember) : "NULL";

            String waktuValue;
            if (t.waktu == null || t.waktu.trim().isEmpty() || t.waktu.equalsIgnoreCase("null")) {
                waktuValue = "CURRENT_TIMESTAMP";
            } else {
                waktuValue = "CAST('" + t.waktu + "' AS TIMESTAMP)";
            }

            String nomorValue = (t.nomor == null || t.nomor.trim().isEmpty() || t.nomor.equalsIgnoreCase("null")) 
                                ? "NULL" 
                                : "'" + t.nomor + "'";

            String query = String.format(Locale.US,
                "INSERT INTO transaksi (id_pesanan, id_member, waktu_transaksi, total_belanja, diskon, pajak, total_akhir, metode_pembayaran, nominal_bayar, kembalian, nomor_kartu_ewallet) " +
                "VALUES (%d, %s, %s, %.2f, %.2f, %.2f, %.2f, '%s', %.2f, %.2f, %s)",
                t.idPesanan, 
                idMemberValue, 
                waktuValue, 
                t.totalBelanja, t.diskon, t.pajak, t.totalAkhir, 
                t.metode, t.nominalBayar, t.kembalian, 
                nomorValue 
            );

            System.out.println("Executing Query: " + query); 
            return dbHelper.insertQueryGetId(query);
        }
    }

    public static class PesananController {
        public static ArrayList<Pesanan> getNomorMejaTerakhir() {
            ArrayList<Pesanan> list = new ArrayList<>();
            
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

        public static boolean isPesananProcessed(int idOrder) {
            boolean processed = false;
            String q = "SELECT status_bayar FROM pesanan WHERE id_pesanan = " + idOrder;
            try (ResultSet rs = dbHelper.selectQuery(q)) {
                if (rs != null && rs.next()) {
                    processed = rs.getInt("status_bayar") == 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return processed;
        }
    }

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
                String sql = "UPDATE member SET points = points + " + tambahPoin + " WHERE id_member = " + idMember;
                dbHelper.executeQuery(sql);
                System.out.println("Poin member berhasil bertambah!");
            } catch (Exception e) {
                System.out.println("Gagal update poin: " + e.getMessage());
            }
        }
    }
}