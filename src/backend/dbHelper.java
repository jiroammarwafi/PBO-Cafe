/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Admin
 */
public class dbHelper {
    private static Connection koneksi;

    public static void bukaKoneksi(){
        if (koneksi == null) {
            try {
                String url = "jdbc:postgresql://localhost:5432/kasir_cafe";
                String user = "postgres";
                String password = "1234";

                Class.forName("org.postgresql.Driver");

                koneksi = DriverManager.getConnection(url, user, password);

                System.out.println("koneksi successed");

            } catch (ClassNotFoundException ex) {
                System.err.println("PostgreSQL JDBC Driver not found: " + ex.getMessage());
            } catch (SQLException ex) {
                System.err.println("Failed to connect to database: " + ex.getMessage());
            }
        }
    }

    public static int insertQueryGetId(String query){
        bukaKoneksi();
        int num = 0;
        int result = -1;
        try{
            Statement stmt = koneksi.createStatement();
            num = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
            {
                result = rs.getInt(1);
            }
            rs.close();
            stmt.close();
        }catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        return result;
        }
    public static boolean executeQuery(String query)
        {
        bukaKoneksi();
        boolean result = false;
        try
        {
        Statement stmt = koneksi.createStatement();
        stmt.executeUpdate(query);
        result = true;
        stmt.close();
        }
        catch (Exception e)
        {
        e.printStackTrace();
        }

        // Database connection info (for reference):
        // Nama database: dbperpus
        // Port: 5432 (PostgreSQL default; 3306 is MySQL default)
        // Alamat server: localhost
        // Username: postgres
        // Password: 170206

        return result;
        }
        public static ResultSet selectQuery(String query)
        {
        bukaKoneksi();
        ResultSet rs = null;
        try
        {
        Statement stmt = koneksi.createStatement();
        rs = stmt.executeQuery(query);
        }
        catch (Exception e)
        {
        e.printStackTrace();
        }
        return rs;
        }
}
