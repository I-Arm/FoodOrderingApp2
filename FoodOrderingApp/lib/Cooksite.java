package lib;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Cooksite extends JPanel {
    
    private JTextArea displayArea;

    public Cooksite() {
        setLayout(new BorderLayout());
        
        // การตั้งค่า JTextArea
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Tahoma", Font.BOLD, 18)); 
        
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setPreferredSize(new Dimension(450, 600)); // เพิ่มขนาดเล็กน้อย

        // ส่วนหัว
        JLabel header = new JLabel("<< รายการอาหารที่ต้องทำ (ตามคิว) >>", SwingConstants.CENTER);
        header.setFont(new Font("Tahoma", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // ส่วนปุ่ม
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10)); 
        
        JButton refreshBtn = new JButton("🔄 รีเฟรชรายการ");
        refreshBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
        refreshBtn.addActionListener(e -> loadAndDisplayOrders());
        buttonPanel.add(refreshBtn);

        JButton completeBtn = new JButton("✅ ทำคิวบนสุดเสร็จแล้ว");
        completeBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
        completeBtn.addActionListener(e -> completeTopQueue());
        buttonPanel.add(completeBtn);

        add(buttonPanel, BorderLayout.SOUTH);
        loadAndDisplayOrders();
    }

    /**
     * ดึงรายการที่รอทำ (Pending) จาก BillHistorySaver และแสดงผล
     */
    private void loadAndDisplayOrders() {
        displayArea.setText("");
        StringBuilder sb = new StringBuilder();
        int totalOrders = 0;
        
        try {
            // ดึงรายการ Pending Orders จาก BillHistorySaver
            // ข้อมูลที่ได้: List<String[]> โดยที่แต่ละ String[] คือ {Queue, Timestamp, Items_Summary}
            java.util.List<String[]> pendingOrders = lib.BillHistorySaver.getPendingOrders();
            totalOrders = pendingOrders.size();

            if (totalOrders == 0) {
                sb.append("\n\n\n\n\n\n\n\n")
                  .append(String.format("%45s\n", ">> ไม่มีคิวที่รอทำในขณะนี้ <<"));
            } else {
                for (String[] order : pendingOrders) {
                    String queue = order[0];
                    String timestamp = order[1];
                    String itemsSummary = order[2];

                    // หัวคิว
                    sb.append("==================================================\n");
                    sb.append(String.format("📢 คิวที่: %s  (%s)\n", queue, timestamp));
                    sb.append("--------------------------------------------------\n");
                    sb.append(String.format("%-25s %-10s %s\n", "เมนู", "ขนาด", "จำนวน"));
                    sb.append("--------------------------------------------------\n");

                    // แยกรายการอาหารแต่ละชนิดใน Summary
                    String[] items = itemsSummary.split(" \\| "); // แยกตามตัวแบ่ง " | "

                    for (String item : items) {
                        // รายละเอียด: Cat/Menu/Type/Price_Per_Unit/Quantity
                        String[] details = item.split("/");
                        
                        // FIX: ดึง Quantity (Index 4)
                        if (details.length >= 5) { 
                            String menu = details[1];
                            String type = details[2];
                            String quantity = details[4]; // <<< Quantity
                            
                            // แสดงผล: เมนู, ขนาด, จำนวน
                            sb.append(String.format("  %-23s %-10s %s\n", menu, type, quantity));
                        } else {
                            sb.append("  !! รายการผิดพลาด: ").append(item).append("\n");
                        }
                    }
                    sb.append("==================================================\n\n");
                }
            }

        } catch (IOException e) {
            sb.append("\n\n!! เกิดข้อผิดพลาดในการอ่านไฟล์ประวัติบิล !!\n");
            e.printStackTrace();
        }
        
        displayArea.setText(sb.toString());
        
        if (totalOrders > 0) {
             displayArea.append(String.format("\n\n>> มีทั้งหมด %d คิวที่รอทำ <<\n", totalOrders));
        }
    }

    private void completeTopQueue() {
        try {
            boolean success = lib.BillHistorySaver.markTopQueueAsCompleted();
            if (success) {
                JOptionPane.showMessageDialog(this, "คิวบนสุดทำเสร็จแล้ว ถูกลบออกจากรายการ", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "ไม่มีคิวที่ต้องทำแล้ว", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
            }
            loadAndDisplayOrders(); // โหลดรายการใหม่เพื่ออัปเดตหน้าจอ
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "เกิดข้อผิดพลาดในการอัปเดตไฟล์: " + e.getMessage(), "ข้อผิดพลาด I/O", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void showCookSite() {
        JFrame frame = new JFrame("หน้าจอครัว (Kitchen Display)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new Cooksite());
        frame.pack(); 
        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);
    }
}