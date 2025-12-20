package ui;

import models.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ClientForm extends JDialog {
    private MainForm mainForm;
    private Client editingClient;
    private boolean isNewClient;

    private JTextField passportField;
    private JTextField nameField;
    private JTextField depositField;
    private JComboBox<String> typeComboBox;
    private JLabel bonusLabel;
    private JLabel bonusValueLabel;
    private JLabel totalLabel;
    private JButton saveButton;
    private JButton cancelButton;

    private DecimalFormat decimalFormat;

    public ClientForm(MainForm parent, String title, boolean isNew) {
        super(parent, title, true);
        this.mainForm = parent;
        this.isNewClient = isNew;

        initNumberFormat();
        setupDialog();
        initComponents();
        setupListeners();
    }

    private void initNumberFormat() {
        decimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        decimalFormat.setGroupingUsed(false);
    }

    private void setupDialog() {
        setSize(500, 450);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Заголовок
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        String titleText = isNewClient ? "ДОБАВЛЕНИЕ НОВОГО КЛИЕНТА" : "РЕДАКТИРОВАНИЕ КЛИЕНТА";
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(0, 70, 140));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // Паспорт
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passportLabel = new JLabel("Номер паспорта (10 цифр): *");
        passportLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(passportLabel, gbc);

        gbc.gridx = 1;
        passportField = new JTextField(20);
        passportField.setEnabled(isNewClient);
        if (!isNewClient) {
            passportField.setBackground(new Color(230, 230, 230));
        }
        mainPanel.add(passportField, gbc);

        // ФИО
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel nameLabel = new JLabel("ФИО клиента: *");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        mainPanel.add(nameField, gbc);

        // Вклад - ЖЕСТКАЯ ВАЛИДАЦИЯ ПРИ ВВОДЕ
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel depositLabel = new JLabel("Сумма вклада (руб.): *");
        depositLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(depositLabel, gbc);

        gbc.gridx = 1;
        depositField = new JTextField(20);

        // ЖЕСТКАЯ ВАЛИДАЦИЯ ПРИ КАЖДОМ ВВОДЕ
        depositField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String currentText = depositField.getText();
                int caretPos = depositField.getCaretPosition();

                // Управляющие клавиши всегда разрешены
                if (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
                    return;
                }

                // 1. Разрешаем ТОЛЬКО цифры, точку и запятую
                if (!Character.isDigit(c) && c != '.' && c != ',') {
                    e.consume();
                    showTempError("Только цифры, точка или запятая!");
                    return;
                }

                // 2. Запрещаем точку/запятую в начале
                if ((c == '.' || c == ',') && currentText.isEmpty()) {
                    e.consume();
                    showTempError("Не начинайте с точки или запятой!");
                    return;
                }

                // 3. Запрещаем вторую точку/запятую
                if ((c == '.' && currentText.contains(".")) ||
                        (c == ',' && currentText.contains(","))) {
                    e.consume();
                    showTempError("Можно только одну точку ИЛИ запятую!");
                    return;
                }

                // 4. Запрещаем и точку, и запятую
                if ((c == '.' && currentText.contains(",")) ||
                        (c == ',' && currentText.contains("."))) {
                    e.consume();
                    showTempError("Используйте или точку, или запятую!");
                    return;
                }

                // 5. Проверяем, что после точки/запятой будут цифры
                if ((c == '.' || c == ',') && caretPos == currentText.length()) {
                    // Точка/запятая в конце - разрешаем, но проверяем позже
                }

                // 6. Максимальная длина
                if (currentText.length() >= 15) {
                    e.consume();
                    showTempError("Слишком длинное число!");
                    return;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                validateDepositInputRealTime();
                calculateBonus();
            }
        });

        // При потере фокуса - строгая проверка
        depositField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                strictValidateAndFormat();
            }
        });

        mainPanel.add(depositField, gbc);

        // Тип клиента
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel typeLabel = new JLabel("Тип клиента: *");
        typeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(typeLabel, gbc);

        gbc.gridx = 1;
        String[] types = {"Обычный клиент", "Пенсионер", "VIP клиент"};
        typeComboBox = new JComboBox<>(types);
        typeComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(typeComboBox, gbc);

        // Информация о бонусе
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel bonusTitleLabel = new JLabel("Тип бонуса:");
        bonusTitleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(bonusTitleLabel, gbc);

        gbc.gridx = 1;
        bonusLabel = new JLabel("Без бонуса");
        bonusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bonusLabel.setForeground(new Color(0, 100, 0));
        mainPanel.add(bonusLabel, gbc);

        // Значение бонуса
        gbc.gridx = 0; gbc.gridy = 6;
        JLabel bonusValueTitleLabel = new JLabel("Размер бонуса:");
        bonusValueTitleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(bonusValueTitleLabel, gbc);

        gbc.gridx = 1;
        bonusValueLabel = new JLabel("0.00 руб.");
        bonusValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bonusValueLabel.setForeground(new Color(0, 100, 0));
        mainPanel.add(bonusValueLabel, gbc);

        // Итоговая сумма
        gbc.gridx = 0; gbc.gridy = 7;
        JLabel totalTitleLabel = new JLabel("Итоговая сумма:");
        totalTitleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(totalTitleLabel, gbc);

        gbc.gridx = 1;
        totalLabel = new JLabel("0.00 руб.");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setForeground(new Color(198, 40, 40));
        mainPanel.add(totalLabel, gbc);

        // Панель кнопок
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 0, 0);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

        saveButton = new JButton(isNewClient ? "💾 Сохранить" : "💾 Обновить");
        saveButton.setBackground(new Color(46, 125, 50));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));

        cancelButton = new JButton("❌ Отмена");
        cancelButton.setBackground(new Color(229, 57, 53));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        updateBonusInfo();
    }

    private void setupListeners() {
        typeComboBox.addActionListener(e -> updateBonusInfo());
        saveButton.addActionListener(e -> saveClient());
        cancelButton.addActionListener(e -> dispose());
    }

    private void showTempError(String message) {
        depositField.setToolTipText(message);
        Timer timer = new Timer(1500, e -> depositField.setToolTipText(null));
        timer.setRepeats(false);
        timer.start();
    }

    // ЖЕСТКАЯ ПРОВЕРКА В РЕАЛЬНОМ ВРЕМЕНИ
    private void validateDepositInputRealTime() {
        String text = depositField.getText();

        if (text.isEmpty()) {
            depositField.setBackground(Color.WHITE);
            return;
        }

        // 1. Проверка на точку/запятую в конце
        if (text.endsWith(".") || text.endsWith(",")) {
            depositField.setBackground(new Color(255, 200, 200)); // Красный
            depositField.setToolTipText("ОШИБКА: Уберите точку/запятую в конце!");
            return;
        }

        // 2. Проверка на точку/запятую без десятичной части
        if (text.contains(".")) {
            String[] parts = text.split("\\.");
            if (parts.length > 1 && parts[1].isEmpty()) {
                depositField.setBackground(new Color(255, 200, 200));
                depositField.setToolTipText("ОШИБКА: После точки должна быть десятичная часть!");
                return;
            }
        }

        if (text.contains(",")) {
            String[] parts = text.split(",");
            if (parts.length > 1 && parts[1].isEmpty()) {
                depositField.setBackground(new Color(255, 200, 200));
                depositField.setToolTipText("ОШИБКА: После запятой должна быть десятичная часть!");
                return;
            }
        }

        // Если все ок
        depositField.setBackground(Color.WHITE);
        depositField.setToolTipText(null);
    }

    // СТРОГАЯ ПРОВЕРКА И ФОРМАТИРОВАНИЕ ПРИ ПОТЕРЕ ФОКУСА
    private void strictValidateAndFormat() {
        String text = depositField.getText().trim();

        if (text.isEmpty()) {
            depositField.setText("");
            return;
        }

        // 1. Если точка/запятая в конце - УДАЛЯЕМ ЕЁ
        if (text.endsWith(".") || text.endsWith(",")) {
            text = text.substring(0, text.length() - 1);
            depositField.setText(text);
            showTempError("Точка/запятая в конце удалена автоматически");
        }

        // 2. Если точка/запятая без десятичной части - УДАЛЯЕМ ЕЁ
        if (text.contains(".")) {
            String[] parts = text.split("\\.");
            if (parts.length > 1 && parts[1].isEmpty()) {
                text = parts[0];
                depositField.setText(text);
                showTempError("Точка без десятичной части удалена");
            }
        }

        if (text.contains(",")) {
            String[] parts = text.split(",");
            if (parts.length > 1 && parts[1].isEmpty()) {
                text = parts[0];
                depositField.setText(text);
                showTempError("Запятая без десятичной части удалена");
            }
        }

        // 3. Парсим и форматируем
        String parseText = text.replace(',', '.');

        try {
            double value = Double.parseDouble(parseText);
            depositField.setText(decimalFormat.format(value));
        } catch (NumberFormatException e) {
            // Если не число - очищаем поле
            depositField.setText("");
            showTempError("Введите корректное число!");
        }
    }

    public void setClientData(Client client) {
        this.editingClient = client;

        if (client != null) {
            passportField.setText(client.getPassport());
            nameField.setText(client.getName());
            depositField.setText(decimalFormat.format(client.getDeposit()));

            String type = client.getType();
            switch (type) {
                case "Вип":
                    typeComboBox.setSelectedIndex(2);
                    break;
                case "Пенсионер":
                    typeComboBox.setSelectedIndex(1);
                    break;
                default:
                    typeComboBox.setSelectedIndex(0);
            }

            updateBonusInfo();
            calculateBonus();
        }
    }

    private void updateBonusInfo() {
        String selectedType = (String) typeComboBox.getSelectedItem();
        switch (selectedType) {
            case "VIP клиент":
                bonusLabel.setText("Процентный бонус (10%)");
                break;
            case "Пенсионер":
                bonusLabel.setText("Фиксированный бонус");
                break;
            default:
                bonusLabel.setText("Без бонуса");
        }

        calculateBonus();
    }

    private void calculateBonus() {
        try {
            String depositText = depositField.getText();
            if (depositText.isEmpty()) {
                bonusValueLabel.setText("0.00 руб.");
                totalLabel.setText("0.00 руб.");
                return;
            }

            // Убираем точку/запятую в конце перед расчетом
            if (depositText.endsWith(".") || depositText.endsWith(",")) {
                depositText = depositText.substring(0, depositText.length() - 1);
            }

            // Заменяем запятую на точку
            depositText = depositText.replace(',', '.');

            double deposit = Double.parseDouble(depositText);

            String selectedType = (String) typeComboBox.getSelectedItem();
            double bonus = 0;

            switch (selectedType) {
                case "VIP клиент":
                    bonus = deposit * 0.1;
                    bonusValueLabel.setText(decimalFormat.format(bonus) + " руб.");
                    break;
                case "Пенсионер":
                    bonus = 3000;
                    bonusValueLabel.setText(decimalFormat.format(bonus) + " руб.");
                    break;
                default:
                    bonusValueLabel.setText("0.00 руб.");
            }

            double total = deposit + bonus;
            totalLabel.setText(decimalFormat.format(total) + " руб.");

        } catch (NumberFormatException e) {
            bonusValueLabel.setText("0.00 руб.");
            totalLabel.setText("0.00 руб.");
        }
    }

    // ЖЕСТКАЯ ВАЛИДАЦИЯ ПРИ СОХРАНЕНИИ
    private boolean validateForm() {
        // 1. Проверка паспорта
        String passport = passportField.getText().trim();
        if (!isValidPassport(passport)) {
            showError("Паспорт должен содержать ровно 10 цифр!");
            passportField.requestFocus();
            return false;
        }

        if (isNewClient && mainForm.clientExistsInDatabase(passport)) {
            showError("Клиент с таким паспортом уже существует!");
            passportField.requestFocus();
            return false;
        }

        // 2. Проверка ФИО
        String name = nameField.getText().trim();
        if (name.isEmpty() || name.length() < 2) {
            showError("Введите ФИО (минимум 2 символа)!");
            nameField.requestFocus();
            return false;
        }

        // 3. СТРОГАЯ ПРОВЕРКА СУММЫ
        String depositText = depositField.getText().trim();

        if (depositText.isEmpty()) {
            showError("Введите сумму вклада!");
            depositField.requestFocus();
            return false;
        }

        // Убираем возможные пробелы
        depositText = depositText.replaceAll("\\s", "");

        // Проверка на валидные символы
        if (!depositText.matches("[\\d.,]+")) {
            showError("Разрешены только цифры, точка или запятая!");
            depositField.requestFocus();
            return false;
        }

        // Проверка на точку/запятую в конце - СТРОГО ЗАПРЕЩЕНО
        if (depositText.endsWith(".") || depositText.endsWith(",")) {
            showError("НЕЛЬЗЯ: Точка или запятая в конце!\n\n" +
                    "Правильные примеры:\n" +
                    "• 1000 (целое число)\n" +
                    "• 1500.50 (с десятичной частью)\n" +
                    "• 2000,75 (с десятичной частью)");
            depositField.requestFocus();
            depositField.selectAll();
            return false;
        }

        // Проверка на точку/запятую без десятичной части
        if (depositText.contains(".")) {
            String[] parts = depositText.split("\\.");
            if (parts.length > 1 && parts[1].isEmpty()) {
                showError("НЕЛЬЗЯ: Точка без десятичной части!\n\n" +
                        "Используйте:\n" +
                        "• 1000 (без точки)\n" +
                        "• 1000.50 (с десятичной частью)");
                depositField.requestFocus();
                depositField.selectAll();
                return false;
            }
        }

        if (depositText.contains(",")) {
            String[] parts = depositText.split(",");
            if (parts.length > 1 && parts[1].isEmpty()) {
                showError("НЕЛЬЗЯ: Запятая без десятичной части!\n\n" +
                        "Используйте:\n" +
                        "• 1000 (без запятой)\n" +
                        "• 1000,50 (с десятичной частью)");
                depositField.requestFocus();
                depositField.selectAll();
                return false;
            }
        }

        // Проверка на несколько точек/запятых
        int dotCount = countChar(depositText, '.');
        int commaCount = countChar(depositText, ',');
        if (dotCount > 1 || commaCount > 1 || (dotCount > 0 && commaCount > 0)) {
            showError("Можно использовать только одну точку ИЛИ одну запятую!");
            depositField.requestFocus();
            depositField.selectAll();
            return false;
        }

        // Заменяем запятую на точку
        String parseText = depositText.replace(',', '.');

        try {
            double deposit = Double.parseDouble(parseText);

            if (deposit <= 0) {
                showError("Сумма должна быть больше 0!");
                depositField.requestFocus();
                depositField.selectAll();
                return false;
            }

            if (deposit > 1000000000) {
                showError("Сумма слишком велика! Максимум 1 000 000 000 руб.");
                depositField.requestFocus();
                depositField.selectAll();
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            showError("Введите корректное число!\n\nПримеры:\n" +
                    "✓ 1000\n" +
                    "✓ 1500.50\n" +
                    "✓ 2000,75\n" +
                    "✓ 50000\n\n" +
                    "✗ 1000. (нельзя)\n" +
                    "✗ 1000, (нельзя)");
            depositField.requestFocus();
            depositField.selectAll();
            return false;
        }
    }

    private boolean isValidPassport(String passport) {
        return passport != null && passport.matches("\\d{10}");
    }

    private int countChar(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }

    private void saveClient() {
        // Сначала проверяем сумму
        if (!validateForm()) {
            return;
        }

        // Если дошли сюда - все проверки пройдены
        String passport = passportField.getText().trim();
        String name = nameField.getText().trim();

        // Получаем сумму (уже проверена)
        String depositText = depositField.getText().replace(',', '.');
        double deposit = Double.parseDouble(depositText);

        String type = (String) typeComboBox.getSelectedItem();

        Client client;
        switch (type) {
            case "VIP клиент":
                client = new VIPClient(name, passport, deposit);
                break;
            case "Пенсионер":
                client = new PensionerClient(name, passport, deposit);
                break;
            default:
                client = new SimpleClient(name, passport, deposit);
        }

        mainForm.addOrUpdateClient(client, isNewClient);
        dispose();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "ОШИБКА", JOptionPane.ERROR_MESSAGE);
    }
}
