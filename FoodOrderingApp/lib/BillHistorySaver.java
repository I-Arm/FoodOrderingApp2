package lib;

import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BillHistorySaver {
    
    private static final String HISTORY_FILE = "data/bill_history.csv";
    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void saveBill(DefaultTableModel orderModel, double total, double paid, double change) throws IOException {
        
        // 1. สร้างโฟลเดอร์ data ถ้ายังไม่มี
        java.io.File dataDir = new java.io.File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // 2. ตรวจสอบว่าไฟล์มีอยู่หรือไม่ (เพื่อตัดสินใจว่าจะใส่ Header หรือไม่)
        boolean fileExists = new java.io.File(HISTORY_FILE).exists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_FILE, true))) {
            
            // 3. บันทึก Header (เฉพาะครั้งแรก)
            if (!fileExists) {
                writer.println("Timestamp,Total,Paid,Change,Items_Summary");
            }
            
            // 4. บันทึกรายละเอียดบิล
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(FILE_FORMATTER);
            
            StringBuilder itemSummary = new StringBuilder();
            for (int i = 0; i < orderModel.getRowCount(); i++) {
                String category = orderModel.getValueAt(i, 0).toString();
                String menu = orderModel.getValueAt(i, 1).toString();
                String type = orderModel.getValueAt(i, 2).toString();
                String price = orderModel.getValueAt(i, 3).toString();
                
                // เข้ารหัสรายการเป็น String เดียว (ใช้ | เป็นตัวแบ่งรายการภายในบิล)
                itemSummary.append(String.format("%s/%s/%s/%.2f", category, menu, type, Double.parseDouble(price)));
                if (i < orderModel.getRowCount() - 1) {
                    itemSummary.append(" | ");
                }
            }
            
            // 5. บันทึก Transaction หลัก
            writer.printf("%s,%.2f,%.2f,%.2f,\"%s\"\n", 
                timestamp, total, paid, change, itemSummary.toString());
        }
    }
}