package lib;
import java.awt.Font;
import java.io.*;
import java.util.*;

public class FoodMenuLoader {
    public static List<Food> loadFoodFromCSV(String filename) {
        List<Food> foodList = new ArrayList<>();
        // Font thaiFont = new Font("Tahoma", Font.PLAIN, 16); // ไม่จำเป็นต้องใช้ที่นี่

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
                         
            String line;
            br.readLine(); // ข้าม header
            while ((line = br.readLine()) != null) {
                // ต้องระวัง: หากชื่อเมนูมีคอมมา อาจจะใช้ตัวแบ่งอื่น เช่น แท็บ หรือ Semicolon
                String[] parts = line.split(","); 
                if (parts.length == 5) { // ต้องมี 5 คอลัมน์
                    String name = parts[0];
                    double normal = Double.parseDouble(parts[1].trim()); // .trim() เพื่อลบช่องว่าง
                    double special = Double.parseDouble(parts[2].trim());
                    String img = parts[3].trim();
                    String category = parts[4].trim(); 
                    
                    foodList.add(new Food(name, normal, special, img, category));
                }
            }
        } catch (FileNotFoundException e) {
             System.err.println("File not found: " + filename);
             // ไม่ต้อง throw ให้ FoodOrderingGUI จัดการ
        } catch (Exception e) {
            e.printStackTrace();
        }
        return foodList;
    }
}