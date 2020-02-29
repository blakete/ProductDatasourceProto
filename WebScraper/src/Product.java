import com.google.gson.annotations.Expose;

public class Product {
    @Expose(serialize = false, deserialize = false)
    private int statusCode;
    @Expose
    private String upc;
    @Expose
    private String ean; // todo other codes
    @Expose
    private String name;
    @Expose
    private String[] codes;
    @Expose
    private String category;
    @Expose
    private String manufacturer;
    @Expose
    private String pictureURL;

    public Product() {

    }

    public Product(String name, String[] codes, String category, String manufacturer) {
        this.name = name;
        this.codes = codes;
        this.category = category;
        this.manufacturer = manufacturer;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getCodes() {
        return codes;
    }

    public void setCodes(String[] codes) {
        this.codes = codes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public String toString() {
        String codes = "";
        for (String code : this.codes)
            codes += code + ", ";
        return "name: " + this.name + ",\ncategory: " + this.category + ", \nmanufacturer: "
                + this.manufacturer + ", \ncodes: " + codes;
    }


}
