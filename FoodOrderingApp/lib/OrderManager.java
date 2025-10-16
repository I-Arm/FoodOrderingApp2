package lib;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat; // เพิ่ม Import สำหรับ DecimalFormat

public class OrderManager {
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private double total = 0.0;
    
    // ใช้ DecimalFormat เพื่อจัดรูปแบบตัวเลขให้ดูดีขึ้น
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public OrderManager(DefaultTableModel tableModel, JLabel totalLabel) {
        this.tableModel = tableModel;
        this.totalLabel = totalLabel;
    }

    public void addToOrder(Food food, String type, double price) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingMenu = tableModel.getValueAt(i, 1).toString();
            String existingType = tableModel.getValueAt(i, 2).toString();
            
            // ถ้าชื่อเมนูและขนาด (Type) เหมือนกัน
            if (existingMenu.equals(food.getName()) && existingType.equals(type)) {
                // อัปเดตจำนวน
                int currentQuantity = Integer.parseInt(tableModel.getValueAt(i, 4).toString());
                tableModel.setValueAt(currentQuantity + 1, i, 4);
                
                // คำนวณยอดรวมใหม่และอัปเดต Label
                calculateTotal(); // คำนวณยอดรวมใหม่ทั้งหมด
                return; // หยุดการทำงาน
            }
        }
        tableModel.addRow(new Object[]{food.getCategory(), food.getName(), type, price, 1});
        
        calculateTotal(); 
    }

    public void removeSelectedItemByClick(JTable orderTable, int selectedRow) {
    
        String menu = tableModel.getValueAt(selectedRow, 4).toString(); 
        String category = tableModel.getValueAt(selectedRow, 0).toString(); 
        int currentQuantity = Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString());

        // Pop-up ยืนยันการลบ
        int confirm = JOptionPane.showOptionDialog(
                    orderTable.getParent(), 
                    "คุณต้องการลบเมนู '" + menu + "' (" + category + ") ออกจากรายการหรือไม่?",
                    "ยืนยันการลบ", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[]{"ลบ/ลดจำนวน", "ยกเลิก"},
                    "ยกเลิก"
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (currentQuantity > 1) {
                    // ถ้าจำนวนมากกว่า 1 ให้ลดจำนวนลง 1
                    tableModel.setValueAt(currentQuantity - 1, selectedRow, 4);
                } else {
                    // ถ้าจำนวนเป็น 1 ให้ลบแถวออก
                    tableModel.removeRow(selectedRow);
                }
                
                calculateTotal(); // คำนวณยอดรวมใหม่ทั้งหมด
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(orderTable.getParent(), "เกิดข้อผิดพลาดในการลบ: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // 1. เมธอดที่หน้าชำระเงินใช้ในการดึงยอดรวม
    public double calculateTotal() {
        double newTotal = 0.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                // คอลัมน์ 3 คือ "ราคา/หน่วย"
                double pricePerUnit = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
                // คอลัมน์ 4 คือ "จำนวน"
                int quantity = Integer.parseInt(tableModel.getValueAt(i, 4).toString()); 
                
                newTotal += (pricePerUnit * quantity);
                
            } catch (NumberFormatException e) {
                // จัดการข้อผิดพลาดถ้าข้อมูลในตารางไม่ใช่ตัวเลข
                System.err.println("Error parsing price or quantity in row " + i);
            }
        }
        this.total = newTotal;
        updateTotalLabel();
        return this.total;
    }
    
    // 2. เมธอดสำหรับล้างรายการสั่งซื้อทั้งหมดหลังชำระเงินเสร็จสิ้น
    public void clearOrder() {
        tableModel.setRowCount(0); // ล้างรายการในตาราง
        this.total = 0.0; // รีเซ็ตยอดรวม
        updateTotalLabel(); // อัปเดต Label ให้เป็นศูนย์
    }


    private void updateTotalLabel() {
        totalLabel.setText(String.format("รวมทั้งหมด: %s บาท", df.format(total) + "บาท"));
    }
    
    // ลบ public double getTotal() ออกไป แล้วใช้ calculateTotal() แทน
}