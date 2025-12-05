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
    private String deskripsi;
    
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
    
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    
    // Database operations
    public void save() {
        if (this.idMenu == null || this.idMenu == 0) {
            insert();
        } else {
            update();
        }
    }
    
    private void insert() {
        String sql = "INSERT INTO menu (nama_menu, id_kategori, harga, status_menu, deskripsi) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        dbHelper.bukaKoneksi();
        int idMenu = dbHelper.insertQueryGetId(sql.replace("?, ?, ?, ?, ?", 
            "'" + this.namaMenu + "', " + this.idKategori + ", " + this.harga + ", '" + 
            this.statusMenu + "', '" + (this.deskripsi != null ? this.deskripsi : "") + "'"));
        
        if (idMenu > 0) {
            this.idMenu = idMenu;
        }
    }
    
    private void update() {
        String sql = "UPDATE menu SET nama_menu = '" + this.namaMenu + "', " +
                    "id_kategori = " + this.idKategori + ", " +
                    "harga = " + this.harga + ", " +
                    "status_menu = '" + this.statusMenu + "', " +
                    "deskripsi = '" + (this.deskripsi != null ? this.deskripsi : "") + "' " +
                    "WHERE id_menu = " + this.idMenu;
        
        dbHelper.bukaKoneksi();
        dbHelper.executeQuery(sql);
    }
    
    public void delete() {
        String sql = "DELETE FROM menu WHERE id_menu = " + this.idMenu;
        dbHelper.bukaKoneksi();
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
                    "m.harga, m.status_menu, m.deskripsi FROM menu m " +
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
                menu.setDeskripsi(rs.getString("deskripsi"));
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
}