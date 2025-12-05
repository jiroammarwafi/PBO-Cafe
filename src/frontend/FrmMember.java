package frontend;

import backend.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FrmMember extends JFrame {

    private static final String FONT_NAME = "Segoe UI";
    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";
    private static final String ERROR_PREFIX = "Error: ";
    private static final String LABEL_ID_MEMBER = "ID Member";
    private static final String LABEL_NAMA_MEMBER = "Nama Member";
    private static final String LABEL_NO_TELP = "No. Telepon";
    private static final String LABEL_POINTS = "Points";
    private static final String LABEL_TANGGAL_JOIN = "Tanggal Join";
    private static final String[] COLUMN_NAMES = {"ID", LABEL_NAMA_MEMBER, LABEL_NO_TELP, LABEL_POINTS, LABEL_TANGGAL_JOIN};

    private JTextField txtIdMember;
    private JTextField txtNamaMember;
    private JTextField txtNoTelp;
    private JTextField txtCari;
    private JSpinner spnPoints;
    private JTextField txtTanggalJoin;
    private JButton btnSimpan;
    private JButton btnHapus;
    private JButton btnTambahBaru;
    private JButton btnCari;
    private JTable tblMember;
    private JScrollPane jScrollPane1;

    public FrmMember() {

        //tampilan UI
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));   
        UIManager.put("Table.showGrid", false);                     
        UIManager.put("Table.intercellSpacing", new Dimension(8, 8));

        //inisialisasi komponen form
        JLabel lblId = new JLabel(LABEL_ID_MEMBER);
        JLabel lblNama = new JLabel(LABEL_NAMA_MEMBER);
        JLabel lblNoTelp = new JLabel(LABEL_NO_TELP);
        JLabel lblPoints = new JLabel(LABEL_POINTS);
        JLabel lblTanggalJoin = new JLabel(LABEL_TANGGAL_JOIN);

        txtIdMember = new JTextField();
        txtNamaMember = new JTextField();
        txtNoTelp = new JTextField();
        spnPoints = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
        txtTanggalJoin = new JTextField();
        txtCari = new JTextField();

        btnSimpan = makeButton("Simpan");
        btnHapus = makeButton("Hapus");
        btnTambahBaru = makeButton("Tambah Baru");
        btnCari = makeButton("Cari");

        txtIdMember.setText("0");
        txtIdMember.setEnabled(false);
        txtTanggalJoin.setEnabled(false);

        tblMember = new JTable();
        jScrollPane1 = new JScrollPane(tblMember);

        setLayout(null); // manual layout

        //font style
        Font fLabel = new Font(FONT_NAME, Font.PLAIN, 14);
        Font fText = new Font(FONT_NAME, Font.PLAIN, 14);
        Font fBtn = new Font(FONT_NAME, Font.BOLD, 14);

        lblId.setFont(fLabel);
        lblNama.setFont(fLabel);
        lblNoTelp.setFont(fLabel);
        lblPoints.setFont(fLabel);
        lblTanggalJoin.setFont(fLabel);

        txtIdMember.setFont(fText);
        txtNamaMember.setFont(fText);
        txtNoTelp.setFont(fText);
        spnPoints.setFont(fText);
        txtTanggalJoin.setFont(fText);
        txtCari.setFont(fText);

        btnSimpan.setFont(fBtn);
        btnHapus.setFont(fBtn);
        btnTambahBaru.setFont(fBtn);
        btnCari.setFont(fBtn);

        tblMember.setFont(fText);
        tblMember.setRowHeight(28);

        //rapikan grid - layout form
        int xLabel = 30;
        int xField = 150;
        int wField = 300;
        int hField = 28;
        int gapY = 35;

        // POSISI FORM (atas)
        lblId.setBounds(xLabel, 30, 120, 25);
        txtIdMember.setBounds(xField, 30, 80, hField);

        lblNama.setBounds(xLabel, 30 + gapY, 120, 25);
        txtNamaMember.setBounds(xField, 30 + gapY, wField, hField);

        lblNoTelp.setBounds(xLabel, 30 + gapY * 2, 120, 25);
        txtNoTelp.setBounds(xField, 30 + gapY * 2, wField, hField);

        lblPoints.setBounds(xLabel, 30 + gapY * 3, 120, 25);
        spnPoints.setBounds(xField, 30 + gapY * 3, 100, hField);

        lblTanggalJoin.setBounds(xLabel, 30 + gapY * 4, 120, 25);
        txtTanggalJoin.setBounds(xField, 30 + gapY * 4, wField, hField);

        // POSISI TOMBOL (bawah - setelah form)
        int startYTombol = 30 + gapY * 5 + 20; // Dikurangi 1 gap karena email dihapus
        int tombolWidth = 120;
        int tombolHeight = 35;
        int gapXTombol = 20; // Jarak antar tombol

        btnSimpan.setBounds(xLabel, startYTombol, tombolWidth, tombolHeight);
        btnHapus.setBounds(xLabel + tombolWidth + gapXTombol, startYTombol, tombolWidth, tombolHeight);
        btnTambahBaru.setBounds(xLabel + (tombolWidth + gapXTombol) * 2, startYTombol, tombolWidth, tombolHeight);

        // POSISI PENCARIAN (sejajar dengan tombol tapi di kanan)
        txtCari.setBounds(xLabel + (tombolWidth + gapXTombol) * 3, startYTombol, 150, tombolHeight);
        btnCari.setBounds(xLabel + (tombolWidth + gapXTombol) * 3 + 160, startYTombol, 80, tombolHeight);

        // POSISI TABLE (bawah sekali - setelah tombol)
        jScrollPane1.setBounds(30, startYTombol + tombolHeight + 20, 800, 250);

        add(lblId);
        add(txtIdMember);
        add(lblNama);
        add(txtNamaMember);
        add(lblNoTelp);
        add(txtNoTelp);
        add(lblPoints);
        add(spnPoints);
        add(lblTanggalJoin);
        add(txtTanggalJoin);
        add(btnSimpan);
        add(btnHapus);
        add(btnTambahBaru);
        add(txtCari);
        add(btnCari);
        add(jScrollPane1);

        // ====== EVENT ======
        btnSimpan.addActionListener(e -> simpan());
        btnHapus.addActionListener(e -> hapus());
        btnTambahBaru.addActionListener(e -> kosongkanForm());
        btnCari.addActionListener(e -> cari(txtCari.getText()));
        tblMember.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                isiFormDariTabel();
            }
        });

        // Frame Setting - diperlebar untuk menampung lebih banyak kolom
        setTitle("Form Member Café");
        setSize(880, 600); // Diperkecil karena ada lebih sedikit field
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tampilkanData();
        kosongkanForm();
    }

    // STYLE BUTTON (MENGHILANGKAN GARIS FOKUS, MEMBERI EFEK HALUS)
    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(139, 69, 19)); // Warna coklat café
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(new Color(120, 60, 15)));
        return b;
    }

    //CRUD
    private void simpan() {
        // Validasi form
        if (txtNamaMember.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama member harus diisi!");
            txtNamaMember.requestFocus();
            return;
        }

        try {
            Member member = new Member();
            member.setIdMember(Integer.parseInt(txtIdMember.getText()));
            member.setNamaMember(txtNamaMember.getText().trim());
            member.setNoTelp(txtNoTelp.getText().trim());
            member.setPoints((Integer) spnPoints.getValue());
            
            // Jika ID = 0, berarti baru, set tanggal join
            if (member.getIdMember() == 0) {
                member.setTanggalJoin(LocalDateTime.now());
            }

            member.save();

            txtIdMember.setText(String.valueOf(member.getIdMember()));
            tampilkanData();
            JOptionPane.showMessageDialog(this, "Data member berhasil disimpan!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, ERROR_PREFIX + e.getMessage());
        }
    }

    private void hapus() {
        int row = tblMember.getSelectedRow();
        if (row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Yakin ingin menghapus data member ini?", "Konfirmasi Hapus", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int id = Integer.parseInt(tblMember.getValueAt(row, 0).toString());
                    Member member = new Member().getById(id);
                    if (member.delete()) {
                        tampilkanData();
                        kosongkanForm();
                        JOptionPane.showMessageDialog(this, "Data member berhasil dihapus!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Gagal menghapus data member!");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, ERROR_PREFIX + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus terlebih dahulu.");
        }
    }

    private void isiFormDariTabel() {
        int row = tblMember.getSelectedRow();
        if (row >= 0) {
            try {
                int id = Integer.parseInt(tblMember.getValueAt(row, 0).toString());
                Member member = new Member().getById(id);
                
                txtIdMember.setText(String.valueOf(member.getIdMember()));
                txtNamaMember.setText(member.getNamaMember());
                txtNoTelp.setText(member.getNoTelp() != null ? member.getNoTelp() : "");
                spnPoints.setValue(member.getPoints() != null ? member.getPoints() : 0);
                
                // Format tanggal join
                if (member.getTanggalJoin() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
                    txtTanggalJoin.setText(member.getTanggalJoin().format(formatter));
                } else {
                    txtTanggalJoin.setText("");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, ERROR_PREFIX + e.getMessage());
            }
        }
    }

    private void kosongkanForm() {
        txtIdMember.setText("0");
        txtNamaMember.setText("");
        txtNoTelp.setText("");
        spnPoints.setValue(0);
        txtTanggalJoin.setText("");
        txtCari.setText("");
        tblMember.clearSelection();
    }

    private void tampilkanData() {
        DefaultTableModel model = new DefaultTableModel(new Object[][]{}, COLUMN_NAMES) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tidak bisa edit langsung di tabel
            }
        };

        try {
            for (Member m : new Member().getAll()) {
                // Format tanggal join untuk display
                String tanggalJoinFormatted = "";
                if (m.getTanggalJoin() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
                    tanggalJoinFormatted = m.getTanggalJoin().format(formatter);
                }

                model.addRow(new Object[]{
                    m.getIdMember(),
                    m.getNamaMember(),
                    m.getNoTelp() != null ? m.getNoTelp() : "-",
                    m.getPoints() != null ? m.getPoints() : 0,
                    tanggalJoinFormatted
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }

        tblMember.setModel(model);
        
        // Atur lebar kolom
        tblMember.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tblMember.getColumnModel().getColumn(1).setPreferredWidth(150);  // Nama Member
        tblMember.getColumnModel().getColumn(2).setPreferredWidth(120);  // No. Telepon
        tblMember.getColumnModel().getColumn(3).setPreferredWidth(60);   // Points
        tblMember.getColumnModel().getColumn(4).setPreferredWidth(120);  // Tanggal Join
    }

    private void cari(String keyword) {
        DefaultTableModel model = new DefaultTableModel(new Object[][]{}, COLUMN_NAMES) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            for (Member m : new Member().search(keyword)) {
                // Format tanggal join untuk display
                String tanggalJoinFormatted = "";
                if (m.getTanggalJoin() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
                    tanggalJoinFormatted = m.getTanggalJoin().format(formatter);
                }

                model.addRow(new Object[]{
                    m.getIdMember(),
                    m.getNamaMember(),
                    m.getNoTelp() != null ? m.getNoTelp() : "-",
                    m.getPoints() != null ? m.getPoints() : 0,
                    tanggalJoinFormatted
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }

        tblMember.setModel(model);
    }

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        new FrmMember().setVisible(true);
    }
}