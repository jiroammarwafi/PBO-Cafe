package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Menu {
    private Integer idMenu;
    private String namaMenu;
    private Integer idKategori;
    private String namaKategori;
    private Double harga;
    private String statusMenu;
    
    
    // Constructors
    public Menu() {}
    
    public Menu(Integer idMenu, String namaMenu, Integer idKategori, String namaKategori,
                Double harga, String statusMenu) {
        this.idMenu = idMenu;
        this.namaMenu = namaMenu;
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
        this.harga = harga;
        this.statusMenu = statusMenu;
    }
    
    // Getters and Setters
    public Integer getIdMenu() { return idMenu; }
    public void setIdMenu(Integer idMenu) { this.idMenu = idMenu; }
    
    public String getNamaMenu() { return namaMenu; }
    public void setNamaMenu(String namaMenu) { this.namaMenu = namaMenu; }
    
    public Integer getIdKategori() { return idKategori; }
    public void setIdKategori(Integer idKategori) { this.idKategori = idKategori; }
    
    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }
    
    public Double getHarga() { return harga; }
    public void setHarga(Double harga) { this.harga = harga; }
    
    public String getStatusMenu() { return statusMenu; }
    public void setStatusMenu(String statusMenu) { this.statusMenu = statusMenu; }
    
    
    // Database operations
    public void save() {
        if (this.idMenu == null || this.idMenu == 0) {
            insert();
        } else {
            update();
        }
    }
    
    private void insert() {
        String sql = "INSERT INTO menu (nama_menu, id_kategori, harga, status_menu) " +
                    "VALUES (?, ?, ?, ?)";
        
        dbHelper.bukaKoneksi();
        int idMenu = dbHelper.insertQueryGetId(sql.replace("?, ?, ?, ?",
            "'" + this.namaMenu + "', " + this.idKategori + ", " + this.harga + ", '" +
            this.statusMenu + "'"));
        
        if (idMenu > 0) {
            this.idMenu = idMenu;
        }
    }
    
    private void update() {
        String sql = "UPDATE menu SET nama_menu = '" + this.namaMenu + "', " +
                    "id_kategori = " + this.idKategori + ", " +
                    "harga = " + this.harga + ", " +
                    "status_menu = '" + this.statusMenu + "' " +
                    "WHERE id_menu = " + this.idMenu;
        
        dbHelper.bukaKoneksi();
        dbHelper.executeQuery(sql);
    }
    
    public void delete() {
        if (this.idMenu == null || this.idMenu == 0) return;

        dbHelper.bukaKoneksi();

        // Count references in pesanan_detail
        String countSql = "SELECT COUNT(*) AS cnt FROM pesanan_detail WHERE id_menu = " + this.idMenu;
        ResultSet rs = dbHelper.selectQuery(countSql);
        try {
            int cnt = 0;
            if (rs != null && rs.next()) {
                cnt = rs.getInt("cnt");
            }

            if (cnt > 0) {
                // Provide a concise list (up to 3) of transactions that reference this menu
                String listSql = "SELECT p.id_pesanan, COALESCE(p.no_meja,'-') AS no_meja, COALESCE(p.nama_pelanggan,'-') AS nama_pelanggan, pd.qty " +
                                 "FROM pesanan p JOIN pesanan_detail pd ON p.id_pesanan = pd.id_pesanan " +
                                 "WHERE pd.id_menu = " + this.idMenu + " ORDER BY p.id_pesanan ASC LIMIT 3";
                ResultSet rs2 = dbHelper.selectQuery(listSql);
                StringBuilder details = new StringBuilder();
                int shown = 0;
                while (rs2 != null && rs2.next()) {
                    if (shown > 0) details.append("; ");
                    String rawPelanggan = rs2.getString("nama_pelanggan");
                    String pelangganDisplay = rawPelanggan;
                    if (rawPelanggan != null) {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^Pelanggan\\s*(\\d+)$").matcher(rawPelanggan.trim());
                        if (m.find()) {
                            pelangganDisplay = "Pelanggan [" + m.group(1) + "]";
                        }
                    }
                    details.append(String.format("Order #%d \u2014 Meja: %s \u2014 Pelanggan: %s \u2014 Qty: %d", rs2.getInt("id_pesanan"), rs2.getString("no_meja"), pelangganDisplay, rs2.getInt("qty")));
                    shown++;
                }

                String more = cnt > shown ? String.format(" (+%d lagi)", cnt - shown) : "";
                String msg;
                if (details.length() > 0) {
                    msg = "Tidak bisa dihapus — dipakai di: " + details.toString() + more;
                } else {
                    msg = "Tidak bisa dihapus — menu ini dipakai di " + cnt + " transaksi.";
                }
                throw new RuntimeException(msg);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String sql = "DELETE FROM menu WHERE id_menu = " + this.idMenu;
        dbHelper.executeQuery(sql);
    }
    
    public static List<Menu> getAllMenu() {
        List<Menu> menuList = new ArrayList<>();
        String sql = "SELECT m.id_menu, m.nama_menu, m.id_kategori, km.nama_kategori, " +
                    "m.harga, m.status_menu FROM menu m " +
                    "LEFT JOIN kategori_menu km ON m.id_kategori = km.id_kategori " +
                    "ORDER BY m.id_menu";
        
        dbHelper.bukaKoneksi();
        ResultSet rs = dbHelper.selectQuery(sql);
        
        try {
            while (rs.next()) {
                Menu menu = new Menu(
                    rs.getInt("id_menu"),
                    rs.getString("nama_menu"),
                    rs.getInt("id_kategori"),
                    rs.getString("nama_kategori"),
                    rs.getDouble("harga"),
                    rs.getString("status_menu")
                );
                menuList.add(menu);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all menu: " + e.getMessage());
        }
        
        return menuList;
    }
    
    public static List<Menu> searchMenu(String keyword) {
        List<Menu> menuList = new ArrayList<>();
        String sql = "SELECT m.id_menu, m.nama_menu, m.id_kategori, km.nama_kategori, " +
                    "m.harga, m.status_menu FROM menu m " +
                    "LEFT JOIN kategori_menu km ON m.id_kategori = km.id_kategori " +
                    "WHERE m.nama_menu ILIKE '%" + keyword + "%' " +
                    "OR km.nama_kategori ILIKE '%" + keyword + "%' " +
                    "ORDER BY m.id_menu";
        
        dbHelper.bukaKoneksi();
        ResultSet rs = dbHelper.selectQuery(sql);
        
        try {
            while (rs.next()) {
                Menu menu = new Menu(
                    rs.getInt("id_menu"),
                    rs.getString("nama_menu"),
                    rs.getInt("id_kategori"),
                    rs.getString("nama_kategori"),
                    rs.getDouble("harga"),
                    rs.getString("status_menu")
                );
                menuList.add(menu);
            }
        } catch (SQLException e) {
            System.err.println("Error searching menu: " + e.getMessage());
        }
        
        return menuList;
    }
    
    public static Menu getMenuById(int idMenu) {
        String sql = "SELECT m.id_menu, m.nama_menu, m.id_kategori, km.nama_kategori, " +
                    "m.harga, m.status_menu FROM menu m " +
                    "LEFT JOIN kategori_menu km ON m.id_kategori = km.id_kategori " +
                    "WHERE m.id_menu = " + idMenu;
        
        dbHelper.bukaKoneksi();
        ResultSet rs = dbHelper.selectQuery(sql);
        
        try {
            if (rs.next()) {
                Menu menu = new Menu(
                    rs.getInt("id_menu"),
                    rs.getString("nama_menu"),
                    rs.getInt("id_kategori"),
                    rs.getString("nama_kategori"),
                    rs.getDouble("harga"),
                    rs.getString("status_menu")
                );
                return menu;
            }
        } catch (SQLException e) {
            System.err.println("Error getting menu by id: " + e.getMessage());
        }
        
        return null;
    }
    
    public static List<String> getAllKategori() {
        List<String> kategoriList = new ArrayList<>();
        String sql = "SELECT nama_kategori FROM kategori_menu ORDER BY id_kategori";
        
        dbHelper.bukaKoneksi();
        ResultSet rs = dbHelper.selectQuery(sql);
        
        try {
            while (rs.next()) {
                kategoriList.add(rs.getString("nama_kategori"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all kategori: " + e.getMessage());
        }
        
        return kategoriList;
    }
    
    public static int getIdKategoriByNama(String namaKategori) {
        String sql = "SELECT id_kategori FROM kategori_menu WHERE nama_kategori = '" + namaKategori + "'";
        
        dbHelper.bukaKoneksi();
        ResultSet rs = dbHelper.selectQuery(sql);
        
        try {
            if (rs.next()) {
                return rs.getInt("id_kategori");
            }
        } catch (SQLException e) {
            System.err.println("Error getting kategori id: " + e.getMessage());
        }
        
        return 0;
    }

    // --- Usage helpers ---
    public static int getUsageCount(int idMenu) {
        dbHelper.bukaKoneksi();
        String sql = "SELECT COUNT(*) AS cnt FROM pesanan_detail WHERE id_menu = " + idMenu;
        ResultSet rs = dbHelper.selectQuery(sql);
        try {
            if (rs != null && rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.err.println("Error getting usage count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns concise usage summaries for a menu. If limit <= 0, returns all.
     * Each entry formatted: "#<id_pesanan> <no_meja>/<nama_pelanggan> x<qty>"
     */
    public static List<String> getUsageSummary(int idMenu, int limit) {
        List<String> list = new ArrayList<>();
        dbHelper.bukaKoneksi();
        String sql = "SELECT p.id_pesanan, COALESCE(p.no_meja,'-') AS no_meja, COALESCE(p.nama_pelanggan,'-') AS nama_pelanggan, pd.qty " +
                 "FROM pesanan p JOIN pesanan_detail pd ON p.id_pesanan = pd.id_pesanan " +
                 "WHERE pd.id_menu = " + idMenu + " ORDER BY p.id_pesanan ASC";
        if (limit > 0) sql += " LIMIT " + limit;

        ResultSet rs = dbHelper.selectQuery(sql);
        try {
            while (rs != null && rs.next()) {
                String rawPelanggan = rs.getString("nama_pelanggan");
                String pelangganDisplay = rawPelanggan;
                if (rawPelanggan != null) {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("^Pelanggan\\s*(\\d+)$").matcher(rawPelanggan.trim());
                    if (m.find()) {
                        pelangganDisplay = "Pelanggan [" + m.group(1) + "]";
                    }
                }
                String entry = String.format("Order #%d \u2014 Meja: %s \u2014 Pelanggan: %s \u2014 Qty: %d", rs.getInt("id_pesanan"), rs.getString("no_meja"), pelangganDisplay, rs.getInt("qty"));
                list.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("Error getting usage summary: " + e.getMessage());
        }
        return list;
    }
}