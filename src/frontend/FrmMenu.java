package frontend;

import backend.Menu;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class FrmMenu extends JFrame {

    private static final String FONT_NAME = "Segoe UI";
    private static final String LABEL_ID_MENU = "ID Menu";
    private static final String LABEL_NAMA_MENU = "Nama Menu";
    private static final String LABEL_KATEGORI = "Kategori";
    private static final String LABEL_HARGA = "Harga";
    private static final String LABEL_STATUS = "Status Menu";
    private static final String[] COLUMN_NAMES = {"ID", LABEL_NAMA_MENU, LABEL_KATEGORI, LABEL_HARGA, LABEL_STATUS};

    private JTextField txtIdMenu;
    private JTextField txtNamaMenu;
    private JComboBox<String> cmbKategori;
    private JTextField txtHarga;
    private JComboBox<String> cmbStatus;
    private JTextField txtCari;
    
    private JButton btnTambah;
    private JButton btnEdit;
    private JButton btnHapus;
    private JButton btnRefresh;
    private JButton btnCari;
    
    private JTable tblMenu;
    private JScrollPane jScrollPane1;
    private DefaultTableModel tableModel;

    public FrmMenu() {
        // UI Manager
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("Table.showGrid", false);
        UIManager.put("Table.intercellSpacing", new Dimension(8, 8));

        // Initialize components
        JLabel lblId = new JLabel(LABEL_ID_MENU);
        JLabel lblNama = new JLabel(LABEL_NAMA_MENU);
        JLabel lblKategori = new JLabel(LABEL_KATEGORI);
        JLabel lblHarga = new JLabel(LABEL_HARGA);
        JLabel lblStatus = new JLabel(LABEL_STATUS);

        txtIdMenu = new JTextField();
        txtNamaMenu = new JTextField();
        txtHarga = new JTextField();
        cmbKategori = new JComboBox<>();
        cmbStatus = new JComboBox<>();
        txtCari = new JTextField();

        // Load data
        loadKategori();
        cmbStatus.addItem("Tersedia");
        cmbStatus.addItem("Habis");

        btnTambah = makeButton("Tambah");
        btnEdit = makeButton("Edit");
        btnHapus = makeButton("Hapus");
        btnRefresh = makeButton("Refresh");
        btnCari = makeButton("Cari");

        txtIdMenu.setText("0");
        txtIdMenu.setEnabled(false);

        // Table setup
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0);
        tblMenu = new JTable(tableModel);
        jScrollPane1 = new JScrollPane(tblMenu);

        // Layout
        setLayout(null);

        // Fonts
        Font fLabel = new Font(FONT_NAME, Font.PLAIN, 14);
        Font fText = new Font(FONT_NAME, Font.PLAIN, 14);
        Font fBtn = new Font(FONT_NAME, Font.BOLD, 14);

        lblId.setFont(fLabel);
        lblNama.setFont(fLabel);
        lblKategori.setFont(fLabel);
        lblHarga.setFont(fLabel);
        lblStatus.setFont(fLabel);

        txtIdMenu.setFont(fText);
        txtNamaMenu.setFont(fText);
        txtHarga.setFont(fText);
        cmbKategori.setFont(fText);
        cmbStatus.setFont(fText);
        txtCari.setFont(fText);

        btnTambah.setFont(fBtn);
        btnEdit.setFont(fBtn);
        btnHapus.setFont(fBtn);
        btnRefresh.setFont(fBtn);
        btnCari.setFont(fBtn);

        tblMenu.setFont(fText);
        tblMenu.setRowHeight(28);

        // Position form
        int xLabel = 30;
        int xField = 150;
        int wField = 300;
        int hField = 28;
        int gapY = 35;

        lblId.setBounds(xLabel, 30, 120, 25);
        txtIdMenu.setBounds(xField, 30, 80, hField);

        lblNama.setBounds(xLabel, 30 + gapY, 120, 25);
        txtNamaMenu.setBounds(xField, 30 + gapY, wField, hField);

        lblKategori.setBounds(xLabel, 30 + gapY * 2, 120, 25);
        cmbKategori.setBounds(xField, 30 + gapY * 2, wField, hField);

        lblHarga.setBounds(xLabel, 30 + gapY * 3, 120, 25);
        txtHarga.setBounds(xField, 30 + gapY * 3, wField, hField);

        lblStatus.setBounds(xLabel, 30 + gapY * 4, 120, 25);
        cmbStatus.setBounds(xField, 30 + gapY * 4, wField, hField);

        // Buttons position
        int startYTombol = 30 + gapY * 5 + 20;
        int tombolWidth = 100;
        int tombolHeight = 35;
        int gapXTombol = 15;

        btnTambah.setBounds(xLabel, startYTombol, tombolWidth, tombolHeight);
        btnEdit.setBounds(xLabel + tombolWidth + gapXTombol, startYTombol, tombolWidth, tombolHeight);
        btnHapus.setBounds(xLabel + (tombolWidth + gapXTombol) * 2, startYTombol, tombolWidth, tombolHeight);
        btnRefresh.setBounds(xLabel + (tombolWidth + gapXTombol) * 3, startYTombol, tombolWidth, tombolHeight);

        // Search position
        txtCari.setBounds(xLabel + (tombolWidth + gapXTombol) * 4, startYTombol, 120, tombolHeight);
        btnCari.setBounds(xLabel + (tombolWidth + gapXTombol) * 4 + 130, startYTombol, 70, tombolHeight);

        // Table position
        jScrollPane1.setBounds(30, startYTombol + tombolHeight + 20, 850, 300);

        // Add components
        add(lblId);
        add(txtIdMenu);
        add(lblNama);
        add(txtNamaMenu);
        add(lblKategori);
        add(cmbKategori);
        add(lblHarga);
        add(txtHarga);
        add(lblStatus);
        add(cmbStatus);
        add(btnTambah);
        add(btnEdit);
        add(btnHapus);
        add(btnRefresh);
        add(txtCari);
        add(btnCari);
        add(jScrollPane1);

        // Event handlers
        btnTambah.addActionListener(e -> tambahMenu());
        btnEdit.addActionListener(e -> editMenu());
        btnHapus.addActionListener(e -> hapusMenu());
        btnRefresh.addActionListener(e -> refreshData());
        btnCari.addActionListener(e -> cariMenu());
        
        tblMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    loadMenuToForm();
                }
            }
        });

        // Frame setup
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Manajemen Menu Makanan/Minuman");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setVisible(true);

        // Load initial data
        refreshData();
    }

    private JButton makeButton(String label) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setBorder(null);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadKategori() {
        cmbKategori.removeAllItems();
        List<String> kategoriList = Menu.getAllKategori();
        for (String kategori : kategoriList) {
            cmbKategori.addItem(kategori);
        }
    }

    private void refreshData() {
        // Refresh kategori dropdown agar selalu sinkron dengan database
        loadKategori();
        
        tableModel.setRowCount(0);
        List<Menu> menuList = Menu.getAllMenu();
        for (Menu menu : menuList) {
            Object[] row = {
                menu.getIdMenu(),
                menu.getNamaMenu(),
                menu.getNamaKategori(),
                String.format("%.2f", menu.getHarga()),
                menu.getStatusMenu()
            };
            tableModel.addRow(row);
        }
        clearForm();
    }

    private void cariMenu() {
        String keyword = txtCari.getText().trim();
        if (keyword.isEmpty()) {
            refreshData();
            return;
        }

        tableModel.setRowCount(0);
        List<Menu> menuList = Menu.searchMenu(keyword);
        for (Menu menu : menuList) {
            Object[] row = {
                menu.getIdMenu(),
                menu.getNamaMenu(),
                menu.getNamaKategori(),
                String.format("%.2f", menu.getHarga()),
                menu.getStatusMenu()
            };
            tableModel.addRow(row);
        }
    }

    private void tambahMenu() {
        if (txtNamaMenu.getText().trim().isEmpty() || 
            txtHarga.getText().trim().isEmpty() ||
            cmbKategori.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Mohon lengkapi semua field!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Menu menu = new Menu();
            menu.setNamaMenu(txtNamaMenu.getText().trim());
            menu.setIdKategori(Menu.getIdKategoriByNama((String) cmbKategori.getSelectedItem()));
            menu.setHarga(Double.parseDouble(txtHarga.getText().trim()));
            menu.setStatusMenu((String) cmbStatus.getSelectedItem());

            menu.save();
            JOptionPane.showMessageDialog(this, "Menu berhasil ditambahkan!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editMenu() {
        int selectedRow = tblMenu.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih menu yang ingin diubah!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (txtNamaMenu.getText().trim().isEmpty() || 
            txtHarga.getText().trim().isEmpty() ||
            cmbKategori.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Mohon lengkapi semua field!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idMenu = Integer.parseInt(txtIdMenu.getText());
            Menu menu = new Menu();
            menu.setIdMenu(idMenu);
            menu.setNamaMenu(txtNamaMenu.getText().trim());
            menu.setIdKategori(Menu.getIdKategoriByNama((String) cmbKategori.getSelectedItem()));
            menu.setHarga(Double.parseDouble(txtHarga.getText().trim()));
            menu.setStatusMenu((String) cmbStatus.getSelectedItem());

            menu.save();
            JOptionPane.showMessageDialog(this, "Menu berhasil diubah!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusMenu() {
        int selectedRow = tblMenu.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih menu yang ingin dihapus!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus menu ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idMenu = Integer.parseInt(txtIdMenu.getText());

                int cnt = Menu.getUsageCount(idMenu);
                if (cnt > 0) {
                    // show concise list (max 4) and two buttons: Lihat Semua, Tutup
                    java.util.List<String> sample = Menu.getUsageSummary(idMenu, 4);
                    StringBuilder msg = new StringBuilder();
                    msg.append("Menu ini sudah dipakai di ").append(cnt).append(" transaksi.\n\nContoh transaksi:\n");
                    for (String s : sample) msg.append(" - ").append(s).append("\n");

                    Object[] options = {"Lihat Semua", "Tutup"};
                    int choice = JOptionPane.showOptionDialog(this, msg.toString(), "Tidak bisa dihapus", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                    if (choice == 0) {
                        // show full list in scrollable dialog
                        java.util.List<String> all = Menu.getUsageSummary(idMenu, 0);
                        JTextArea ta = new JTextArea(15, 50);
                        ta.setEditable(false);
                        StringBuilder allText = new StringBuilder();
                        for (String s : all) allText.append(s).append("\n");
                        ta.setText(allText.toString());
                        JScrollPane sp = new JScrollPane(ta);
                        sp.setPreferredSize(new Dimension(600, 300));
                        JOptionPane.showMessageDialog(this, sp, "Daftar Transaksi yang memakai menu", JOptionPane.INFORMATION_MESSAGE);
                    }
                    return; // do not delete
                }

                Menu menu = new Menu();
                menu.setIdMenu(idMenu);
                menu.delete();
                JOptionPane.showMessageDialog(this, "Menu berhasil dihapus!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (RuntimeException rex) {
                JOptionPane.showMessageDialog(this, rex.getMessage(), "Tidak bisa dihapus", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadMenuToForm() {
        int selectedRow = tblMenu.getSelectedRow();
        if (selectedRow != -1) {
            int idMenu = (int) tableModel.getValueAt(selectedRow, 0);
            Menu menu = Menu.getMenuById(idMenu);
            
            if (menu != null) {
                txtIdMenu.setText(String.valueOf(menu.getIdMenu()));
                txtNamaMenu.setText(menu.getNamaMenu());
                cmbKategori.setSelectedItem(menu.getNamaKategori());
                txtHarga.setText(String.valueOf(menu.getHarga()));
                cmbStatus.setSelectedItem(menu.getStatusMenu());
            }
        }
    }

    private void clearForm() {
        txtIdMenu.setText("0");
        txtNamaMenu.setText("");
        txtHarga.setText("");
        txtCari.setText("");
        cmbStatus.setSelectedIndex(0);
        if (cmbKategori.getItemCount() > 0) {
            cmbKategori.setSelectedIndex(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmMenu());
    }
}