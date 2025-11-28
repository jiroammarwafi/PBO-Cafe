package Backend;
import java.sql.*;
public class DBHelper {

    public static Connection bukaKoneksi() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver"); 

            String url = "jdbc:postgresql://localhost:5432/kasir_cafe"; 
            String user = "postgres";
            String pass = "12345"; // Ganti dengan password superuser PostgreSQL

            conn = DriverManager.getConnection(url, user, pass);

        } catch (Exception e) {
            System.out.println("Error koneksi: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }
    public static int insertQueryGetId(String query) {
        int generatedId = -1; // Nilai default jika gagal
        Connection conn = bukaKoneksi();
        
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            
            ResultSet rs = stmt.getGeneratedKeys();
            
            if (rs.next()) {
                // Ambil nilai ID yang digenerate
                generatedId = rs.getInt(1);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return generatedId;
    }
    public static boolean executeQuery(String query) {
        boolean success = false;
        Connection conn = bukaKoneksi();
        
        try {
            Statement stmt = conn.createStatement();
            
            stmt.execute(query);
            
            success = true; // Jika tidak ada error, anggap sukses
            
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
    public static ResultSet selectQuery(String query) {
        ResultSet rs = null;
        Connection conn = bukaKoneksi();
        
        try {
            Statement stmt = conn.createStatement();

            rs = stmt.executeQuery(query);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }
}