package superior.com.superior.models;

public class AnimalNames {

    private String supplier_name;
    private String contact;
    private String supplier_id;


    public AnimalNames(String supplier_name, String contact, String supplier_id) {
        this.supplier_name = supplier_name;
        this.contact = contact;
        this.supplier_id = supplier_id;
    }

    public String getSupplier_name() {
        return supplier_name;
    }

    public String getContact() {
        return contact;
    }

    public String getSupplier_id() {
        return supplier_id;
    }

//    public AnimalNames(String animalName) {
//        this.animalName = animalName;
//    }
//
//    public String getAnimalName() {
//        return this.animalName;
//    }





}