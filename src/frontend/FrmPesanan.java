// ...existing code...
package frontend;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import backend.dbHelper;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FrmPesanan extends JFrame {

    private JTextField txtCariMenu, txtIdPesanan;
    private JComboBox<String> cbMeja;
    private JTable tblMenu, tblCart, tblRiwayat, tblRiwayatDetail;
    private DefaultTableModel modelMenu, modelCart, modelRiwayat, modelRiwayatDetail;
    private JLabel lblSubtotal;
    private JButton btnCari, btnSimpan, btnDetail, btnHapus;
    private JButton btnKembali;
    private JButton btnExtra;

    private Timer cartUpdateTimer;

    public FrmPesanan() {
        setTitle("Form Pemesanan");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel lblMeja = new JLabel("No Meja :");
        lblMeja.setBounds(20, 20, 80, 25);
        add(lblMeja);
        cbMeja = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
        cbMeja.setBounds(100, 20, 100, 25);
        add(cbMeja);

        JLabel lblCari = new JLabel("Cari Menu :");
        lblCari.setBounds(20, 60, 80, 25);
        add(lblCari);
        txtCariMenu = new JTextField();
        txtCariMenu.setBounds(100, 60, 200, 25);
        add(txtCariMenu);
        btnCari = new JButton("Cari");
        btnCari.setBounds(310, 60, 70, 25);
        add(btnCari);

        modelMenu = new DefaultTableModel(new Object[]{"Nama", "Harga", "Qty"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
            }
        };

        tblMenu = new JTable(modelMenu);
        JScrollPane menuPane = new JScrollPane(tblMenu);
        menuPane.setBounds(20, 110, 380, 200);
        menuPane.setBorder(BorderFactory.createTitledBorder("Daftar Menu : (edit QTY)"));
        add(menuPane);

        lblSubtotal = new JLabel("Subtotal : Rp 0");
        lblSubtotal.setBounds(430, 320, 300, 25);
        add(lblSubtotal);

        modelCart = new DefaultTableModel(new String[]{"Nama Menu", "Qty", "Total"}, 0);
        tblCart = new JTable(modelCart);
        JScrollPane cartPane = new JScrollPane(tblCart);
        cartPane.setBounds(430, 110, 430, 200);
        cartPane.setBorder(BorderFactory.createTitledBorder("List Detail Pesanan"));
        add(cartPane);

        btnSimpan = new JButton("Simpan");
        btnSimpan.setBounds(760, 320, 100, 25);
        add(btnSimpan);

        modelRiwayat = new DefaultTableModel(new String[]{"ID", "No Meja", "Subtotal"}, 0);
        tblRiwayat = new JTable(modelRiwayat);
        JScrollPane historyPane = new JScrollPane(tblRiwayat);
        historyPane.setBounds(20, 360, 840, 200);
        historyPane.setBorder(BorderFactory.createTitledBorder("List Riwayat Pesanan"));
        add(historyPane);

        modelRiwayatDetail = new DefaultTableModel(new String[]{"ID Pesanan", "Nama Menu", "Harga","Qty", "Subtotal"}, 0);
        tblRiwayatDetail = new JTable(modelRiwayatDetail);
        JScrollPane historyDetilPane = new JScrollPane(tblRiwayatDetail);
        historyDetilPane.setBounds(20, 360, 840, 200);
        historyDetilPane.setBorder(BorderFactory.createTitledBorder("List Riwayat Detail Pesanan"));
        add(historyDetilPane);
        historyDetilPane.setVisible(false);

        JLabel lblId = new JLabel("Masukkan ID :");
        lblId.setBounds(20, 570, 100, 25);
        add(lblId);

        txtIdPesanan = new JTextField();
        txtIdPesanan.setBounds(120, 570, 120, 25);
        add(txtIdPesanan);

        btnDetail = new JButton("Lihat Detail Pesanan");
        btnDetail.setBounds(260, 570, 160, 25);
        add(btnDetail);
        btnDetail.addActionListener(e -> {
            String id = txtIdPesanan.getText();

            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan ID pesanan untuk melihat detail!");
                return;
            }

            updateTableRiwayatDetail(Integer.parseInt(id));

            historyPane.setVisible(false);
            historyDetilPane.setVisible(true);
            btnDetail.setVisible(false);
            btnHapus.setVisible(false);
            btnKembali.setVisible(true);
        });

        btnHapus = new JButton("Hapus Data");
        btnHapus.setBounds(430, 570, 120, 25);

        btnKembali = new JButton("Kembali ke Riwayat");
        btnKembali.setBounds(560, 570, 180, 25);
        add(btnKembali);
        btnKembali.setVisible(false);

        btnKembali.addActionListener(e -> {
            historyPane.setVisible(true);
            historyDetilPane.setVisible(false);
            btnDetail.setVisible(true);
            btnHapus.setVisible(true);
            btnKembali.setVisible(false);
            modelRiwayatDetail.setRowCount(0);
        });
        add(btnHapus);
        btnHapus.addActionListener(e -> {
            String id = txtIdPesanan.getText();

            if(id.isEmpty()){
                JOptionPane.showMessageDialog(this, "Masukkan ID pesanan yang akan dihapus!");
                return;
            }

            int idInt = -1;
            try {
                idInt = Integer.parseInt(id);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID pesanan tidak valid!");
                return;
            }

            // Fitur: peringatan - tidak bisa dihapus jika pesanan sudah diproses (sudah dibayar). 
            // Cara kerja: memanggil TransaksiBackend.PesananController.isPesananProcessed(id); 
            // jika true tampilkan dialog dan batalkan penghapusan, jika false lanjutkan hapus.
            boolean processed = backend.TransaksiBackend.PesananController.isPesananProcessed(idInt);
            if (processed) {
                JOptionPane.showMessageDialog(this, "Pesanan sudah diproses di form transaksi, tidak dapat dihapus.");
                return;
            }

            String deleteDetail = "DELETE FROM pesanan_detail WHERE id_pesanan=" + idInt;
            String deletePesanan = "DELETE FROM pesanan WHERE id_pesanan=" + idInt;

            dbHelper.executeQuery(deleteDetail);
            dbHelper.executeQuery(deletePesanan);

            JOptionPane.showMessageDialog(this, "Pesanan berhasil dihapus!");

            loadDataPesanan();
        });

        btnExtra = new JButton("Bayar");
        btnExtra.setBounds(760, 570, 100, 25);
        add(btnExtra);
        btnExtra.addActionListener(e -> {
            btnBayarActionPerformed(e);
        });

        cartUpdateTimer = new Timer(300, ev -> updateCartImmediate());
        cartUpdateTimer.setRepeats(false);

        modelMenu.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    if (e.getColumn() == 2 || e.getColumn() == TableModelEvent.ALL_COLUMNS) {
                        cartUpdateTimer.restart();
                    }
                }
            }
        });

        btnCari.addActionListener(e -> doSearchMenu());
        btnSimpan.addActionListener(e -> doSaveOrderInBackground());

        loadMenu();
        loadDataPesanan();

        setVisible(true);
    }

    private void updateCartImmediate() {
        SwingUtilities.invokeLater(() -> updateCart());
    }

    private void doSearchMenu() {
        modelMenu.setRowCount(0);
        String keyword = txtCariMenu.getText().trim().replace("'", "''");
        String sql = "SELECT nama_menu, harga FROM menu WHERE nama_menu ILIKE '%" + keyword + "%'";
        ResultSet rs = null;
        try {
            rs = dbHelper.selectQuery(sql);
            while (rs != null && rs.next()) {
                modelMenu.addRow(new Object[]{
                        rs.getString("nama_menu"),
                        rs.getInt("harga"),
                        0
                });
            }
            if (rs != null) rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadMenu() {
        modelMenu.setRowCount(0);
        String sql = "SELECT nama_menu, harga FROM menu WHERE status_menu = 'Tersedia'";
        ResultSet rs = null;
        try {
            rs = dbHelper.selectQuery(sql);
            while (rs != null && rs.next()) {
                modelMenu.addRow(new Object[]{
                        rs.getString("nama_menu"),
                        rs.getInt("harga"),
                        0
                });
            }
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateCart() {
        modelCart.setRowCount(0);
        long subtotal = 0L;

        for (int i = 0; i < modelMenu.getRowCount(); i++) {
            Object qtyObj = modelMenu.getValueAt(i, 2);
            int qty = 0;
            try {
                qty = Integer.parseInt(qtyObj.toString());
            } catch (Exception ex) {
                continue;
            }
            if (qty > 0) {
                String nama = modelMenu.getValueAt(i, 0).toString();
                int harga = 0;
                try {
                    harga = Integer.parseInt(modelMenu.getValueAt(i, 1).toString());
                } catch (Exception ex) {
                    harga = 0;
                }
                int total = harga * qty;
                modelCart.addRow(new Object[]{nama, qty, total});
                subtotal += total;
            }
        }
        lblSubtotal.setText("Subtotal : Rp " + String.format("%,d", subtotal).replace(',', '.'));
    }

    private void updateTableRiwayatDetail(int idPesanan) {
        modelRiwayatDetail.setRowCount(0);
        String sql = "SELECT dp.id_pesanan, b.nama_menu, dp.qty, dp.harga, dp.subtotal\n" +
                "FROM pesanan_detail dp\n" +
                "INNER JOIN menu b\n" +
                "ON dp.id_menu = b.id_menu\n" +
                "WHERE dp.id_pesanan = " + idPesanan;
        ResultSet rs = null;
        try {
            rs = dbHelper.selectQuery(sql);
            while (rs != null && rs.next()) {
                modelRiwayatDetail.addRow(new Object[]{
                        rs.getInt("id_pesanan"),
                        rs.getString("nama_menu"),
                        rs.getInt("harga"),
                        rs.getInt("qty"),
                        rs.getInt("subtotal")
                });
            }
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void doSaveOrderInBackground() {
        String nama = "Tamu";
        updateCart();
        if (modelCart.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!");
            return;
        }

        btnSimpan.setEnabled(false);
        btnCari.setEnabled(false);
        txtCariMenu.setEnabled(false);
        tblMenu.setVisible(false);
        tblCart.setVisible(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private int generatedId = -1;
            private String errorMsg = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    String noMeja = cbMeja.getSelectedItem().toString();
                    String escNama = nama.replace("'", "''");

                    int subtotal = 0;

                    String sqlPesanan = "INSERT INTO pesanan (no_meja, nama_pelanggan, waktu_pesan) " +
                            "VALUES (" + noMeja + ", '" + escNama + "', NOW());";

                    generatedId = dbHelper.insertQueryGetId(sqlPesanan);
                    if (generatedId <= 0) {
                        errorMsg = "Gagal mendapatkan ID pesanan dari DB. (Pastikan tabel 'pesanan' memiliki auto-increment ID)";
                        return false;
                    }

                    for (int i = 0; i < modelCart.getRowCount(); i++) {
                        String namaMenu = modelCart.getValueAt(i, 0).toString().replace("'", "''");
                        int qty = Integer.parseInt(modelCart.getValueAt(i, 1).toString());

                        String sel = "SELECT id_menu, harga FROM menu WHERE nama_menu = '" + namaMenu + "' LIMIT 1";
                        ResultSet rs = dbHelper.selectQuery(sel);
                        int idMenu = 0;
                        int harga = 0;
                        if (rs != null && rs.next()) {
                            idMenu = rs.getInt("id_menu");
                            harga = rs.getInt("harga");
                        }
                        if (rs != null) try { rs.close(); } catch (Exception ex) {}

                        if (idMenu == 0) {
                            System.err.println("Menu tidak ditemukan: " + namaMenu);
                            continue;
                        }

                        subtotal += (harga * qty);

                        String sqlDetail = String.format(
                                "INSERT INTO pesanan_detail (id_pesanan, id_menu, qty, harga) VALUES (%d, %d, %d, %d);",
                                generatedId, idMenu, qty, harga);

                        int okDetail = dbHelper.insertQueryGetId(sqlDetail);
                        if (okDetail <= 0) {
                            errorMsg = "Gagal menyimpan detail pesanan. SQL: " + sqlDetail;
                            System.err.println(errorMsg);
                            return false;
                        }
                    }

                    return true;

                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorMsg = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                boolean success = false;
                try {
                    success = get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    success = false;
                }

                btnSimpan.setEnabled(true);
                btnCari.setEnabled(true);
                txtCariMenu.setEnabled(true);
                tblMenu.setVisible(true);
                tblCart.setVisible(true);

                if (success) {
                    JOptionPane.showMessageDialog(FrmPesanan.this, "Pesanan berhasil disimpan! ID: " + generatedId);
                    loadDataPesanan();
                    modelCart.setRowCount(0);
                    for (int r = 0; r < modelMenu.getRowCount(); r++) modelMenu.setValueAt(0, r, 2);
                    lblSubtotal.setText("Subtotal : Rp 0");
                } else {
                    JOptionPane.showMessageDialog(FrmPesanan.this, "Gagal menyimpan pesanan: " + (errorMsg != null ? errorMsg : "Unknown"));
                }
            }
        };

        worker.execute();
    }

    private void loadDataPesanan() {
        modelRiwayat.setRowCount(0);

        String sql = "SELECT p.id_pesanan, p.nama_pelanggan, p.no_meja, COALESCE(SUM(pd.subtotal), 0) as subtotal " +
                "FROM pesanan p LEFT JOIN pesanan_detail pd ON p.id_pesanan = pd.id_pesanan " +
                "GROUP BY p.id_pesanan, p.nama_pelanggan, p.no_meja " +
                "ORDER BY p.id_pesanan DESC";

        ResultSet rs = dbHelper.selectQuery(sql);

        try {
            while (rs != null && rs.next()) {
                modelRiwayat.addRow(new Object[]{
                        rs.getInt("id_pesanan"),
                        rs.getInt("no_meja"),
                        rs.getInt("subtotal")
                });
            }
            if (rs != null) rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void btnBayarActionPerformed(java.awt.event.ActionEvent evt) {
        String nama = "Tamu";

        FrmTransaksiPembayaran frm = new FrmTransaksiPembayaran(nama);
        frm.setVisible(true);

        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmPesanan());
    }
}
// ...existing code...