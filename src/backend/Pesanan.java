package backend;
import java.time.LocalDate;
import java.time.LocalTime;

public class Pesanan{
    private int idOrder, noMeja;
    private String nama;
    private LocalTime waktuPesan;
    private LocalDate tanggalPesan;

    // --- Getters ---
    public int getIdOrder () {
        return idOrder;
    }

    public void setIdOrder (int newId) {
        this.idOrder = newId;
    }

    public String getNama () {
        return nama;
    }

    public void setNama (String newNama) {
        this.nama = newNama;
    }

    public int getNoMeja () {
        return noMeja;
    }

    public void setNoMeja (int NewNoMeja) {
        this.noMeja = NewNoMeja;
    }
    public LocalTime getWaktuPesan() {
        return waktuPesan;
    }
    public void setWaktuPesan(LocalTime waktuPesan) {
        this.waktuPesan = waktuPesan;
    }
    public void setTanggalPesan(LocalDate tanggalPesan) {
        this.tanggalPesan = tanggalPesan;
    }public LocalDate getTanggalPesan() {
        return tanggalPesan;
    }
    // --- Constructors ---
    public Pesanan () {}

    public Pesanan (String nama, int noMeja, LocalTime waktuPesan, LocalDate tanggalPesan) {
        this.nama = nama;
        this.noMeja = noMeja;
        this.tanggalPesan = tanggalPesan;
        this.waktuPesan = waktuPesan;
    }
}
