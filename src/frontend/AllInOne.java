package frontend;
import javax.swing.*;
import java.awt.*;

public class AllInOne extends JFrame {
    private JTabbedPane tabbedPane;

    public AllInOne() {
        setTitle("PBO Cafe Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 730);
        setLocationRelativeTo(null);
        setResizable(true);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        addFormTab("Pesanan", new FrmPesanan());
        addFormTab("Menu", new FrmMenu());
        addFormTab("Kategori Menu", new KategoriMenuApp());
        addFormTab("Member", new FrmMember());
        addFormTab("Transaksi Pembayaran", new FrmTransaksiPembayaran());
        addFormTab("Laporan Penjualan", new FrmLaporanPenjualan());

        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void addFormTab(String tabName, JFrame form) {
        form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(form.getContentPane(), BorderLayout.CENTER);
        
        tabbedPane.addTab(tabName, contentPanel);
    }

    public static void main(String[] args) {
       
        SwingUtilities.invokeLater(() -> {
            UIManager.put("Button.focus", new Color(0, 0, 0, 0));
            new AllInOne();
        });
    }
}
