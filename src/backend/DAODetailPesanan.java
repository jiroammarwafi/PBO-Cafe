package backend;

import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAODetailPesanan {

    // Ambil semua detail untuk 1 order
    public ArrayList<DetailPesanan> getAllByOrder(int idOrder) {
        ArrayList<DetailPesanan> listDetail = new ArrayList<>();
        String sql = "SELECT * FROM pesanan_detail WHERE id_pesanan = " + idOrder;
        ResultSet rs = dbHelper.selectQuery(sql);

        try {
            while (rs != null && rs.next()) {
                DetailPesanan detail = new DetailPesanan();
                detail.setIdDetail(rs.getInt("id_detail"));
                detail.setIdOrder(rs.getInt("id_pesanan"));
                detail.setIdMenu(rs.getInt("id_menu"));
                detail.setHarga(rs.getInt("harga"));
                detail.setQty(rs.getInt("qty"));
                detail.setSubtotal(rs.getInt("subtotal"));
                detail.setCatatan(rs.getString("catatan"));
                listDetail.add(detail);
            }
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listDetail;
    }

    // Simpan detail (insert / update)
    public void save(DetailPesanan d) {
        // hitung subtotal
        d.setSubtotal(d.getHarga() * d.getQty());

        if (d.getIdDetail() == 0) {
            String sql = "INSERT INTO pesanan_detail (id_pesanan, id_menu, harga, qty, subtotal, catatan) VALUES ("
                    + d.getIdOrder() + ", "
                    + d.getIdMenu() + ", "
                    + d.getHarga() + ", "
                    + d.getQty() + ", "
                    + d.getSubtotal() + ", "
                    + (d.getCatatan() != null ? "'" + d.getCatatan().replace("'", "''") + "'" : "NULL") + ")";
            d.setIdDetail(dbHelper.insertQueryGetId(sql));
        } else {
            String sql = "UPDATE pesanan_detail SET "
                    + "id_menu = " + d.getIdMenu() + ", "
                    + "harga = " + d.getHarga() + ", "
                    + "qty = " + d.getQty() + ", "
                    + "subtotal = " + d.getSubtotal() + ", "
                    + "catatan = " + (d.getCatatan() != null ? "'" + d.getCatatan().replace("'", "''") + "'" : "NULL")
                    + " WHERE id_detail = " + d.getIdDetail();
            dbHelper.executeQuery(sql);
        }
    }

    public void delete(DetailPesanan d) {
        String sql = "DELETE FROM pesanan_detail WHERE id_detail = " + d.getIdDetail();
        dbHelper.executeQuery(sql);
    }

    public DetailPesanan getById(int idDetail) {
        DetailPesanan d = new DetailPesanan();
        String sql = "SELECT * FROM pesanan_detail WHERE id_detail = " + idDetail;
        ResultSet rs = dbHelper.selectQuery(sql);

        try {
            if (rs != null && rs.next()) {
                d.setIdDetail(rs.getInt("id_detail"));
                d.setIdOrder(rs.getInt("id_pesanan"));
                d.setIdMenu(rs.getInt("id_menu"));
                d.setHarga(rs.getInt("harga"));
                d.setQty(rs.getInt("qty"));
                d.setSubtotal(rs.getInt("subtotal"));
                d.setCatatan(rs.getString("catatan"));
            }
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return d;
    }

    // edit qty by idDetail (helper)
    public void editQty(int idDetail, int newQty) {
        DetailPesanan d = getById(idDetail);
        if (d.getIdDetail() != 0) {
            d.setQty(newQty);
            save(d);
        }
    }
}
