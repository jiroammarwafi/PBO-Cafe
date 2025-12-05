package backend;

import java.sql.ResultSet;
import java.util.ArrayList;
import backend.dbHelper;

public class Kategori {

    // Atribut
    private int idkategori;
    private String nama;

    // Konstruktor
    public Kategori() {
    }

    public Kategori(String nama) {
        this.nama = nama;
    }

    // Getter dan Setter
    public int getIdkategori() {
        return idkategori;
    }

    public void setIdkategori(int idkategori) {
        this.idkategori = idkategori;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    // Ambil 1 kategori berdasarkan id
    public Kategori getById(int id) {
        Kategori kat = new Kategori();
        ResultSet rs = dbHelper.selectQuery(
                "SELECT * FROM kategori_menu WHERE id_kategori = '" + id + "'"
        );
        try {
            while (rs.next()) {
                kat = new Kategori();
                kat.setIdkategori(rs.getInt("id_kategori"));
                kat.setNama(rs.getString("nama_kategori"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kat;
    }

    // Ambil semua kategori
    public ArrayList<Kategori> getAll() {
        ArrayList<Kategori> listKategori = new ArrayList<>();
        ResultSet rs = dbHelper.selectQuery("SELECT * FROM kategori_menu");
        try {
            while (rs.next()) {
                Kategori kat = new Kategori();
                kat.setIdkategori(rs.getInt("id_kategori"));
                kat.setNama(rs.getString("nama_kategori"));
                listKategori.add(kat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listKategori;
    }

    // Simpan (INSERT / UPDATE)
    public void save() {
        if (getById(idkategori).getIdkategori() == 0) {
            String SQL = "INSERT INTO kategori_menu (nama_kategori) VALUES ("
                    + " '" + this.nama + "'"
                    + " )";
            this.idkategori = dbHelper.insertQueryGetId(SQL);
        } else {
            String SQL = "UPDATE kategori_menu SET "
                    + " nama_kategori = '" + this.nama + "' "
                    + " WHERE id_kategori = '" + this.idkategori + "'";
            dbHelper.executeQuery(SQL);
        }
    }

    // Hapus
    public void delete() {
        String SQL = "DELETE FROM kategori_menu WHERE id_kategori = '" + this.idkategori + "'";
        dbHelper.executeQuery(SQL);
    }
}