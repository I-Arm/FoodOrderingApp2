package lib;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AdminPanelManager {
    private Component parent;
    private List<Food> foodList;
    private FoodOrderingGUI guiFrame;

    public AdminPanelManager(Component parent, List<Food> foodList) {
        this.parent = parent;
        this.foodList = foodList;
        // ตรวจสอบและเก็บ reference ของ FoodOrderingGUI
        if (parent instanceof FoodOrderingGUI) {
            this.guiFrame = (FoodOrderingGUI) parent;
        }
    }

    public void openAdminPanel() {
        JFrame adminFrame = new JFrame("จัดการเมนู (Admin)");
        adminFrame.setSize(700, 400); // เพิ่มขนาดให้ดูดีขึ้น
        adminFrame.setLocationRelativeTo(parent);
        Font thaiFont = new Font("Tahoma", Font.PLAIN, 16);
        
        String[] columns = {"ชื่อเมนู", "ราคา ปกติ", "ราคา พิเศษ", "รูปภาพ", "ประเภท"};
        // ไม่อนุญาตให้แก้ไขในตาราง Admin โดยตรง (ป้องกันความผิดพลาด)
        DefaultTableModel adminModel = new DefaultTableModel(columns, 0) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false;
             }
        };
        
        // โหลดข้อมูลจาก foodList 
        for (Food f : foodList) {
            adminModel.addRow(new Object[]{
                f.getName(), f.getNormalPrice(), f.getSpecialPrice(),
                f.getImagePath(), f.getCategory()
            });
        }

        JTable adminTable = new JTable(adminModel);
        adminTable.setFont(thaiFont);
        adminTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(adminTable);

        JButton addBtn = new JButton("เพิ่มเมนู");
        JButton editBtn = new JButton("แก้ไขเมนู");
        JButton deleteBtn = new JButton("ลบเมนู");
        
        addBtn.addActionListener(e -> 
            addOrEditMenu(adminModel, null, adminFrame) 
        );

        editBtn.addActionListener(e -> {
            int row = adminTable.getSelectedRow();
            if (row != -1) {
                addOrEditMenu(adminModel, row, adminFrame); 
            } else {
                JOptionPane.showMessageDialog(adminFrame, "กรุณาเลือกเมนูที่จะแก้ไข");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = adminTable.getSelectedRow();
            if (row != -1) {
                // Pop-up ยืนยันการลบ
                int confirm = JOptionPane.showConfirmDialog(adminFrame, 
                    "คุณแน่ใจหรือไม่ว่าต้องการลบเมนู '" + adminModel.getValueAt(row, 0) + "'?", 
                    "ยืนยันการลบ", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    adminModel.removeRow(row);
                    
                    // *** 1. บันทึกข้อมูลหลังการลบ ***
                    try {
                        FoodMenuSaver.saveMenuToCSV(adminModel);
                        if (guiFrame != null) {
                            guiFrame.reloadMenuButtons();
                        }
                        JOptionPane.showMessageDialog(adminFrame, "ลบเมนูเรียบร้อย");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(adminFrame, "เกิดข้อผิดพลาดในการบันทึกไฟล์หลังการลบ", "ข้อผิดพลาด I/O", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(adminFrame, "กรุณาเลือกเมนูที่จะลบ");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // จัดปุ่มให้อยู่ตรงกลาง
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        adminFrame.add(scrollPane, BorderLayout.CENTER);
        adminFrame.add(btnPanel, BorderLayout.SOUTH);
        adminFrame.setVisible(true);
    }
    

    private void addOrEditMenu(DefaultTableModel model, Integer rowIndex, JFrame parentFrame) {
        
        // รายการประเภทสำหรับ JComboBox
        String[] categories = {"อาหาร", "เครื่องดื่ม", "ของหวาน", "อื่นๆ"}; 
        
        // 1. ประกาศและสร้าง Fields
        JTextField nameField = new JTextField(20);
        JTextField normalField = new JTextField(10);
        JTextField specialField = new JTextField(10);
        
        // 2. การจัดการรูปภาพ (Composite Component)
        JTextField imgPathField = new JTextField(20); 
        imgPathField.setEditable(false);
        JButton browseButton = new JButton("เลือกรูป..."); 
        JPanel imgPanel = new JPanel(new BorderLayout(5, 0));
        imgPanel.add(imgPathField, BorderLayout.CENTER);
        imgPanel.add(browseButton, BorderLayout.EAST);
        
        // 3. การจัดการประเภท: ใช้ JComboBox 
        JComboBox<String> categoryComboBox = new JComboBox<>(categories); 
        
        Font thaiFont = new Font("Tahoma", Font.PLAIN, 16); 
        
        // ตั้งค่า Font
        nameField.setFont(thaiFont);
        normalField.setFont(thaiFont);
        specialField.setFont(thaiFont);
        imgPathField.setFont(thaiFont);
        categoryComboBox.setFont(thaiFont);
        browseButton.setFont(thaiFont);

        // *** กำหนดค่าเริ่มต้นสำหรับแก้ไข (Edit) ***
        if (rowIndex != null) {
            nameField.setText(model.getValueAt(rowIndex, 0).toString());
            normalField.setText(model.getValueAt(rowIndex, 1).toString());
            specialField.setText(model.getValueAt(rowIndex, 2).toString());
            // สำหรับรูปภาพ ให้ตั้งค่า Path เดิม
            imgPathField.setText(model.getValueAt(rowIndex, 3).toString()); 
            // ตั้งค่า JComboBox ให้แสดงค่าปัจจุบัน
            categoryComboBox.setSelectedItem(model.getValueAt(rowIndex, 4).toString());
        }

        // 4. Action Listener สำหรับปุ่มเลือกรูป
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Images (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));

            int result = fileChooser.showOpenDialog(parentFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                
                File selectedFile = fileChooser.getSelectedFile();
                
                // *** 1. กำหนดโฟลเดอร์ปลายทาง ***
                File destinationDir = new File("images/");
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs(); // สร้างโฟลเดอร์ถ้ายังไม่มี
                }
                
                // 2. สร้างไฟล์ปลายทาง (ใช้ชื่อไฟล์เดิม)
                File destinationFile = new File(destinationDir, selectedFile.getName());
                
                try {
                    // *** 3. คัดลอกไฟล์จริงไปยังโฟลเดอร์ปลายทาง ***
                    Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    // 4. ตั้งค่า Path สัมพัทธ์ใน Field
                    String suggestedPath = "images/" + destinationFile.getName();
                    imgPathField.setText(suggestedPath);
                    
                    JOptionPane.showMessageDialog(parentFrame, 
                        "คัดลอกรูปภาพ '" + selectedFile.getName() + "' ไปยัง data/images/ เรียบร้อยแล้ว", 
                        "เสร็จสิ้น", JOptionPane.INFORMATION_MESSAGE);

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(parentFrame, 
                        "เกิดข้อผิดพลาดในการคัดลอกไฟล์: " + ex.getMessage(), 
                        "ข้อผิดพลาด I/O", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        // 5. สร้าง inputPanel ด้วย GridBagLayout
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // ขอบด้านนอก

        // Array ที่เก็บ Label และ Component ที่ถูกต้อง
        Object[][] components = {
            {"ชื่อเมนู:", nameField}, 
            {"ราคา ปกติ:", normalField}, 
            {"ราคา พิเศษ:", specialField}, 
            {"รูปภาพ:", imgPanel},         // <--- Component ที่ถูกต้อง
            {"ประเภท:", categoryComboBox}  // <--- Component ที่ถูกต้อง
        };

        for (int i = 0; i < components.length; i++) {
            // คอลัมน์ที่ 0: Label
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0; 
            JLabel label = new JLabel(components[i][0].toString());
            label.setFont(thaiFont);
            inputPanel.add(label, gbc);

            // คอลัมน์ที่ 1: Component (ต้อง Cast)
            gbc.gridx = 1;
            gbc.gridy = i;
            gbc.weightx = 1; 
            inputPanel.add((Component) components[i][1], gbc); // ใช้ Cast (Component)
        }

        // 6. เรียก JOptionPane
        int option = JOptionPane.showOptionDialog(
            parentFrame, 
            inputPanel, 
            (rowIndex == null ? "เพิ่มเมนูใหม่" : "แก้ไขเมนู"), 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, null, null
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                // ดึงค่าที่ถูกต้อง
                String name = nameField.getText().trim();
                double normal = Double.parseDouble(normalField.getText().trim());
                double special = Double.parseDouble(specialField.getText().trim());
                String img = imgPathField.getText().trim(); // ดึงจาก imgPathField
                String category = categoryComboBox.getSelectedItem().toString(); 
                
                // ตรวจสอบข้อมูลไม่ว่างเปล่า
                if (name.isEmpty() || img.isEmpty() || category.isEmpty() || normalField.getText().trim().isEmpty() || specialField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(parentFrame, "กรุณากรอกข้อมูลให้ครบทุกช่อง", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                    return; 
                }

                // 7. การเพิ่ม/แก้ไขแถวใน TableModel
                if (rowIndex == null) {
                    model.addRow(new Object[]{name, normal, special, img, category});
                } else {
                    model.setValueAt(name, rowIndex, 0);
                    model.setValueAt(normal, rowIndex, 1);
                    model.setValueAt(special, rowIndex, 2);
                    model.setValueAt(img, rowIndex, 3);
                    model.setValueAt(category, rowIndex, 4);
                }

                // 8. บันทึกและอัปเดต GUI
                FoodMenuSaver.saveMenuToCSV(model);
                if (guiFrame != null) {
                    guiFrame.reloadMenuButtons();
                }

                JOptionPane.showMessageDialog(parentFrame, (rowIndex == null ? "เพิ่มเมนู" : "แก้ไขเมนู") + "เรียบร้อย");

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parentFrame, "กรุณากรอกราคาให้เป็นตัวเลขที่ถูกต้อง", "ข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parentFrame, "เกิดข้อผิดพลาดในการบันทึกไฟล์: " + e.getMessage(), "ข้อผิดพลาด I/O", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}