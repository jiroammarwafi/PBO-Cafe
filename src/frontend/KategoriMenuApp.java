package frontend;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import backend.Kategori;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class KategoriMenuApp extends JFrame {
    private JTextField txtIdKategori;
    private JTextField txtNamaKategori;
    private JTable tableKategori;
    private DefaultTableModel tableModel;

    public KategoriMenuApp() {
        setTitle("Form Kategori Menu");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        // Panel utama
        JPanel panelMain = new JPanel(new BorderLayout(10, 10));
        panelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel form input
        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblIdKategori = new JLabel("ID Kategori:");
        JLabel lblNamaKategori = new JLabel("Nama Kategori:");

        txtIdKategori = new JTextField(15);
        txtNamaKategori = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelForm.add(lblIdKategori, gbc);

        gbc.gridx = 1;
        panelForm.add(txtIdKategori, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelForm.add(lblNamaKategori, gbc);

        gbc.gridx = 1;
        panelForm.add(txtNamaKategori, gbc);

        // Panel tombol
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnTambah = new JButton("Tambah");
        JButton btnEdit = new JButton("Edit");
        JButton btnHapus = new JButton("Hapus");

        panelButton.add(btnTambah);
        panelButton.add(btnEdit);
        panelButton.add(btnHapus);

        // Tabel kategori (hanya ID dan Nama sesuai requirement)
        String[] columnNames = {"ID Kategori", "Nama Kategori"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // mencegah edit langsung di tabel
            }
        };
        tableKategori = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableKategori);

        // Load data dari backend (DB lewat class Kategori)
        loadDataFromBackend();

        // Tambahkan listener tombol
        btnTambah.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tambahKategori();
            }
        });

        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editKategori();
            }
        });

        btnHapus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hapusKategori();
            }
        });

        // Listener klik tabel untuk isi form
        tableKategori.getSelectionModel().addListSelectionListener(event -> {
            int selectedRow = tableKategori.getSelectedRow();
            if (selectedRow >= 0) {
                txtIdKategori.setText(tableModel.getValueAt(selectedRow, 0).toString());
                txtNamaKategori.setText(tableModel.getValueAt(selectedRow, 1).toString());
            }
        });

        // Susun ke dalam panel utama
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(panelForm, BorderLayout.CENTER);
        topPanel.add(panelButton, BorderLayout.SOUTH);

        panelMain.add(topPanel, BorderLayout.NORTH);
        panelMain.add(scrollPane, BorderLayout.CENTER);

        setContentPane(panelMain);
    }

    private void loadDataFromBackend() {
        tableModel.setRowCount(0);

        ArrayList<Kategori> list = new Kategori().getAll();
        for (Kategori k : list) {
            tableModel.addRow(new Object[]{
                    k.getIdkategori(),
                    k.getNama()
            });
        }
    }

    private void tambahKategori() {
        String idText = txtIdKategori.getText().trim();
        String nama = txtNamaKategori.getText().trim();

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Kategori wajib diisi", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tambah kategori baru lewat backend (ID auto dari DB)
        Kategori kat = new Kategori();
        kat.setNama(nama);
        kat.save();

        loadDataFromBackend();
        clearForm();
    }

    private void editKategori() {
        int selectedRow = tableKategori.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin diedit", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idText = txtIdKategori.getText().trim();
        String nama = txtNamaKategori.getText().trim();

        if (idText.isEmpty() || nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID dan Nama Kategori wajib diisi", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID Kategori harus berupa angka (integer)", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ambil dari backend lalu update nama
        Kategori kat = new Kategori().getById(id);
        if (kat.getIdkategori() == 0) {
            JOptionPane.showMessageDialog(this, "Data kategori dengan ID tersebut tidak ditemukan", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        kat.setNama(nama);
        // keterangan tetap seperti di DB, form tidak mengubahnya
        kat.save();

        loadDataFromBackend();
        clearForm();
    }

    private void hapusKategori() {
        int selectedRow = tableKategori.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin dihapus", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());

            Kategori kat = new Kategori().getById(id);
            if (kat.getIdkategori() == 0) {
                JOptionPane.showMessageDialog(this, "Data kategori dengan ID tersebut tidak ditemukan", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            kat.delete();

            loadDataFromBackend();
            clearForm();
        }
    }

    private void clearForm() {
        txtIdKategori.setText("");
        txtNamaKategori.setText("");
        tableKategori.clearSelection();
        txtIdKategori.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new KategoriMenuApp().setVisible(true);
        });
    }
}
