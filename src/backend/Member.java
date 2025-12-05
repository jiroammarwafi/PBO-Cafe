package backend;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Member {
    private Integer idMember;
    private String namaMember;
    private String noTelp;
    private Integer points;
    private LocalDateTime tanggalJoin;
    
    // Konstruktor
    public Member() {}
    
    public Member(Integer idMember, String namaMember, String noTelp, 
                  Integer points, LocalDateTime tanggalJoin) {
        this.idMember = idMember;
        this.namaMember = namaMember;
        this.noTelp = noTelp;
        this.points = points;
        this.tanggalJoin = tanggalJoin;
    }
    
    // Getters and Setters
    public Integer getIdMember() { return idMember; }
    public void setIdMember(Integer idMember) { this.idMember = idMember; }
    
    public String getNamaMember() { return namaMember; }
    public void setNamaMember(String namaMember) { this.namaMember = namaMember; }
    
    public String getNoTelp() { return noTelp; }
    public void setNoTelp(String noTelp) { this.noTelp = noTelp; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    
    public LocalDateTime getTanggalJoin() { return tanggalJoin; }
    public void setTanggalJoin(LocalDateTime tanggalJoin) { this.tanggalJoin = tanggalJoin; }
    
    // Database dengan dbHelper
    public void save() {
        if (this.idMember == null || this.idMember == 0) {
            insert();
        } else {
            update();
        }
    }
    
    private void insert() {
        String sql = "INSERT INTO member (nama_member, no_telp, points, tanggal_join) " +
                    "VALUES ('" + escapeSql(this.namaMember) + "', '" + escapeSql(this.noTelp) + "', " + 
                    (this.points != null ? this.points : 0) + ", " +
                    "CURRENT_TIMESTAMP)";
        
        int generatedId = dbHelper.insertQueryGetId(sql);
        if (generatedId != -1) {
            this.idMember = generatedId;
        }
    }
    
    private void update() {
        String sql = "UPDATE member SET nama_member = '" + escapeSql(this.namaMember) + 
                    "', no_telp = '" + escapeSql(this.noTelp) + 
                    "', points = " + (this.points != null ? this.points : 0) + 
                    " WHERE id_member = " + this.idMember;
        
        dbHelper.executeQuery(sql);
    }
    
    public boolean delete() {
        String sql = "DELETE FROM member WHERE id_member = " + this.idMember;
        return dbHelper.executeQuery(sql);
    }
    
    // Methods for database operations (non-static for consistency)
    public Member getById(int id) {
        String sql = "SELECT * FROM member WHERE id_member = " + id;
        ResultSet rs = dbHelper.selectQuery(sql);
        
        try {
            if (rs.next()) {
                return mapResultSetToMember(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting member by ID: " + e.getMessage());
        }
        return null;
    }
    
    public List<Member> getAll() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member ORDER BY id_member";
        ResultSet rs = dbHelper.selectQuery(sql);
        
        try {
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all members: " + e.getMessage());
        }
        return members;
    }
    
    public List<Member> search(String keyword) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE " +
                    "LOWER(nama_member) LIKE LOWER('%" + escapeSql(keyword) + "%') OR " +
                    "no_telp LIKE '%" + escapeSql(keyword) + "%' " +
                    "ORDER BY id_member";
        
        ResultSet rs = dbHelper.selectQuery(sql);
        try {
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching members: " + e.getMessage());
        }
        return members;
    }
    
    // Helper method to prevent SQL injection
    private static String escapeSql(String input) {
        if (input == null) return "";
        return input.replace("'", "''");
    }
    
    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setIdMember(rs.getInt("id_member"));
        member.setNamaMember(rs.getString("nama_member"));
        member.setNoTelp(rs.getString("no_telp"));
        member.setPoints(rs.getInt("points"));
        
        Timestamp timestamp = rs.getTimestamp("tanggal_join");
        if (timestamp != null) {
            member.setTanggalJoin(timestamp.toLocalDateTime());
        }
        
        return member;
    }
    
    @Override
    public String toString() {
        return "Member{" +
                "idMember=" + idMember +
                ", namaMember='" + namaMember + '\'' +
                ", noTelp='" + noTelp + '\'' +
                ", points=" + points +
                ", tanggalJoin=" + tanggalJoin +
                '}';
    }
}