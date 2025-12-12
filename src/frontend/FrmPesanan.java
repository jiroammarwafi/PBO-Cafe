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
import java.util.ArrayList;
import java.util.List;

public class FrmPesanan extends JFrame {

    private JTextField txtNama, txtCariMenu, txtIdPesanan;
    private JComboBox<String> cbMeja;
    private JTable tblMenu, tblCart, tblRiwayat, tblRiwayatDetail;
    private DefaultTableModel modelMenu, modelCart, modelRiwayat, modelRiwayatDetail;
    private JLabel lblSubtotal;
    private JButton btnCari, btnSimpan, btnDetail, btnHapus;
    private JButton btnKembali;

    // Debounce timer to prevent too frequent cart updates
    private Timer cartUpdateTimer;

    public FrmPesanan() {
        setTitle("Form Pemesanan");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // ================= INPUT ATAS =================
        JLabel lblNama = new JLabel("Nama :");
        lblNama.setBounds(20, 20, 80, 25);
        add(lblNama);
        txtNama = new JTextField();
        txtNama.setBounds(100, 20, 200, 25);
        add(txtNama);

        JLabel lblMeja = new JLabel("No Meja :");
        lblMeja.setBounds(350, 20, 80, 25);
        add(lblMeja);
        cbMeja = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
        cbMeja.setBounds(430, 20, 100, 25);
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

        // ================= TABEL MENU =================
        modelMenu = new DefaultTableModel(new Object[]{"Nama", "Harga", "Qty"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2; // only Qty editable
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                // store value and let listener handle changes via debounce
                super.setValueAt(aValue, row, column);
            }
        };

        tblMenu = new JTable(modelMenu);
        JScrollPane menuPane = new JScrollPane(tblMenu);
        menuPane.setBounds(20, 110, 380, 200);
        menuPane.setBorder(BorderFactory.createTitledBorder("Daftar Menu : (edit QTY)"));
        add(menuPane);

        // subtotal label (single instance)
        lblSubtotal = new JLabel("Subtotal : Rp 0");
        lblSubtotal.setBounds(430, 320, 300, 25);
        add(lblSubtotal);

        // ================= TABEL CART =================
        modelCart = new DefaultTableModel(new String[]{"Nama Menu", "Qty", "Total"}, 0);
        tblCart = new JTable(modelCart);
        JScrollPane cartPane = new JScrollPane(tblCart);
        cartPane.setBounds(430, 110, 430, 200);
        cartPane.setBorder(BorderFactory.createTitledBorder("List Detail Pesanan"));
        add(cartPane);

        // ================= Tombol Simpan =================
        btnSimpan = new JButton("Simpan");
        btnSimpan.setBounds(760, 320, 100, 25);
        add(btnSimpan);

        // ================= TABEL RIWAYAT PESANAN =================
        modelRiwayat = new DefaultTableModel(new String[]{"ID", "Nama Pelanggan", "No Meja", "Subtotal"}, 0);
        tblRiwayat = new JTable(modelRiwayat);
        JScrollPane historyPane = new JScrollPane(tblRiwayat);
        historyPane.setBounds(20, 360, 840, 200);
        historyPane.setBorder(BorderFactory.createTitledBorder("List Riwayat Pesanan"));
        add(historyPane);

        // ================= TABEL RIWAYAT DETAIL PESANAN ================= (dipindahkan)
        modelRiwayatDetail = new DefaultTableModel(new String[]{"ID Pesanan", "Nama Menu", "Harga","Qty", "Subtotal"}, 0);
        tblRiwayatDetail = new JTable(modelRiwayatDetail);
        JScrollPane historyDetilPane = new JScrollPane(tblRiwayatDetail);
        historyDetilPane.setBounds(20, 360, 840, 200); // Posisi sama dengan historyPane
        historyDetilPane.setBorder(BorderFactory.createTitledBorder("List Riwayat Detail Pesanan"));
        add(historyDetilPane);
        historyDetilPane.setVisible(false); // <--- INI PENTING: Sembunyikan secara default 

        // ================= DETAIL & HAPUS =================
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

            // 1. Update data detail
            updateTableRiwayatDetail(Integer.parseInt(id));
            
            // 2. Kontrol Visibilitas
            historyPane.setVisible(false);
            historyDetilPane.setVisible(true); // Tampilkan Detail
            btnDetail.setVisible(false);
            btnHapus.setVisible(false);
            btnKembali.setVisible(true); // Tampilkan tombol Kembali
        }); // Jangan ada kurung tutup atau semicolon ekstra di sini

        btnHapus = new JButton("Hapus Data");
        btnHapus.setBounds(430, 570, 120, 25);

        btnKembali = new JButton("Kembali ke Riwayat"); // <-- DEKLARASI TOMBOL BARU
        btnKembali.setBounds(560, 570, 180, 25);
        add(btnKembali);
        btnKembali.setVisible(false); // Sembunyikan saat awal

        // **Tambahkan ActionListener untuk Tombol Kembali**
        btnKembali.addActionListener(e -> {
            historyPane.setVisible(true); // Tampilkan Riwayat Utama
            historyDetilPane.setVisible(false); // Sembunyikan Riwayat Detail
            btnDetail.setVisible(true);
            btnHapus.setVisible(true);
            btnKembali.setVisible(false); // Sembunyikan tombol Kembali
            modelRiwayatDetail.setRowCount(0); // Optional: Kosongkan data detail
        });
        add(btnHapus);
        btnHapus.addActionListener(e -> {
            String id = txtIdPesanan.getText();

            if(id.isEmpty()){
                JOptionPane.showMessageDialog(this, "Masukkan ID pesanan yang akan dihapus!");
                return;
            }

            String deleteDetail = "DELETE FROM pesanan_detail WHERE id_pesanan=" + id;
            String deletePesanan = "DELETE FROM pesanan WHERE id_pesanan=" + id;

            dbHelper.executeQuery(deleteDetail);
            dbHelper.executeQuery(deletePesanan);

            JOptionPane.showMessageDialog(this, "Pesanan berhasil dihapus!");

            loadDataPesanan(); // Refresh tabel bawah
        });

        // ================= Debounce Timer =================
        cartUpdateTimer = new Timer(300, ev -> updateCartImmediate());
        cartUpdateTimer.setRepeats(false);

        // Table model listener: start debounce when Qty changed
        modelMenu.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    if (e.getColumn() == 2 || e.getColumn() == TableModelEvent.ALL_COLUMNS) {
                        // schedule the update (debounced)
                        cartUpdateTimer.restart();
                    }
                }
            }
        });

        // ================= Button Actions =================
        btnCari.addActionListener(e -> doSearchMenu());
        btnSimpan.addActionListener(e -> doSaveOrderInBackground());

        // initial load
        loadMenu();
        loadDataPesanan();

        setVisible(true);

        
    }

    // immediate update (called by Timer)
    private void updateCartImmediate() {
        SwingUtilities.invokeLater(() -> updateCart());
    }

    // search menu (safe-ish: escape single quotes)
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
        // build cart from modelMenu (only rows with qty > 0)
        modelCart.setRowCount(0);
        long subtotal = 0L;

        for (int i = 0; i < modelMenu.getRowCount(); i++) {
            Object qtyObj = modelMenu.getValueAt(i, 2);
            int qty = 0;
            try {
                qty = Integer.parseInt(qtyObj.toString());
            } catch (Exception ex) {
                // ignore invalid qty
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
        // build cart from modelMenu (only rows with qty > 0)
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
        // Validate quick
        String nama = txtNama.getText().trim();
        // pastikan cart ter-update sebelum validasi (hindari race dengan debounce)
        updateCart();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama pelanggan belum diisi!");
            return;
        }
        if (modelCart.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!");
            return;
        }

        // disable UI elements
        btnSimpan.setEnabled(false);
        btnCari.setEnabled(false);
        txtCariMenu.setEnabled(false);
        tblMenu.setVisible(false);
        tblCart.setVisible(false);
        
        // Create SwingWorker to do DB work off the EDT
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private int generatedId = -1;
            private String errorMsg = null;

            @Override
            protected Boolean doInBackground() {                    
                try {
                    String noMeja = cbMeja.getSelectedItem().toString();    
                    String escNama = nama.replace("'", "''");
                    
                    // Variabel untuk menyimpan total subtotal
                    int subtotal = 0; 
                    
                    // =========================================================
                    // LANGKAH 1: INSERT HEADER PESANAN (TANPA SUBTOTAL)
                    // Catatan: Subtotal akan di-UPDATE di Langkah 3
                    // =========================================================
                    String sqlPesanan = "INSERT INTO pesanan (no_meja, nama_pelanggan, waktu_pesan) " +
                            "VALUES (" + noMeja + ", '" + escNama + "', NOW());"; // Hapus koma berlebih

                    generatedId = dbHelper.insertQueryGetId(sqlPesanan);
                    if (generatedId <= 0) {
                        errorMsg = "Gagal mendapatkan ID pesanan dari DB. (Pastikan tabel 'pesanan' memiliki auto-increment ID)";
                        return false;
                    }
                    
                    // =========================================================
                    // LANGKAH 2: LOOP DETAIL DAN INSERT KE pesanan_detail
                    // =========================================================
                    for (int i = 0; i < modelCart.getRowCount(); i++) {
                        String namaMenu = modelCart.getValueAt(i, 0).toString().replace("'", "''");
                        int qty = Integer.parseInt(modelCart.getValueAt(i, 1).toString());

                        // ambil id_menu & harga dari tabel menu
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

                        subtotal += (harga * qty); // Akumulasi subtotal

                        // REVISI SQL DETAIL: Pastikan format string benar dan tanpa koma/spasi berlebih
                        String sqlDetail = String.format(
                            "INSERT INTO pesanan_detail (id_pesanan, id_menu, qty, harga) VALUES (%d, %d, %d, %d);",
                            generatedId, idMenu, qty, harga);
                            
                        // Ganti insertQueryGetId -> executeQuery untuk INSERT detail
                        int okDetail = dbHelper.insertQueryGetId(sqlDetail);
                        if (okDetail <= 0) {
                            errorMsg = "Gagal menyimpan detail pesanan. SQL: " + sqlDetail;
                            System.err.println(errorMsg);
                            return false;
                        }
                    }
                    
                    // =========================================================
                    // LANGKAH 3: UPDATE SUBTOTAL DI HEADER PESANAN
                    // =========================================================
                    sqlPesanan = "UPDATE pesanan SET subtotal = " + subtotal + " WHERE id_pesanan = " + generatedId;
                    
                    // PASTIKAN MENGGUNAKAN executeQuery() untuk UPDATE
                    boolean updateOK = dbHelper.executeQuery(sqlPesanan); 

                    if (!updateOK) {
                        // Ini penting: Jika update subtotal gagal, data pesanan (header) tidak lengkap.
                        System.err.println("Gagal UPDATE subtotal untuk ID: " + generatedId);
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

                // re-enable UI and refresh
                btnSimpan.setEnabled(true);
                btnCari.setEnabled(true);
                txtCariMenu.setEnabled(true);
                tblMenu.setVisible(true);
                tblCart.setVisible(true);

                if (success) {
                    JOptionPane.showMessageDialog(FrmPesanan.this, "Pesanan berhasil disimpan! ID: " + generatedId);
                    // refresh history & reset
                    loadDataPesanan();
                    txtNama.setText("");
                    modelCart.setRowCount(0);
                    // reset qty in menu
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

        String sql = "SELECT id_pesanan, nama_pelanggan, no_meja, "
                + " subtotal "
                + "FROM pesanan p ORDER BY p.id_pesanan DESC";

        ResultSet rs = dbHelper.selectQuery(sql);

        try {
            while (rs != null && rs.next()) {
                modelRiwayat.addRow(new Object[]{
                        rs.getInt("id_pesanan"),
                        rs.getString("nama_pelanggan"),
                        rs.getInt("no_meja"),
                        rs.getInt("subtotal")
                });
            }
            if (rs != null) rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // run on EDT
        SwingUtilities.invokeLater(() -> new FrmPesanan());
    }
}
// ...existing code...