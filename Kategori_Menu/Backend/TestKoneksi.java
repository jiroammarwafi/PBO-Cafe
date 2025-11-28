package Backend;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestKoneksi {

    public static void main(String[] args) {
        System.out.println("--- TES 1: Menjalankan INSERT ke kategori_menu ---");

        String namaTes = "Kategori Tes " + System.currentTimeMillis();

        String sqlInsert = "INSERT INTO public.kategori_menu (nama_kategori) VALUES ('"
                           + namaTes + "')";

        boolean insertSukses = DBHelper.executeQuery(sqlInsert);

        if (insertSukses) {
            System.out.println("SUKSES: Data tes berhasil dimasukkan ke tabel 'kategori_menu'.");
        } else {
            System.out.println("GAGAL: Tidak bisa memasukkan data.");
            return;
        }

        System.out.println("\n--- TES 2: Menjalankan SELECT ---");
        System.out.println("Membaca semua data dari tabel 'kategori_menu':");

        String sqlSelect = "SELECT id_kategori, nama_kategori FROM public.kategori_menu ORDER BY id_kategori";

        ResultSet rs = DBHelper.selectQuery(sqlSelect);

        int jumlahBaris = 0;
        try {
            while (rs.next()) {
                int id = rs.getInt("id_kategori");
                String nama = rs.getString("nama_kategori");

                System.out.println("  > ID: " + id + ", Nama: " + nama);
                jumlahBaris++;
            }

            if (jumlahBaris > 0) {
                System.out.println("SUKSES: Berhasil membaca " + jumlahBaris + " baris data.");
            } else {
                System.out.println("PERINGATAN: Tidak ada data yang dibaca (tabel mungkin kosong?).");
            }

        } catch (SQLException e) {
            System.out.println("GAGAL: Error saat membaca data dari ResultSet.");
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.getStatement().getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
