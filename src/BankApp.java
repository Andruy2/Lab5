import ui.MainForm;
import javax.swing.*;
import java.awt.*;

public class BankApp {
    public static void main(String[] args) {
        // Устанавливаем нативный Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Настройки шрифтов и цветов
        setupUIManager();

        // Запускаем приложение
        SwingUtilities.invokeLater(() -> {
            MainForm mainForm = new MainForm();
            mainForm.setVisible(true);

            // Показываем приветственное сообщение
            showWelcomeMessage(mainForm);
        });
    }

    private static void setupUIManager() {
        // Устанавливаем шрифты для всех компонентов
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
        Font boldFont = new Font("Segoe UI", Font.BOLD, 12);

        UIManager.put("Button.font", defaultFont);
        UIManager.put("ToggleButton.font", defaultFont);
        UIManager.put("RadioButton.font", defaultFont);
        UIManager.put("CheckBox.font", defaultFont);
        UIManager.put("ColorChooser.font", defaultFont);
        UIManager.put("ComboBox.font", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("List.font", defaultFont);
        UIManager.put("MenuBar.font", defaultFont);
        UIManager.put("MenuItem.font", defaultFont);
        UIManager.put("RadioButtonMenuItem.font", defaultFont);
        UIManager.put("CheckBoxMenuItem.font", defaultFont);
        UIManager.put("Menu.font", defaultFont);
        UIManager.put("PopupMenu.font", defaultFont);
        UIManager.put("OptionPane.font", defaultFont);
        UIManager.put("Panel.font", defaultFont);
        UIManager.put("ProgressBar.font", defaultFont);
        UIManager.put("ScrollPane.font", defaultFont);
        UIManager.put("Viewport.font", defaultFont);
        UIManager.put("TabbedPane.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TableHeader.font", boldFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("PasswordField.font", defaultFont);
        UIManager.put("TextArea.font", defaultFont);
        UIManager.put("TextPane.font", defaultFont);
        UIManager.put("EditorPane.font", defaultFont);
        UIManager.put("TitledBorder.font", boldFont);
        UIManager.put("ToolBar.font", defaultFont);
        UIManager.put("ToolTip.font", defaultFont);
        UIManager.put("Tree.font", defaultFont);
    }

    private static void showWelcomeMessage(JFrame parent) {
        String message = """
            🏦 ДОБРО ПОЖАЛОВАТЬ В БАНКОВСКУЮ СИСТЕМУ!
            
            📋 КРАТКАЯ ИНСТРУКЦИЯ:
            
            1. ДОБАВЛЕНИЕ КЛИЕНТОВ:
               • Нажмите "➕ Добавить"
               • Заполните все обязательные поля (*)
               • Выберите тип клиента
            
            2. РЕДАКТИРОВАНИЕ:
               • Выберите клиента в таблице
               • Нажмите "✏️ Редактировать"
               • Или дважды кликните по строке
            
            3. СОРТИРОВКА:
               • Используйте кнопки в разделе "Сортировка"
               • По имени, вкладу или типу
            
            4. ЭКСПОРТ/ИМПОРТ:
               • Экспорт в TXT или CSV
               • Импорт из текстовых файлов
            
            5. БАЗА ДАННЫХ:
               • Создание резервных копий
               • Восстановление из backup
            
            💡 СОВЕТ: Все изменения автоматически сохраняются в БД.
            
            Файл базы данных: bank_database.db
            Резервные копии: bank_database_backup.db
            """;

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(parent, scrollPane,
                "Добро пожаловать в банковскую систему!",
                JOptionPane.INFORMATION_MESSAGE);
    }
}