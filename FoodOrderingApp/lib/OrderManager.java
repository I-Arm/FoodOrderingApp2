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
        // [Category, Menu, Type, Price]
        tableModel.addRow(new Object[]{food.getCategory(), food.getName(), type, price});
        total += price;
        updateTotalLabel();
    }

    public void removeSelectedItemByClick(JTable orderTable, int selectedRow) {
    
        String menu = tableModel.getValueAt(selectedRow, 1).toString(); 
        String category = tableModel.getValueAt(selectedRow, 0).toString(); 

        // Pop-up ยืนยันการลบ
        int confirm = JOptionPane.showOptionDialog(
                    orderTable.getParent(), 
                    "คุณต้องการลบเมนู '" + menu + "' (" + category + ") ออกจากรายการหรือไม่?",
                    "ยืนยันการลบ", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[]{"ลบ", "ยกเลิก"},
                    "ยกเลิก"
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // ดึงราคาจากคอลัมน์ที่ 3
                double price = Double.parseDouble(tableModel.getValueAt(selectedRow, 3).toString());
                
                total -= price;
                tableModel.removeRow(selectedRow);
                updateTotalLabel();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(orderTable.getParent(), "เกิดข้อผิดพลาดในการลบ: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // 1. เมธอดที่หน้าชำระเงินใช้ในการดึงยอดรวม
    public double calculateTotal() {
        return total;
    }
    
    // 2. เมธอดสำหรับล้างรายการสั่งซื้อทั้งหมดหลังชำระเงินเสร็จสิ้น
    public void clearOrder() {
        tableModel.setRowCount(0); // ล้างรายการในตาราง
        this.total = 0.0; // รีเซ็ตยอดรวม
        updateTotalLabel(); // อัปเดต Label ให้เป็นศูนย์
    }


    private void updateTotalLabel() {
        totalLabel.setText(String.format("รวมทั้งหมด: %s บาท", df.format(total)));
    }
    
    // ลบ public double getTotal() ออกไป แล้วใช้ calculateTotal() แทน
}