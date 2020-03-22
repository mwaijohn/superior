package superior.com.superior.database;

public class Suppliers {
    int _id;
    String supp_name;
    String contact;
    String supplier_id;

    public Suppliers(){};

    public Suppliers(int _id, String supp_name, String contact, String supplier_id) {
        this._id = _id;
        this.supp_name = supp_name;
        this.contact = contact;
        this.supplier_id = supplier_id;
    }

    public Suppliers(String supp_name, String contact, String supplier_id) {
        this.supp_name = supp_name;
        this.contact = contact;
        this.supplier_id = supplier_id;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getSupp_name() {
        return supp_name;
    }

    public void setSupp_name(String supp_name) {
        this.supp_name = supp_name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getSupplier_id() {
        return supplier_id;
    }

    public void setSupplier_id(String supplier_id) {
        this.supplier_id = supplier_id;
    }
}
