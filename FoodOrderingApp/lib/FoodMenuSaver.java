package lib;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class FoodMenuSaver {

    private static final String FILENAME = "data/menu.csv";

    public static void saveMenuToCSV(DefaultTableModel model) throws IOException {
        try (PrintWriter pw = new PrintWriter(
            new OutputStreamWriter(new FileOutputStream(FILENAME), StandardCharsets.UTF_8))) 
        {
            // 1. เขียน Header
            pw.println("Name,Normal,Special,Image,Category");
            
            // 2. วนลูปเขียนข้อมูลในแต่ละแถว
            Vector<Vector> dataVector = model.getDataVector();
            for (Vector row : dataVector) {
                StringBuilder sb = new StringBuilder();
                
                if (row.size() >= 5) {
                    // ชื่อเมนู
                    sb.append(escapeCSV(row.get(0).toString())).append(","); 
                    // ราคาปกติ
                    sb.append(escapeCSV(row.get(1).toString())).append(","); 
                    // ราคาพิเศษ
                    sb.append(escapeCSV(row.get(2).toString())).append(","); 
                    // รูปภาพ
                    sb.append(escapeCSV(row.get(3).toString())).append(","); 
                    // ประเภท
                    sb.append(escapeCSV(row.get(4).toString())); 
                }
                pw.println(sb.toString());
            }
        }
    }
    
    // Helper function สำหรับจัดการเครื่องหมาย , และ " ในข้อมูล CSV
    private static String escapeCSV(String value) {
        if (value.contains("\"")) {
            value = value.replace("\"", "\"\"");
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value + "\"";
        }
        return value;
    }
}
