/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package frontend;

import backend.*;
import backend.TransaksiBackend.MemberController;
import backend.TransaksiBackend.PesananController;
import backend.TransaksiBackend.Transaksi;
import backend.TransaksiBackend.TransaksiController;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Admin
 */
public class FrmTransaksiPembayaran extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmTransaksiPembayaran.class.getName());

    private int idPesananDipilih = 0;

    public FrmTransaksiPembayaran() {
        initComponents();
        cmbNomorMeja.addActionListener(e -> {
            ambilTotalBelanjaMeja();
        });
        
        // Tambah listener untuk menghitung kembalian real-time
        txtNominalBayar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                hitungKembalian();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                hitungKembalian();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                hitungKembalian();
            }
        });
        
        resetForm();
        isiComboNomorOrder();
        groupRadioCash();
        groupRadioMember();
        panelMember.setVisible(false);
        panelDigital.setVisible(false);
        txtDiskon.setEditable(false);
        txtService.setEditable(false);
    }

    public FrmTransaksiPembayaran(String namaDariPesanan) {
        initComponents();

        tentukanPanelMember(namaDariPesanan);

        resetForm();
        isiComboNomorOrder();
        groupRadioCash();
        groupRadioMember();
    }

    private void resetForm() {
        generateIdTransaksi();
        idPesananDipilih = 0;
        txtTotalBelanja.setText("0");
        txtDiskon.setText("0");
        txtService.setText("0");
        txtTotalAkhir.setText("0");
        txtNominalBayar.setText("0");
        txtKembalian.setText("0");
        txtIdMember.setText("0");
        txtNama.setText("");
        btnSimpan.setEnabled(false); 
        txtDiskon.setEditable(false);
        txtService.setEditable(false);// Tombol simpan terkunci sampai "Proses" diklik
    }

    private void isiComboNomorOrder() {
        cmbNomorMeja.removeAllItems(); 
    ArrayList<TransaksiBackend.Pesanan> listMeja = PesananController.getNomorMejaTerakhir();
    
        if (listMeja.isEmpty()) {
            cmbNomorMeja.addItem("-- Tidak ada pesanan --");
            txtTotalBelanja.setText("0");
            txtTotalAkhir.setText("0");
        } else {
            for (TransaksiBackend.Pesanan p : listMeja) {
                cmbNomorMeja.addItem(String.valueOf(p.getNoMeja()));
            }
            
            // Pilih meja paling atas
            cmbNomorMeja.setSelectedIndex(0);
            
            // Panggil fungsi secara manual satu kali untuk inisialisasi total belanja
            ambilTotalBelanjaMeja();
        }
    }

    private void ambilTotalBelanjaMeja() {
        Object selectedItem = cmbNomorMeja.getSelectedItem();
    
        if (selectedItem != null && !selectedItem.toString().contains("-")) {
            try {
                int noMeja = Integer.parseInt(selectedItem.toString());
                ArrayList<TransaksiBackend.Pesanan> list = PesananController.getNomorMejaTerakhir();
                
                for (TransaksiBackend.Pesanan p : list) {
                    if (p.getNoMeja() == noMeja) {
                        double total = PesananController.getTotalBelanja(p.getIdOrder());
                        txtTotalBelanja.setText(String.format("%.0f", total));
                        
                        // Update perhitungan pajak dan diskon otomatis
                        hitungDiskonDanPajak();
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                txtTotalBelanja.setText("0");
            }
        }
    }

    private void generateIdTransaksi() {
        txtIdTransaksi.setText(String.valueOf(TransaksiController.getLastId() + 1));
        txtTanggal.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private void groupRadioCash() {
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbMember); 
        bg.add(rbNonMember);
        
        rbMember.addActionListener(e -> { 
            panelMember.setVisible(true); 
            panelNonMember.setVisible(false);
            hitungDiskonDanPajak();
        });
        
        rbNonMember.addActionListener(e -> { 
            panelMember.setVisible(false); 
            panelNonMember.setVisible(true);
            txtDiskon.setText("0");
            hitungDiskonDanPajak(); 
        });
    }

    private void groupRadioMember() {
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbMember); bg.add(rbNonMember);
        rbMember.addActionListener(e -> { panelMember.setVisible(true); panelNonMember.setVisible(false); });
        rbNonMember.addActionListener(e -> { panelMember.setVisible(false); panelNonMember.setVisible(true); });
    }

   private void hitungDiskonDanPajak() {
        try {
            double totalBelanja = Double.parseDouble(txtTotalBelanja.getText().isEmpty() ? "0" : txtTotalBelanja.getText());
            double pajak = totalBelanja * 0.10;
            double diskon = 0;

            if (rbMember.isSelected() && !txtIdMember.getText().equals("0")) {
                int idMem = Integer.parseInt(txtIdMember.getText());
                
                // Ambil poin dari database
                String queryPoin = "SELECT points FROM member WHERE id_member = " + idMem;
                ResultSet rs = dbHelper.selectQuery(queryPoin);
                
                if (rs != null && rs.next()) {
                    int poin = rs.getInt("points");

                    if (poin >= 10) {
                        diskon = totalBelanja * 0.5;
                    }
                }
            }

            double totalAkhir = totalBelanja + pajak - diskon;

            // Tampilkan ke masing-masing field
            txtService.setText(String.format("%.0f", pajak));
            txtDiskon.setText(String.format("%.0f", diskon)); 
            txtTotalAkhir.setText(String.format("%.0f", totalAkhir));
            
        } catch (Exception e) {
            System.out.println("Gagal menghitung: " + e.getMessage());
        }
    }
    
    private void hitungKembalian() {
        try {
            String nominalBayarText = txtNominalBayar.getText().trim();
            String totalAkhirText = txtTotalAkhir.getText().trim();
            
            if (nominalBayarText.isEmpty() || totalAkhirText.isEmpty()) {
                txtKembalian.setText("0");
                return;
            }
            
            double nominalBayar = Double.parseDouble(nominalBayarText.replace(",", "."));
            double totalAkhir = Double.parseDouble(totalAkhirText.replace(",", "."));
            
            double kembalian = nominalBayar - totalAkhir;
            
            // Tampilkan kembalian, jika negatif tampilkan 0
            if (kembalian < 0) {
                txtKembalian.setText("0");
            } else {
                txtKembalian.setText(String.format("%.0f", kembalian));
            }
        } catch (NumberFormatException e) {
            txtKembalian.setText("0");
        }
    }
        
    private void tentukanPanelMember(String nama) {
        if (nama.isEmpty()) {
            rbNonMember.setSelected(true);
            panelMember.setVisible(false);
            panelNonMember.setVisible(true);
            return;
        }

        try {
            // Ambil ID dan POIN dari database
            String sql = "SELECT id_member, points FROM member WHERE nama_member ILIKE '" + nama.replace("'", "''") + "'";
            ResultSet rs = dbHelper.selectQuery(sql);

            if (rs != null && rs.next()) {
                int id = rs.getInt("id_member");
                int poin = rs.getInt("points"); // Ambil poin member
                
                txtIdMember.setText(String.valueOf(id));
                rbMember.setSelected(true);
                panelMember.setVisible(true);
                panelNonMember.setVisible(false);
                
                // Simpan informasi poin sementara di label (opsional) atau gunakan logika langsung
                if (poin >= 10) {
                    // Memberikan tanda bahwa mereka punya reward diskon
                    txtDiskon.setToolTipText("Member memiliki " + poin + " poin. Diskon 10% aktif.");
                }
            } else {
                rbNonMember.setSelected(true);
                panelMember.setVisible(false);
                panelNonMember.setVisible(true);
                txtIdMember.setText("0");
            }
            
            hitungDiskonDanPajak(); // Hitung ulang setelah status member dipastikan
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        jPanel9 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtIdTransaksi = new javax.swing.JTextField();
        cmbNomorMeja = new javax.swing.JComboBox<>();
        txtTanggal = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtTotalBelanja = new javax.swing.JTextField();
        txtDiskon = new javax.swing.JTextField();
        txtService = new javax.swing.JTextField();
        txtTotalAkhir = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        rbCash = new javax.swing.JRadioButton();
        rbEWallet = new javax.swing.JRadioButton();
        panelCash = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtNominalBayar = new javax.swing.JTextField();
        txtKembalian = new javax.swing.JTextField();
        panelDigital = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtNomor = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        btnProses = new javax.swing.JButton();
        btnSimpan = new javax.swing.JButton();
        panelMember = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtIdMember = new javax.swing.JTextField();
        txtNama = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        panelNonMember = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtNamaNonMember = new javax.swing.JTextField();
        jPanel12 = new javax.swing.JPanel();
        rbMember = new javax.swing.JRadioButton();
        rbNonMember = new javax.swing.JRadioButton();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(51, 153, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setForeground(new java.awt.Color(102, 153, 255));
        jPanel2.setToolTipText("");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Form Transaksi Cafe");
        jLabel1.setPreferredSize(new java.awt.Dimension(96, 36));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(96, 96, 96))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setText("ID Transaksi");

        jLabel3.setText("Nomor Meja");

        jLabel4.setText("Tanggal/Waktu");

        txtIdTransaksi.setEnabled(false);
        txtIdTransaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdTransaksiActionPerformed(evt);
            }
        });

        cmbNomorMeja.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(txtTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(31, 31, 31)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbNomorMeja, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtIdTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(310, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtIdTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbNomorMeja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel5.setText("Total Belanja");

        jLabel6.setText("Diskon");

        jLabel7.setText("Pajak/Service");

        jLabel8.setText("Total Akhir");

        txtTotalBelanja.setEnabled(false);

        txtDiskon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDiskonActionPerformed(evt);
            }
        });

        txtService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtServiceActionPerformed(evt);
            }
        });

        txtTotalAkhir.setEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel5)
                            .addGap(31, 31, 31)
                            .addComponent(txtTotalBelanja, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel7)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(30, 30, 30)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(txtTotalAkhir, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(txtService))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(63, 63, 63)
                        .addComponent(txtDiskon, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtTotalBelanja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtDiskon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtTotalAkhir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        rbCash.setText("Cash");
        rbCash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbCashActionPerformed(evt);
            }
        });

        rbEWallet.setText("E-Wallet/Debit");
        rbEWallet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbEWalletActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(153, 153, 153)
                .addComponent(rbCash, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(rbEWallet)
                .addContainerGap(139, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbCash)
                    .addComponent(rbEWallet))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        panelCash.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Cash");

        jLabel10.setText("Nominal Bayar");

        jLabel11.setText("Kembalian");

        txtKembalian.setEnabled(false);
        txtKembalian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtKembalianActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCashLayout = new javax.swing.GroupLayout(panelCash);
        panelCash.setLayout(panelCashLayout);
        panelCashLayout.setHorizontalGroup(
            panelCashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelCashLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addGap(26, 26, 26)
                .addGroup(panelCashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtNominalBayar)
                    .addComponent(txtKembalian, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelCashLayout.setVerticalGroup(
            panelCashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCashLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtNominalBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtKembalian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelDigital.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("E-Wallet/Debit");

        jLabel13.setText("Nomor E-Wallet/Kartu");

        javax.swing.GroupLayout panelDigitalLayout = new javax.swing.GroupLayout(panelDigital);
        panelDigital.setLayout(panelDigitalLayout);
        panelDigitalLayout.setHorizontalGroup(
            panelDigitalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDigitalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDigitalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelDigitalLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtNomor, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 273, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelDigitalLayout.setVerticalGroup(
            panelDigitalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDigitalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDigitalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtNomor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnProses.setText("Proses");
        btnProses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProsesActionPerformed(evt);
            }
        });

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnProses, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnProses)
                    .addComponent(btnSimpan))
                .addGap(37, 37, 37))
        );

        panelMember.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Member");

        jLabel14.setText("ID Member");

        txtIdMember.setEnabled(false);
        txtIdMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdMemberActionPerformed(evt);
            }
        });

        txtNama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNamaActionPerformed(evt);
            }
        });

        jLabel16.setText("Nama");

        javax.swing.GroupLayout panelMemberLayout = new javax.swing.GroupLayout(panelMember);
        panelMember.setLayout(panelMemberLayout);
        panelMemberLayout.setHorizontalGroup(
            panelMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelMemberLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(panelMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtNama, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                    .addComponent(txtIdMember))
                .addContainerGap(307, Short.MAX_VALUE))
        );
        panelMemberLayout.setVerticalGroup(
            panelMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMemberLayout.createSequentialGroup()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14)
                    .addComponent(txtIdMember, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        panelNonMember.setBorder(new javax.swing.border.LineBorder(null));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Non-Member");

        jLabel18.setText("Nama");

        txtNamaNonMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNamaNonMemberActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelNonMemberLayout = new javax.swing.GroupLayout(panelNonMember);
        panelNonMember.setLayout(panelNonMemberLayout);
        panelNonMemberLayout.setHorizontalGroup(
            panelNonMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNonMemberLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelNonMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelNonMemberLayout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panelNonMemberLayout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtNamaNonMember, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(307, 307, 307))))
        );
        panelNonMemberLayout.setVerticalGroup(
            panelNonMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNonMemberLayout.createSequentialGroup()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelNonMemberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNamaNonMember, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(0, 15, Short.MAX_VALUE))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        rbMember.setText("Member");
        rbMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbMemberActionPerformed(evt);
            }
        });

        rbNonMember.setText("Non-Member");
        rbNonMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbNonMemberActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(153, 153, 153)
                .addComponent(rbMember, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(rbNonMember)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbMember)
                    .addComponent(rbNonMember))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelNonMember, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelMember, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCash, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelDigital, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelNonMember, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelMember, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCash, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelDigital, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtIdTransaksiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdTransaksiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdTransaksiActionPerformed

    private void txtDiskonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDiskonActionPerformed
        // TODO add your handling code here:
       
    }//GEN-LAST:event_txtDiskonActionPerformed

    private void rbCashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbCashActionPerformed
        // TODO add your handling code here:
        panelCash.setVisible(true);
        panelDigital.setVisible(false);
    }//GEN-LAST:event_rbCashActionPerformed

    private void txtKembalianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtKembalianActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtKembalianActionPerformed

    private void btnProsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProsesActionPerformed
        // TODO add your handling code here:
        try {
            hitungDiskonDanPajak(); 

            double totalAkhir = Double.parseDouble(txtTotalAkhir.getText().replace(",", "."));
            
            // 3. Validasi khusus jika metode pembayaran adalah CASH
            if (rbCash.isSelected()) {
                if (txtNominalBayar.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Silakan isi Nominal Bayar dulu!");
                    btnSimpan.setEnabled(false); // Pastikan tetap mati
                    return;
                }
                
                double bayar = Double.parseDouble(txtNominalBayar.getText().replace(",", "."));
                
                if (bayar < totalAkhir) {
                    JOptionPane.showMessageDialog(this, "Uang Kurang! Tidak bisa memproses simpan.");
                    btnSimpan.setEnabled(false); // Tetap matikan jika uang kurang
                    return;
                }
                
                // Hitung Kembalian
                double kembalian = bayar - totalAkhir;
                txtKembalian.setText(String.format("%.0f", kembalian));
            }  else if (rbEWallet.isSelected()) {
                if (txtNomor.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Isi Nomor Kartu/Referensi dulu!");
                    btnSimpan.setEnabled(false);
                    return;
                }
            }

            btnSimpan.setEnabled(true);
            
            // Beri tanda visual agar user tahu
            btnSimpan.requestFocus(); 
            JOptionPane.showMessageDialog(this, "Perhitungan Selesai. Silakan klik SIMPAN.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Input angka tidak valid! Periksa Nominal Bayar.");
            btnSimpan.setEnabled(false);
        }
    }//GEN-LAST:event_btnProsesActionPerformed

   private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {
        // 1. Matikan tombol segera agar tidak diklik dua kali
        btnSimpan.setEnabled(false);

        try {
            Transaksi t = new Transaksi();
            
            // --- SET DATA TRANSAKSI ---
            // Penanganan Waktu
            if (txtTanggal.getText().isEmpty() || txtTanggal.getText().equals("null")) {
                t.waktu = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else {
                t.waktu = txtTanggal.getText();
            }

            // Ambil ID Pesanan dari Meja yang dipilih
            int noMeja = Integer.parseInt(cmbNomorMeja.getSelectedItem().toString());
            ArrayList<TransaksiBackend.Pesanan> list = PesananController.getNomorMejaTerakhir();
            for(TransaksiBackend.Pesanan p : list) {
                if(p.getNoMeja() == noMeja) {
                    t.idPesanan = p.getIdOrder();
                    break;
                }
            }
            
            // Set data lainnya dari TextField
            t.idMember = Integer.parseInt(txtIdMember.getText());
            t.totalBelanja = Double.parseDouble(txtTotalBelanja.getText());
            t.diskon = Double.parseDouble(txtDiskon.getText());
            t.pajak = Double.parseDouble(txtService.getText());
            t.totalAkhir = Double.parseDouble(txtTotalAkhir.getText());
            t.nominalBayar = Double.parseDouble(txtNominalBayar.getText());
            t.kembalian = Double.parseDouble(txtKembalian.getText());
            t.metode = rbCash.isSelected() ? "Cash" : (rbEWallet.isSelected() ? "E-WALLET" : "DEBIT");
            t.nomor = txtNomor.getText();

            // 2. PROSES SIMPAN (HANYA DIPANGGIL 1 KALI)
            int idBaru = TransaksiController.insert(t);

            if (idBaru > 0) {
                PesananController.updateStatusBayar(t.idPesanan);

                if (t.idMember > 0) {

                    if (t.totalBelanja >= 50000) {
                        dbHelper.executeQuery("UPDATE member SET points = points + 1 WHERE id_member = " + t.idMember);
                    }

                    if (t.diskon > 0) {
                        dbHelper.executeQuery("UPDATE member SET points = 0 WHERE id_member = " + t.idMember);
                    }
                }

                // 4. BUAT NOTA
                String nota = "----------------CAFE----------------\n"
                            + "---------STRUK PEMBAYARAN---------\n"
                            + "----------------------------------\n"
                            + "ID Transaksi : " + idBaru + "\n"
                            + "No Meja      : " + noMeja + "\n"
                            + "Tanggal      : " + t.waktu + "\n"
                            + "----------------------------------\n"
                            + "Total Belanja: " + t.totalBelanja + "\n"
                            + "Diskon       : " + t.diskon + "\n"
                            + "Pajak/Servis : " + t.pajak + "\n"
                            + "TOTAL AKHIR  : " + t.totalAkhir + "\n"
                            + "----------------------------------\n"
                            + "Member       : " + txtNama.getText() + " (" + t.idMember + ")\n"
                            + "----------------------------------\n";
                
                if (rbCash.isSelected()) {
                    nota += "Pembayaran   : CASH\n"
                        + "Dibayar      : " + t.nominalBayar + "\n"
                        + "Kembalian    : " + t.kembalian + "\n";
                } else {
                    nota += "Pembayaran   : " + t.metode + "\n"
                        + "No. Ref      : " + t.nomor + "\n";
                }
                nota += "----------------------------------\n"
                    + "     Terima kasih!\n";

                JOptionPane.showMessageDialog(this, "Transaksi Berhasil!");
                JOptionPane.showMessageDialog(this, nota); // Tampilkan struk

                resetForm();
                isiComboNomorOrder(); 
                
            } else {
                JOptionPane.showMessageDialog(this, "Gagal Simpan ke Database.");
                btnSimpan.setEnabled(true);
            }
            
            if (t.idMember > 0 && Double.parseDouble(txtDiskon.getText()) > 0) {
                String sqlReset = "UPDATE member SET points = 0 WHERE id_member = " + t.idMember;
                dbHelper.executeQuery(sqlReset);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi Kesalahan: " + e.getMessage());
            btnSimpan.setEnabled(true);
        }
    }

    private void txtServiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtServiceActionPerformed
        // TODO add your handling code here:
        hitungDiskonDanPajak();
    }//GEN-LAST:event_txtServiceActionPerformed

    private void txtIdMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdMemberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdMemberActionPerformed

    private void txtNamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNamaActionPerformed
        // TODO add your handling code here:
        String nama = txtNama.getText().trim();
        int id = MemberController.getIdMemberByNama(nama);
        if (id > 0) {
            txtIdMember.setText(String.valueOf(id));
            hitungDiskonDanPajak(); // Langsung hitung diskon setelah ID ditemukan
            JOptionPane.showMessageDialog(this, "Member ditemukan!");
        } else {
            txtIdMember.setText("0");
            hitungDiskonDanPajak();
            JOptionPane.showMessageDialog(this, "Member tidak ditemukan.");
        }
    }//GEN-LAST:event_txtNamaActionPerformed

    private void txtNamaKeyReleased(java.awt.event.KeyEvent evt) {                                    
        String namaCari = txtNama.getText().trim();
        
        if (namaCari.isEmpty()) {
            txtIdMember.setText("0");
            hitungDiskonDanPajak();
            return;
        }

        try {
            // Cari ID dan Poin berdasarkan nama yang diketik
            String sql = "SELECT id_member, points FROM member WHERE nama_member ILIKE '" + namaCari.replace("'", "''") + "%'";
            ResultSet rs = dbHelper.selectQuery(sql);

            if (rs != null && rs.next()) {
                txtIdMember.setText(String.valueOf(rs.getInt("id_member")));
                hitungDiskonDanPajak(); 
            } else {
                txtIdMember.setText("0");
                txtDiskon.setText("0");
                hitungDiskonDanPajak();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rbEWalletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbEWalletActionPerformed
        // TODO add your handling code here:
        // TODO add your handling code here:
        panelCash.setVisible(false);
        panelDigital.setVisible(true);
    }//GEN-LAST:event_rbEWalletActionPerformed

    private void cmbNomorMejaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbNomorMejaActionPerformed
        // TODO add your handling code here:
        String q = "select no_meja from pesanan";
        ResultSet rs = dbHelper.selectQuery(q);
        
        
    }//GEN-LAST:event_cmbNomorMejaActionPerformed

    private void txtNamaNonMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNamaNonMemberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNamaNonMemberActionPerformed

    private void rbMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbMemberActionPerformed
        // TODO add your handling code here:
        panelMember.setVisible(true);
        panelNonMember.setVisible(false);
        txtIdMember.setText("0"); // Reset
        hitungDiskonDanPajak();   // Update total (diskon jadi aktif)
    }//GEN-LAST:event_rbMemberActionPerformed

    private void rbNonMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbNonMemberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbNonMemberActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FrmTransaksiPembayaran().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnProses;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<String> cmbNomorMeja;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel panelCash;
    private javax.swing.JPanel panelDigital;
    private javax.swing.JPanel panelMember;
    private javax.swing.JPanel panelNonMember;
    private javax.swing.JRadioButton rbCash;
    private javax.swing.JRadioButton rbEWallet;
    private javax.swing.JRadioButton rbMember;
    private javax.swing.JRadioButton rbNonMember;
    private javax.swing.JTextField txtDiskon;
    private javax.swing.JTextField txtIdMember;
    private javax.swing.JTextField txtIdTransaksi;
    private javax.swing.JTextField txtKembalian;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtNamaNonMember;
    private javax.swing.JTextField txtNominalBayar;
    private javax.swing.JTextField txtNomor;
    private javax.swing.JTextField txtService;
    private javax.swing.JTextField txtTanggal;
    private javax.swing.JTextField txtTotalAkhir;
    private javax.swing.JTextField txtTotalBelanja;
    // End of variables declaration//GEN-END:variables
}
