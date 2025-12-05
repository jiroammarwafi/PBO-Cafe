package backend;

public class DetailPesanan {
    private int idDetail, idOrder, idMenu, harga, qty, subtotal;
    private String catatan;

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }
    public String getCatatan() {
        return catatan;
    }
    public void setHarga(int harga) {
        this.harga = harga;
    }
    public int getHarga() {
        return harga;
    }
    public void setIdDetail(int idDetail) {
        this.idDetail = idDetail;
    }
    public int getIdDetail() {
        return idDetail;
    }
    public void setIdMenu(int idMenu) {
        this.idMenu = idMenu;
    }
    public int getIdMenu() {
        return idMenu;
    }
    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }
    public int getIdOrder() {
        return idOrder;
    }
    public void setQty(int qty) {
        this.qty = qty;
    }
    public int getQty() {
        return qty;
    }
    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }
    public int getSubtotal() {
        return subtotal;
    }

    public DetailPesanan(){}
    public DetailPesanan(int idDetail, int idOrder, int idMenu, int harga, int qty, String catatan){
        this.idDetail = idDetail;
        this.idMenu = idMenu;
        this.idOrder = idOrder;
        this.harga = harga;
        this.catatan = catatan;
        this.qty = qty;
        this.subtotal = qty * harga;
    }
}
