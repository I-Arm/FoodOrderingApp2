package lib;
public class Food {
    String name;
    private double normalPrice;
    private double specialPrice;
    private String imagePath;
    private String category; 

    public Food(String name, double normalPrice, double specialPrice, String imagePath, String category) {
        this.name = name;
        this.normalPrice = normalPrice;
        this.specialPrice = specialPrice;
        this.imagePath = imagePath;
        this.category = category;
    }

    public String getName() { return name; }
    public double getNormalPrice() { return normalPrice; }
    public double getSpecialPrice() { return specialPrice; }
    public String getImagePath() { return imagePath; }
    public String getCategory() { return category; }
}