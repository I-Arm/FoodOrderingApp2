package lib;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.event.DocumentEvent;
import java.io.IOException; // <--- เพิ่ม Import ตัวนี้

public class FoodOrderingGUI extends JFrame { 
    private JTable orderTable;
    private DefaultTableModel tableModel; // <--- ตัวแปรที่ถูกต้อง
    private JLabel totalLabel;
    private JPanel menuPanel;
    private List<Food> foodList;
    private OrderManager orderManager;
    private AdminPanelManager adminPanelManager; 
    

    public FoodOrderingGUI(List<Food> foodList) {
        this.foodList = foodList;
        
        // สร้าง Manager Objects 
        String[] columns = {"ประเภท", "เมนู", "ขนาด", "ราคา"}; 
        tableModel = new DefaultTableModel(columns, 0); // <--- ใช้ tableModel
        totalLabel = new JLabel("รวมทั้งหมด: 0.0 บาท");
        
        this.orderManager = new OrderManager(tableModel, totalLabel);
        this.adminPanelManager = new AdminPanelManager(this, foodList); 
        
        
        setTitle("ระบบสั่งอาหาร");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Font thaiFont = new Font("Tahoma", Font.PLAIN, 16);
        
        // ส่วนเมนู (ซ้าย)
        menuPanel = new JPanel(); 
        menuPanel.setLayout(new GridLayout(0, 3, 10, 10));
        
        reloadMenuButtons();
        
        // ส่วนตะกร้า (ขวา)
        orderTable = new JTable(tableModel);
        orderTable.setFont(thaiFont);
        orderTable.setRowHeight(30);
        orderTable.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(orderTable);

        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.add(scrollPane, BorderLayout.CENTER);
        orderPanel.add(totalLabel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("บันทึก");
        JButton payBtn = new JButton("ชำระเงิน");
        JButton adminBtn = new JButton("จัดการระบบ");
        
        saveBtn.setFont(thaiFont);
        payBtn.setFont(thaiFont);
        adminBtn.setFont(thaiFont);
        
        adminBtn.setVisible(false);
        adminBtn.addActionListener(e -> adminPanelManager.openAdminPanel()); 
        saveBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "บันทึกออเดอร์เรียบร้อย!"));
        
        // *********** แก้ไขตรงนี้: เชื่อมต่อปุ่มชำระเงิน ***********
        payBtn.addActionListener(e -> openPaymentDialog()); 
        // *******************************************************

        buttonPanel.add(saveBtn);
        buttonPanel.add(payBtn);
        buttonPanel.add(adminBtn); 

        orderPanel.add(buttonPanel, BorderLayout.NORTH);

        add(new JScrollPane(menuPanel), BorderLayout.CENTER);
        add(orderPanel, BorderLayout.EAST);

        // Listener สำหรับแสดงปุ่มลบ
        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { 
                    int selectedRow = orderTable.getSelectedRow();
                    if (selectedRow != -1) {
                        orderManager.removeSelectedItemByClick(orderTable, selectedRow); 
                    }
                }
            }
        });
        
        
        // --- Admin Login Logic ---
        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(thaiFont);

        loginBtn.addActionListener(e -> {
            if (loginBtn.getText().equals("Login")) {
                JTextField userField = new JTextField();
                JPasswordField passField = new JPasswordField();
                Object[] fields = {"Username:", userField, "Password:", passField};

                int option = JOptionPane.showConfirmDialog(this, fields, "Admin Login", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String user = userField.getText();
                    String pass = new String(passField.getPassword());
                    if (user.equals("admin") && pass.equals("1234")) {
                        JOptionPane.showMessageDialog(this, "เข้าสู่ระบบ Admin สำเร็จ!");
                        loginBtn.setText("Admin: Logout");
                        adminBtn.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Username หรือ Password ไม่ถูกต้อง");
                    }
                }
            } else {
                loginBtn.setText("Login");
                JOptionPane.showMessageDialog(this, "ออกจากระบบแล้ว");
                adminBtn.setVisible(false);
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(loginBtn);
        add(topPanel, BorderLayout.NORTH);
    }
 
    private void openPaymentDialog() {
        // ดึงยอดรวมจาก OrderManager (ตอนนี้ OrderManager มีเมธอด calculateTotal() แล้ว)
        double total = orderManager.calculateTotal();

        if (total <= 0) {
            JOptionPane.showMessageDialog(this, "ไม่มีรายการอาหารในตะกร้าเพื่อชำระเงิน", "ข้อผิดพลาด", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DecimalFormat df = new DecimalFormat("#,##0.00");
        Font thaiFont = new Font("Tahoma", Font.PLAIN, 16);

        JDialog paymentDialog = new JDialog(this, "ชำระเงิน", true);
        paymentDialog.setSize(450, 450);
        paymentDialog.setLayout(new BorderLayout(10, 10));
        paymentDialog.setLocationRelativeTo(this);

        // --- 1. ส่วนแสดงรายการและยอดรวม ---
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        displayPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel headerLabel = new JLabel("รายการสั่งซื้อและยอดรวม");
        headerLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea summaryArea = new JTextArea(10, 30);
        summaryArea.setEditable(false);
        summaryArea.setFont(thaiFont);
        summaryArea.setText(getBillSummary(tableModel)); // <--- ใช้ tableModel ที่ถูกต้อง
        
        JLabel totalLabel = new JLabel("ยอดรวมสุทธิ: " + df.format(total) + " บาท");
        totalLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        totalLabel.setForeground(new Color(200, 0, 0));

        displayPanel.add(headerLabel);
        displayPanel.add(Box.createVerticalStrut(10));
        displayPanel.add(new JScrollPane(summaryArea));
        displayPanel.add(Box.createVerticalStrut(10));
        displayPanel.add(totalLabel);
        displayPanel.add(Box.createVerticalStrut(10));
        
        
        // --- 2. ส่วนคำนวณเงิน ---
        JPanel calcPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField paidField = new JTextField(15);
        paidField.setFont(thaiFont);
        JLabel changeLabel = new JLabel("0.00 บาท");
        changeLabel.setFont(new Font("Tahoma", Font.BOLD, 18));

        gbc.gridx = 0; gbc.gridy = 0; calcPanel.add(createLabel("รับเงินมา:", thaiFont), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; calcPanel.add(paidField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; calcPanel.add(createLabel("เงินทอน:", thaiFont), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; calcPanel.add(changeLabel, gbc);
        
        // Listener เพื่อคำนวณเงินทอนแบบ Real-time
        paidField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { calculateChange(); }
            @Override
            public void removeUpdate(DocumentEvent e) { calculateChange(); }
            @Override
            public void changedUpdate(DocumentEvent e) { calculateChange(); }
            
            private void calculateChange() {
                try {
                    double paid = Double.parseDouble(paidField.getText().trim());
                    double change = paid - total;
                    
                    if (change < 0) {
                        changeLabel.setText("ไม่พอ!");
                        changeLabel.setForeground(Color.RED);
                    } else {
                        changeLabel.setText(df.format(change) + " บาท");
                        changeLabel.setForeground(new Color(0, 150, 0)); 
                    }
                } catch (NumberFormatException ex) {
                    changeLabel.setText("ระบุตัวเลข");
                    changeLabel.setForeground(Color.GRAY);
                }
            }
        });

        // --- 3. ปุ่มเสร็จสิ้น ---
        JButton finishBtn = new JButton("เสร็จสิ้น");
        finishBtn.setFont(new Font("Tahoma", Font.BOLD, 18));
        finishBtn.addActionListener(e -> {
            try {
                double paid = Double.parseDouble(paidField.getText().trim());
                if (paid < total) {
                    JOptionPane.showMessageDialog(paymentDialog, "จำนวนเงินที่รับมาไม่เพียงพอ", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double change = paid - total;
        
                // 2. เตรียมใบเสร็จสรุป (ใช้ JTextArea และ JScrollPane เพื่อให้แสดงได้ยาว)
                String billSummaryText = getBillSummary(tableModel);
        
                // 3. บันทึกข้อมูลสรุปก่อนเคลียร์
                String receiptDetails = billSummaryText + 
                "\n\n==============================================\n" +
                String.format("%-40s %10s\n", "รวมทั้งสิ้น:", df.format(total) + " บาท") +
                String.format("%-40s %10s\n", "รับเงินมา:", df.format(paid) + " บาท") +
                String.format("%-40s %10s\n", "เงินทอน:", df.format(change) + " บาท");
                
                // 1. บันทึกประวัติบิล
                BillHistorySaver.saveBill(tableModel, total, paid, paid - total);
                
                // 2. ล้างรายการสั่งซื้อและอัปเดต GUI
                orderManager.clearOrder(); 
                
                JTextArea finalReceiptArea = new JTextArea(receiptDetails);
                finalReceiptArea.setFont(new Font("Tahoma", Font.PLAIN, 14));
                finalReceiptArea.setEditable(false);
                JScrollPane scrollableReceipt = new JScrollPane(finalReceiptArea);
                scrollableReceipt.setPreferredSize(new Dimension(450, 450));
                
                JOptionPane.showMessageDialog(
                    paymentDialog, 
                    scrollableReceipt, 
                    "✅ การชำระเงินเสร็จสมบูรณ์ | ใบเสร็จ", 
                    JOptionPane.PLAIN_MESSAGE
                );
                // *** ลบ: tableModel.setRowCount(0); ออก เพราะ clearOrder() ทำแล้ว ***
                JOptionPane.showMessageDialog(paymentDialog, "การชำระเงินเสร็จสมบูรณ์ บันทึกรายการแล้ว", "สำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                paymentDialog.dispose(); 
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(paymentDialog, "กรุณากรอกจำนวนเงินที่รับมาให้ถูกต้อง", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(paymentDialog, "เกิดข้อผิดพลาดในการบันทึกประวัติบิล", "ข้อผิดพลาด I/O", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(finishBtn);


        // --- จัดองค์ประกอบของ Dialog ---
        paymentDialog.add(displayPanel, BorderLayout.NORTH);
        paymentDialog.add(calcPanel, BorderLayout.CENTER);
        paymentDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // กำหนดให้ paidField มี focus ทันทีที่เปิด
        paymentDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                paidField.requestFocusInWindow();
            }
        });

        paymentDialog.setVisible(true);
    }
    
    // เมธอดเสริมสำหรับสร้าง Label
    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }

    // เมธอดเสริมสำหรับสรุปรายการ
    private String getBillSummary(DefaultTableModel model) {
        StringBuilder sb = new StringBuilder();
        double total = 0;
        
        sb.append(String.format("%-10s %-20s %-10s %10s\n", "Category", "Menu", "Type", "Price"));
        sb.append("----------------------------------------------------------\n");
        
        for (int i = 0; i < model.getRowCount(); i++) {
            String category = model.getValueAt(i, 0).toString();
            String menu = model.getValueAt(i, 1).toString();
            String type = model.getValueAt(i, 2).toString();
            double price = Double.parseDouble(model.getValueAt(i, 3).toString());
            total += price;
            
            sb.append(String.format("%-10s %-20s %-10s %10.2f\n", category, menu, type, price));
        }
        sb.append("----------------------------------------------------------\n");
        sb.append(String.format("%40s %10.2f\n", "Total:", total));
        
        return sb.toString();
    }

    public void reloadMenuButtons() {
    // 1. ลบปุ่มเก่าทั้งหมด
    menuPanel.removeAll();
    // ต้องมี FoodMenuLoader.loadFoodFromCSV ใน Lib (สมมติว่ามีแล้ว)
    this.foodList = FoodMenuLoader.loadFoodFromCSV("data/menu.csv"); 
    
    Font thaiFont = new Font("Tahoma", Font.PLAIN, 16); 
    
    // 2. สร้างปุ่มใหม่จาก foodList ล่าสุด
    for (Food food : foodList) {
        ImageIcon icon = new ImageIcon(food.getImagePath());
        Image img = icon.getImage().getScaledInstance(120, 90, Image.SCALE_SMOOTH);

        JButton btn = new JButton("<html>" + food.getName() + "</html>", new ImageIcon(img));
        btn.setFont(thaiFont);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);

        // ต้องมี FoodOptionHandler ใน Lib (สมมติว่ามีแล้ว)
        btn.addActionListener(e -> FoodOptionHandler.showFoodOption(this, food, orderManager)); 
        menuPanel.add(btn);
    }

    // 3. อัปเดต UI
    menuPanel.revalidate();
    menuPanel.repaint();
}
}