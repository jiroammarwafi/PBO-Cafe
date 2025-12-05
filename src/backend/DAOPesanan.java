package backend;

import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class DAOPesanan {

    // GET BY ID
    public Pesanan getById(int id) {
        Pesanan psn = null;
        String query = "SELECT id_pesanan, nama_pelanggan, no_meja, waktu_pesan, tanggal, catatan FROM pesanan "
                     + "WHERE id_pesanan = " + id;
        ResultSet rs = dbHelper.selectQuery(query);

        try {
            if (rs != null && rs.next()) {
                psn = new Pesanan();
                psn.setIdOrder(rs.getInt("id_pesanan"));
                psn.setNama(rs.getString("nama_pelanggan"));
                psn.setNoMeja(rs.getInt("no_meja"));
                java.sql.Date sd = rs.getDate("tanggal");
                java.sql.Time st = rs.getTime("waktu_pesan");
                if (sd != null) psn.setTanggalPesan(sd.toLocalDate());
                if (st != null) psn.setWaktuPesan(st.toLocalTime());
            }
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return psn;
    }

    // INSERT or UPDATE. Returns generated id via setIdOrder on object
    public void save(Pesanan p) {
        if (p.getIdOrder() == 0) {
            // INSERT
            String nama = (p.getNama() != null) ? "'" + p.getNama().replace("'", "''") + "'" : "NULL";
            String sql = "INSERT INTO pesanan (nama_pelanggan, no_meja, tanggal, waktu_pesan, catatan) VALUES ("
                    + nama + ", "
                    + p.getNoMeja() + ", "
                    + "'" + p.getTanggalPesan() + "', "
                    + "'" + p.getWaktuPesan() + "', "
                    + "NULL)"; // catatan header diisi later via update (we'll update after detail if needed)
            p.setIdOrder(dbHelper.insertQueryGetId(sql));
        } else {
            // UPDATE
            String nama = (p.getNama() != null) ? "'" + p.getNama().replace("'", "''") + "'" : "NULL";
            String sql = "UPDATE pesanan SET "
                    + "nama_pelanggan = " + nama + ", "
                    + "no_meja = " + p.getNoMeja() + ", "
                    + "tanggal = '" + p.getTanggalPesan() + "', "
                    + "waktu_pesan = '" + p.getWaktuPesan() + "' "
                    + "WHERE id_pesanan = " + p.getIdOrder();
            dbHelper.executeQuery(sql);
        }
    }

    // Update catatan header (satu catatan untuk seluruh pesanan)
    public void updateCatatanHeader(int idOrder, String catatan) {
        String c = (catatan != null) ? "'" + catatan.replace("'", "''") + "'" : "NULL";
        String sql = "UPDATE pesanan SET catatan = " + c + " WHERE id_pesanan = " + idOrder;
        dbHelper.executeQuery(sql);
    }

    // DELETE
    public void delete(Pesanan p) {
        String sql = "DELETE FROM pesanan WHERE id_pesanan = " + p.getIdOrder();
        dbHelper.executeQuery(sql);
    }

    // GET ALL
    public ArrayList<Pesanan> getAll() {
        ArrayList<Pesanan> listPesanan = new ArrayList<>();
        String sql = "SELECT id_pesanan, nama_pelanggan, no_meja, tanggal, waktu_pesan FROM pesanan ORDER BY id_pesanan DESC";
        ResultSet rs = dbHelper.selectQuery(sql);

        try {
            while (rs != null && rs.next()) {
                Pesanan psn = new Pesanan();
                psn.setIdOrder(rs.getInt("id_pesanan"));
                psn.setNama(rs.getString("nama_pelanggan"));
                psn.setNoMeja(rs.getInt("no_meja"));
                java.sql.Date sd = rs.getDate("tanggal");
                java.sql.Time st = rs.getTime("waktu_pesan");
                if (sd != null) psn.setTanggalPesan(sd.toLocalDate());
                if (st != null) psn.setWaktuPesan(st.toLocalTime());
                listPesanan.add(psn);
            }
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listPesanan;
    }

    // SEARCH (by nama_pelanggan or no_meja)
    public ArrayList<Pesanan> search(String keyword) {
        ArrayList<Pesanan> listPesanan = new ArrayList<>();
        String sql = "SELECT id_pesanan, nama_pelanggan, no_meja, tanggal, waktu_pesan FROM pesanan WHERE "
                + "LOWER(nama_pelanggan) LIKE LOWER('%" + keyword.replace("'", "''") + "%') OR "
                + "CAST(no_meja AS VARCHAR) LIKE '%" + keyword.replace("'", "''") + "%' ORDER BY id_pesanan DESC";
        ResultSet rs = dbHelper.selectQuery(sql);

        try {
            while (rs != null && rs.next()) {
                Pesanan psn = new Pesanan();
                psn.setIdOrder(rs.getInt("id_pesanan"));
                psn.setNama(rs.getString("nama_pelanggan"));
                psn.setNoMeja(rs.getInt("no_meja"));
                java.sql.Date sd = rs.getDate("tanggal");
                java.sql.Time st = rs.getTime("waktu_pesan");
                if (sd != null) psn.setTanggalPesan(sd.toLocalDate());
                if (st != null) psn.setWaktuPesan(st.toLocalTime());
                listPesanan.add(psn);
            }
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listPesanan;
    }
}
