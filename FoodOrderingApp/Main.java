import javax.swing.*;
import java.awt.*;
import java.util.List;
import lib.*; 


public class Main { 
    public static void main(String[] args) {
    
        // ตั้ง font ให้อ่านไทยได้ทั้งหมด
        Font thaiFont = new Font("Tahoma", Font.PLAIN, 16);
        UIManager.put("Label.font", thaiFont);
        UIManager.put("Button.font", thaiFont);
        UIManager.put("Table.font", thaiFont);
        UIManager.put("TableHeader.font", thaiFont);
        UIManager.put("TextField.font", thaiFont);
        UIManager.put("TextArea.font", thaiFont);
        UIManager.put("OptionPane.messageFont", thaiFont);
        UIManager.put("OptionPane.buttonFont", thaiFont);

        SwingUtilities.invokeLater(() -> {
            // โหลดข้อมูลจาก data/menu.csv
            List<Food> foods = FoodMenuLoader.loadFoodFromCSV("data/menu.csv");
            
            if (foods.isEmpty()) {
                JOptionPane.showMessageDialog(null, 
                    "ไม่พบข้อมูลเมนูในไฟล์ data/menu.csv หรือไฟล์เสียหาย", 
                    "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new FoodOrderingGUI(foods).setVisible(true); 
        });
    }
}