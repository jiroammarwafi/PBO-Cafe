/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Backend for Laporan Penjualan
 * Handles all database operations for sales report
 * @author luvma
 */
public class LaporanBackend {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    // ============================================
    // LAPORAN ITEM MODEL
    // ============================================
    public static class LaporanItem {
        public int idTransaksi;
        public String tanggal;
        public String namaMenu;
        public int qty;
        public double subtotal;
        public String metodePembayaran;

        public LaporanItem(int idTransaksi, String tanggal, String namaMenu, int qty, double subtotal, String metodePembayaran) {
            this.idTransaksi = idTransaksi;
            this.tanggal = tanggal;
            this.namaMenu = namaMenu;
            this.qty = qty;
            this.subtotal = subtotal;
            this.metodePembayaran = metodePembayaran;
        }
    }

    // ============================================
    // LAPORAN SUMMARY MODEL
    // ============================================
    public static class LaporanSummary {
        public ArrayList<LaporanItem> items;
        public double totalPendapatan;
        public int totalTransaksi;

        public LaporanSummary(ArrayList<LaporanItem> items, double totalPendapatan, int totalTransaksi) {
            this.items = items;
            this.totalPendapatan = totalPendapatan;
            this.totalTransaksi = totalTransaksi;
        }
    }

    // ============================================
    // LAPORAN CONTROLLER (DATABASE OPERATIONS)
    // ============================================
    public static class LaporanController {

        /**
         * Fetch all laporan data
         */
        public static LaporanSummary getAllLaporan() {
            String sql = "SELECT * FROM v_laporan ORDER BY tanggal DESC, id_transaksi";
            return getLaporanFromQuery(sql);
        }

        /**
         * Fetch laporan by date range
         */
        public static LaporanSummary getLaporanByDateRange(Date startDate, Date endDate) {
            if (startDate == null || endDate == null) {
                return null;
            }

            if (startDate.after(endDate)) {
                return null;
            }

            String startDateStr = dateFormat.format(startDate);
            String endDateStr = dateFormat.format(endDate);
            String sql = "SELECT * FROM v_laporan WHERE tanggal BETWEEN '" + startDateStr + "' AND '" + endDateStr + "' ORDER BY tanggal DESC, id_transaksi";
            return getLaporanFromQuery(sql);
        }

        /**
         * Internal method to fetch data from database and calculate summary
         */
        private static LaporanSummary getLaporanFromQuery(String sql) {
            ArrayList<LaporanItem> items = new ArrayList<>();
            double totalPendapatan = 0;
            int totalTransaksi = 0;

            try {
                ResultSet rs = dbHelper.selectQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        int idTransaksi = rs.getInt("id_transaksi");
                        String tanggal = rs.getDate("tanggal").toString();
                        String namaMenu = rs.getString("nama_menu");
                        int qty = rs.getInt("qty");
                        double subtotal = rs.getDouble("subtotal");
                        String metodePembayaran = rs.getString("metode_pembayaran");

                        LaporanItem item = new LaporanItem(idTransaksi, tanggal, namaMenu, qty, subtotal, metodePembayaran);
                        items.add(item);
                        totalPendapatan += subtotal;
                    }
                    rs.close();

                    // Count unique transactions
                    totalTransaksi = countUniqueTransactions(items);

                    return new LaporanSummary(items, totalPendapatan, totalTransaksi);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new LaporanSummary(new ArrayList<>(), 0, 0);
        }

        /**
         * Count unique transaction IDs in laporan items
         */
        private static int countUniqueTransactions(ArrayList<LaporanItem> items) {
            ArrayList<Integer> uniqueIds = new ArrayList<>();
            for (LaporanItem item : items) {
                if (!uniqueIds.contains(item.idTransaksi)) {
                    uniqueIds.add(item.idTransaksi);
                }
            }
            return uniqueIds.size();
        }
    }
}
