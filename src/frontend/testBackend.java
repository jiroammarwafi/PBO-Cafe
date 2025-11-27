/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package frontend;
import backend.*;
import java.sql.SQLException;

/**
 *
 * @author Admin
 */
public class testBackend {
    public static void main(String[] args) throws SQLException{
        dbHelper tes = new dbHelper();
        tes.bukaKoneksi();
    }
}
