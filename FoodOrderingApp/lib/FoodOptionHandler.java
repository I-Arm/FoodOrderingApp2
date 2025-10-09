package lib;
import javax.swing.*;
import java.awt.*;

public class FoodOptionHandler {

    public static void showFoodOption(Component parent, Food food, OrderManager orderManager) {
        String typeOption1, typeOption2;
        double price1 = food.getNormalPrice();
        double price2 = food.getSpecialPrice();

        if (food.getCategory().equals("เครื่องดื่ม")) {
            typeOption1 = "เล็ก";
            typeOption2 = "ใหญ่";
        } else { 
            typeOption1 = "ปกติ";
            typeOption2 = "พิเศษ";
        }

        Object[] options = {
            typeOption1 + " (" + price1 + " บาท)",
            typeOption2 + " (" + price2 + " บาท)"
        };

        int choice = JOptionPane.showOptionDialog(
            parent,
            "เลือกขนาดสำหรับ " + food.getName(),
            "เลือกราคา",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            orderManager.addToOrder(food, typeOption1, price1);
        } else if (choice == JOptionPane.NO_OPTION) {
            orderManager.addToOrder(food, typeOption2, price2);
        }
    }
}