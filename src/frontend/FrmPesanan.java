package frontend;

import backend.Menu;
import backend.dbHelper;
import backend.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class FrmPesanan extends JFrame {

    private JTextField txtNama, txtMeja;
    private JTable tblMenu, tblDetail;
    private DefaultTableModel modelMenu, modelDetail;
    private JTextArea txtCatatanOrder;
    private JLabel lblTotal;
    private DAOPesanan daoPesanan = new DAOPesanan();
    private DAODetailPesanan daoDetail = new DAODetailPesanan();

    public FrmPesanan() {
        setTitle("Form Pesanan");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        // Top: header inputs
        JPanel pnlTop = new JPanel(new GridLayout(1,4,6,6));
        pnlTop.setBorder(BorderFactory.createTitledBorder("Data Pesanan"));
        txtNama = new JTextField();
        txtMeja = new JTextField();
        pnlTop.add(new JLabel("Nama Pelanggan:"));
        pnlTop.add(txtNama);
        pnlTop.add(new JLabel("Nomor Meja:"));
        pnlTop.add(txtMeja);
        add(pnlTop, BorderLayout.NORTH);

        // Center split: left menu, right detail
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.5);

        // Left = Menu list
        modelMenu = new DefaultTableModel(new Object[]{"ID", "Nama Menu", "Harga", "Qty"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3; // only qty editable
            }
        };
        tblMenu = new JTable(modelMenu);
        JScrollPane spMenu = new JScrollPane(tblMenu);
        spMenu.setBorder(BorderFactory.createTitledBorder("Daftar Menu (isi Qty)"));
        split.setLeftComponent(spMenu);

        // Right = Detail preview
        modelDetail = new DefaultTableModel(new Object[]{"ID Menu","Nama Menu","Harga","Qty","Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblDetail = new JTable(modelDetail);
        JScrollPane spDetail = new JScrollPane(tblDetail);
        spDetail.setBorder(BorderFactory.createTitledBorder("Preview Detail Pesanan"));
        split.setRightComponent(spDetail);

        add(split, BorderLayout.CENTER);

        // Bottom: catatan, actions, total
        JPanel pnlBottom = new JPanel(new BorderLayout(6,6));
        txtCatatanOrder = new JTextArea(3, 40);
        txtCatatanOrder.setLineWrap(true);
        pnlBottom.add(new JScrollPane(txtCatatanOrder), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel();
        JButton btnRefreshPreview = new JButton("Refresh Preview");
        JButton btnClear = new JButton("Reset");
        JButton btnSimpan = new JButton("Simpan Pesanan");
        pnlButtons.add(btnRefreshPreview);
        pnlButtons.add(btnClear);
        pnlButtons.add(btnSimpan);

        lblTotal = new JLabel("Total: Rp 0");
        pnlButtons.add(lblTotal);

        pnlBottom.add(pnlButtons, BorderLayout.SOUTH);

        add(pnlBottom, BorderLayout.SOUTH);

        // Load menu
        loadMenuData();

        // Actions
        btnRefreshPreview.addActionListener(e -> refreshPreviewFromMenu());
        btnClear.addActionListener(e -> resetForm());
        btnSimpan.addActionListener(e -> saveOrder());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadMenuData() {
        modelMenu.setRowCount(0);
        List<Menu> menus = Menu.getAllMenu();
        for (Menu m : menus) {
            // qty default 0
            modelMenu.addRow(new Object[]{m.getIdMenu(), m.getNamaMenu(), m.getHarga(), 0});
        }
    }

    // Build preview detail table from menu table qty > 0
    private void refreshPreviewFromMenu() {
        modelDetail.setRowCount(0);
        double total = 0;
        for (int i = 0; i < modelMenu.getRowCount(); i++) {
            Object qtyObj = modelMenu.getValueAt(i, 3);
            int qty = 0;
            try {
                qty = Integer.parseInt(qtyObj.toString());
            } catch (Exception ex) {
                qty = 0;
            }
            if (qty > 0) {
                int idMenu = (Integer) modelMenu.getValueAt(i, 0);
                String nama = modelMenu.getValueAt(i, 1).toString();
                double hargaD = Double.parseDouble(modelMenu.getValueAt(i, 2).toString());
                int harga = (int) Math.round(hargaD);
                int subtotal = harga * qty;
                modelDetail.addRow(new Object[]{idMenu, nama, harga, qty, subtotal});
                total += subtotal;
            }
        }
        lblTotal.setText("Total: Rp " + (long) total);
    }

    private void resetForm() {
        txtNama.setText("");
        txtMeja.setText("");
        txtCatatanOrder.setText("");
        loadMenuData();
        modelDetail.setRowCount(0);
        lblTotal.setText("Total: Rp 0");
    }

    private void saveOrder() {
        try {
            // 1. create header
            Pesanan psn = new Pesanan();
            String nama = txtNama.getText().trim();
            int meja = 0;
            try {
                meja = Integer.parseInt(txtMeja.getText().trim());
            } catch (Exception ex) {
                meja = 0;
            }
            psn.setNama(nama.isEmpty() ? null : nama);
            psn.setNoMeja(meja);
            psn.setTanggalPesan(LocalDate.now());
            psn.setWaktuPesan(LocalTime.now());

            daoPesanan.save(psn); // will set generated id to psn
            int idOrder = psn.getIdOrder();

            // 2. loop preview rows and insert detail
            for (int i = 0; i < modelDetail.getRowCount(); i++) {
                int idMenu = (Integer) modelDetail.getValueAt(i, 0);
                int harga = (Integer) modelDetail.getValueAt(i, 2);
                int qty = (Integer) modelDetail.getValueAt(i, 3);
                DetailPesanan dp = new DetailPesanan(0, idOrder, idMenu, harga, qty, null); // catatan per item unused here
                daoDetail.save(dp);
            }

            // 3. update catatan header (satu untuk seluruh order)
            String catatanOrder = txtCatatanOrder.getText().trim();
            if (!catatanOrder.isEmpty()) {
                daoPesanan.updateCatatanHeader(idOrder, catatanOrder);
            }

            JOptionPane.showMessageDialog(this, "Pesanan berhasil disimpan. ID: " + idOrder);
            resetForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan pesanan: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // main untuk test run form
    public static void main(String[] args) {
    java.awt.EventQueue.invokeLater(() -> {
        new FrmPesanan().setVisible(true);
    }); 
    }

}
