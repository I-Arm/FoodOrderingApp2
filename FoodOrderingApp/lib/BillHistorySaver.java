package lib;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BillHistorySaver {
    
    private static final String HISTORY_FILE = "data/bill_history.csv";
    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * บันทึกบิลลงในไฟล์ CSV พร้อมสถานะ "Pending" และคืนค่าเลขคิว
     * ตารางมี 5 คอลัมน์: [Category, Menu, Type, Price/Unit, Quantity]
     */
    public static int saveBill(DefaultTableModel orderModel, double total, double paid, double change) throws IOException {
        
        java.io.File dataDir = new java.io.File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File file = new File(HISTORY_FILE);
        boolean fileExists = file.exists();
        int nextQueue = 1;

        // นับเลขคิวถัดไป
        if (fileExists) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                
                br.mark(1024);
                if (br.read() != 0xEF || br.read() != 0xBB || br.read() != 0xBF) {
                    br.reset(); 
                }
                
                int dataRowCount = 0;
                if (br.readLine() != null) { // ข้าม Header
                    while (br.readLine() != null) {
                        dataRowCount++; 
                    }
                }
                nextQueue = dataRowCount + 1;
                
            } catch (FileNotFoundException e) {
                 // เริ่มที่คิว 1
            }
        }
        
        boolean writeBOM = !fileExists;
        try (FileOutputStream os = new FileOutputStream(file, true);
             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {
            
            if (writeBOM) {
                os.write(0xEF); os.write(0xBB); os.write(0xBF);
            }

            if (!fileExists) {
                writer.println("Que,Timestamp,Total,Paid,Change,Items_Summary,Status"); 
            }
            
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(FILE_FORMATTER);
            String queue = String.valueOf(nextQueue);
            
            // ItemSummary (รองรับ 5 คอลัมน์: Price/Unit, Quantity)
            StringBuilder itemSummary = new StringBuilder();
            for (int i = 0; i < orderModel.getRowCount(); i++) {
                String pricePerUnit = orderModel.getValueAt(i, 3).toString();
                String quantity = orderModel.getValueAt(i, 4).toString();

                itemSummary.append(String.format("%s/%s/%s/%.2f/%d", 
                    orderModel.getValueAt(i, 0).toString(), 
                    orderModel.getValueAt(i, 1).toString(), 
                    orderModel.getValueAt(i, 2).toString(), 
                    Double.parseDouble(pricePerUnit), 
                    Integer.parseInt(quantity)));
                if (i < orderModel.getRowCount() - 1) {
                    itemSummary.append(" | "); 
                }
            }
            
            // บันทึก Transaction หลัก (Status: Pending)
            writer.printf("%s,%s,%.2f,%.2f,%.2f,\"%s\",Pending\n", 
                queue, timestamp, total, paid, change, itemSummary.toString());
        }
        
        return nextQueue;
    }

    /**
     * ดึงรายการบิลที่มีสถานะ "Pending" ทั้งหมด
     * @return List ของ String array {Queue, Timestamp, Items_Summary}
     */
    public static List<String[]> getPendingOrders() throws IOException {
        List<String[]> pendingOrders = new ArrayList<>();
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            return pendingOrders;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            
            br.mark(1024);
            if (br.read() != 0xEF || br.read() != 0xBB || br.read() != 0xBF) {
                br.reset(); 
            }
            
            String line = br.readLine(); // ข้าม header
            while ((line = br.readLine()) != null) {
                // Regex สำหรับแยก CSV ที่มีคอมมาภายในเครื่องหมายคำพูด
                String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); 
                
                if (parts.length >= 7) {
                    String status = parts[6].trim();
                    if (status.equalsIgnoreCase("Pending")) {
                        // ดึงข้อมูลที่ Cooksite ต้องการ: Que, Timestamp, Items_Summary
                        // .replaceAll("^\"|\"$", "") เพื่อลบเครื่องหมายคำพูดที่ล้อม Items_Summary
                        pendingOrders.add(new String[]{parts[0], parts[1], parts[5].replaceAll("^\"|\"$", "")}); 
                    }
                }
            }
        }
        return pendingOrders;
    }

    /**
     * เปลี่ยนสถานะคิว "Pending" แรกสุด ให้เป็น "Completed"
     * @return true ถ้าทำสำเร็จ, false ถ้าไม่มีคิว Pending
     */
    public static boolean markTopQueueAsCompleted() throws IOException {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            return false;
        }
        
        List<String> lines = new ArrayList<>();
        int pendingQueueRow = -1; // Index ของแถว Pending แรกสุด (หลัง Header)
        int lineNumber = 0;
        
        // 1. อ่านข้อมูลทั้งหมด
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            
            br.mark(1024);
            if (br.read() != 0xEF || br.read() != 0xBB || br.read() != 0xBF) {
                br.reset(); 
            }
            
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
                
                if (lineNumber > 0 && pendingQueueRow == -1) { 
                    String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (parts.length >= 7 && parts[6].trim().equalsIgnoreCase("Pending")) {
                        pendingQueueRow = lineNumber; // บันทึก Index ของแถว Pending แรก
                    }
                }
                lineNumber++;
            }
        }
        
        if (pendingQueueRow == -1) {
            return false; // ไม่มีคิวที่ต้องทำ
        }

        // 2. อัปเดตสถานะของแถวนั้นเป็น "Completed"
        String targetLine = lines.get(pendingQueueRow);
        String updatedLine = targetLine.replaceAll("Pending$", "Completed");
        lines.set(pendingQueueRow, updatedLine);
        
        // 3. เขียนไฟล์ทั้งหมดทับ
        try (FileOutputStream os = new FileOutputStream(file, false); // false = เขียนทับ
             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {
            
            // เขียน BOM
            os.write(0xEF); os.write(0xBB); os.write(0xBF);
            
            for (String line : lines) {
                writer.println(line);
            }
        }
        
        return true;
    }
}