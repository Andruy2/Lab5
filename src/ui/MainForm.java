package ui;

import models.*;
import database.ClientDAO;
import utils.FileUtils;
import database.DatabaseBackup;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class MainForm extends JFrame {
    private Bank bank = new Bank();
    private ClientDAO clientDAO = new ClientDAO();
    private JTable clientTable;
    private DefaultTableModel tableModel;

    public MainForm() {
        setTitle("Банковская система");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        initializeDatabase();
        loadClientsFromDatabase();
        initComponents();
        setIcon();
    }

    private void setIcon() {
        try {
            ImageIcon icon = new ImageIcon("icon.png");
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Иконка не обязательна
        }
    }

    private void initializeDatabase() {
        database.DatabaseConnection.initializeDatabase();
    }

    private void loadClientsFromDatabase() {
        try {
            List<Client> clients = clientDAO.getAllClients();
            bank = new Bank(); // ВАЖНО: создаем новый объект Bank
            for (Client client : clients) {
                bank.addClient(client);
            }
            System.out.println("Загружено клиентов из БД: " + clients.size());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки данных из БД: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        // Главная панель с BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель заголовка
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Банковская система управления клиентами", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 70, 140));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Панель кнопок действий
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        actionButtonPanel.setBorder(BorderFactory.createTitledBorder("Действия"));

        JButton addButton = createStyledButton("➕ Добавить", new Color(46, 125, 50));
        JButton editButton = createStyledButton("✏️ Редактировать", new Color(30, 136, 229));
        JButton deleteButton = createStyledButton("🗑️ Удалить", new Color(229, 57, 53));
        JButton refreshButton = createStyledButton("🔄 Обновить", new Color(121, 85, 72));

        actionButtonPanel.add(addButton);
        actionButtonPanel.add(editButton);
        actionButtonPanel.add(deleteButton);
        actionButtonPanel.add(refreshButton);

        // Панель кнопок файловых операций
        JPanel fileButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        fileButtonPanel.setBorder(BorderFactory.createTitledBorder("Файловые операции"));

        JButton exportTxtButton = createStyledButton("📤 Экспорт в TXT", new Color(0, 121, 107));
        JButton exportCsvButton = createStyledButton("📊 Экспорт в CSV", new Color(0, 121, 107));
        JButton importButton = createStyledButton("📥 Импорт из файла", new Color(194, 24, 91));

        fileButtonPanel.add(exportTxtButton);
        fileButtonPanel.add(exportCsvButton);
        fileButtonPanel.add(importButton);

        // Панель кнопок БД и статистики
        JPanel dbButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        dbButtonPanel.setBorder(BorderFactory.createTitledBorder("База данных"));

        JButton backupButton = createStyledButton("💾 Резервная копия", new Color(123, 31, 162));
        JButton restoreButton = createStyledButton("🔄 Восстановить", new Color(123, 31, 162));
        JButton statsButton = createStyledButton("📈 Статистика", new Color(255, 160, 0));

        dbButtonPanel.add(backupButton);
        dbButtonPanel.add(restoreButton);
        dbButtonPanel.add(statsButton);

        // Панель сортировки
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        sortPanel.setBorder(BorderFactory.createTitledBorder("Сортировка"));

        JButton sortByNameButton = createStyledButton("🔤 По имени", new Color(93, 64, 55));
        JButton sortByDepositButton = createStyledButton("💰 По вкладу", new Color(93, 64, 55));
        JButton sortByTypeButton = createStyledButton("🏷️ По типу", new Color(93, 64, 55));

        sortPanel.add(sortByNameButton);
        sortPanel.add(sortByDepositButton);
        sortPanel.add(sortByTypeButton);

        // Комбинируем все панели кнопок в одну панель
        JPanel topPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        topPanel.add(actionButtonPanel);
        topPanel.add(fileButtonPanel);
        topPanel.add(dbButtonPanel);
        topPanel.add(sortPanel);

        // Таблица клиентов
        String[] columns = {"Паспорт", "ФИО", "Тип клиента", "Вклад", "Бонус", "Описание бонуса"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Double.class;
                return String.class;
            }
        };

        clientTable = new JTable(tableModel);
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.setRowHeight(25);
        clientTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        clientTable.setFont(new Font("Arial", Font.PLAIN, 12));

        // Настраиваем ширину колонок
        clientTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        clientTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        clientTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        clientTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        clientTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        clientTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(clientTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Список клиентов"));

        // Панель информации
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel();
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(Color.GRAY);
        updateInfoLabel(infoLabel);
        infoPanel.add(infoLabel);

        // Обновляем таблицу
        refreshTable();

        // Добавляем компоненты на главную панель
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        // Обработчики событий для кнопок действий
        addButton.addActionListener(e -> openClientForm(null));
        editButton.addActionListener(e -> editSelectedClient());
        deleteButton.addActionListener(e -> deleteSelectedClient());
        refreshButton.addActionListener(e -> refreshAllData()); // ИЗМЕНИЛИ НА refreshAllData()

        // Обработчики для сортировки
        sortByNameButton.addActionListener(e -> {
            bank.sortByName();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Данные отсортированы по имени",
                    "Сортировка", JOptionPane.INFORMATION_MESSAGE);
        });

        sortByDepositButton.addActionListener(e -> {
            bank.sortByDeposit();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Данные отсортированы по вкладу",
                    "Сортировка", JOptionPane.INFORMATION_MESSAGE);
        });

        sortByTypeButton.addActionListener(e -> {
            bank.sortByType();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Данные отсортированы по типу клиента",
                    "Сортировка", JOptionPane.INFORMATION_MESSAGE);
        });

        // Обработчики для файловых операций
        exportTxtButton.addActionListener(e -> exportToTxt());
        exportCsvButton.addActionListener(e -> exportToCsv());
        importButton.addActionListener(e -> importFromFile());

        // Обработчики для БД операций
        backupButton.addActionListener(e -> DatabaseBackup.backupDatabase(this));
        restoreButton.addActionListener(e -> restoreDatabaseAndRefresh()); // ИЗМЕНИЛИ
        statsButton.addActionListener(e -> showStatistics());

        // Двойной клик по таблице для редактирования
        clientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedClient();
                }
            }
        });

        add(mainPanel);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(color.darker());
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    // НОВЫЙ МЕТОД: полное обновление данных
    private void refreshAllData() {
        System.out.println("Полное обновление данных...");
        loadClientsFromDatabase(); // Перезагружаем данные из БД
        refreshTable(); // Обновляем таблицу
        JOptionPane.showMessageDialog(this,
                "Данные успешно обновлены из базы данных",
                "Обновление", JOptionPane.INFORMATION_MESSAGE);
    }

    // НОВЫЙ МЕТОД: восстановление БД с обновлением данных
    private void restoreDatabaseAndRefresh() {
        // Восстанавливаем БД
        DatabaseBackup.restoreDatabase(this);

        // Ждем немного, чтобы файл БД точно скопировался
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Обновляем данные из восстановленной БД
                refreshAllData();
                ((Timer)e.getSource()).stop();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void openClientForm(Client client) {
        ClientForm clientForm;
        if (client == null) {
            clientForm = new ClientForm(this, "Добавление нового клиента", true);
        } else {
            clientForm = new ClientForm(this, "Редактирование клиента", false);
            clientForm.setClientData(client);
        }
        clientForm.setVisible(true);
    }

    private void editSelectedClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow >= 0) {
            String passport = (String) tableModel.getValueAt(selectedRow, 0);
            Client client = bank.getClientByPassport(passport);
            if (client != null) {
                openClientForm(client);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Выберите клиента для редактирования (кликните по строке в таблице)",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow >= 0) {
            String passport = (String) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Вы уверены, что хотите удалить клиента?\n" +
                            "Паспорт: " + passport + "\n" +
                            "ФИО: " + name,
                    "Подтверждение удаления", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    clientDAO.deleteClient(passport);
                    bank.removeClient(passport);
                    refreshTable();

                    JOptionPane.showMessageDialog(this,
                            "Клиент успешно удален:\n" + name + " (" + passport + ")",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);

                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка удаления из БД: " + e.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Выберите клиента для удаления (кликните по строке в таблице)",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);

        for (Client client : bank.getAllClients()) {
            Object[] row = {
                    client.getPassport(),
                    client.getName(),
                    client.getType(),
                    client.getDeposit(),
                    client.getBonusStrategy().getDescription(),
                    getBonusDescription(client)
            };
            tableModel.addRow(row);
        }

        // Обновляем информацию внизу
        updateInfoLabel();
    }

    private String getBonusDescription(Client client) {
        BonusStrategy bonus = client.getBonusStrategy();
        if (bonus instanceof models.PercentageBonus) {
            return "10% от суммы вклада";
        } else if (bonus instanceof models.FixedBonus) {
            return "Фиксированный бонус 3000 руб.";
        } else {
            return "Без бонуса";
        }
    }

    private void updateInfoLabel() {
        Component[] components = getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] subComps = ((JPanel) comp).getComponents();
                for (Component subComp : subComps) {
                    if (subComp instanceof JLabel && ((JLabel) subComp).getText().contains("клиентов")) {
                        int clientCount = bank.getAllClients().size();
                        double totalDeposits = bank.getTotalDeposits();
                        ((JLabel) subComp).setText(String.format(
                                "Всего клиентов: %d | Общая сумма вкладов: %.2f руб. | Средний вклад: %.2f руб.",
                                clientCount, totalDeposits,
                                clientCount > 0 ? totalDeposits / clientCount : 0
                        ));
                        return;
                    }
                }
            }
        }
    }

    private void updateInfoLabel(JLabel label) {
        int clientCount = bank.getAllClients().size();
        double totalDeposits = bank.getTotalDeposits();
        label.setText(String.format(
                "Всего клиентов: %d | Общая сумма вкладов: %.2f руб. | Средний вклад: %.2f руб.",
                clientCount, totalDeposits,
                clientCount > 0 ? totalDeposits / clientCount : 0
        ));
    }

    private void exportToTxt() {
        FileUtils.exportToFile(bank, this);
    }

    private void exportToCsv() {
        FileUtils.exportToCSV(bank, this);
    }

    private void importFromFile() {
        FileUtils.importFromFile(this, clientDAO, bank);
        refreshAllData(); // ИЗМЕНИЛИ: полное обновление после импорта
    }

    private void showStatistics() {
        int clientCount = bank.getAllClients().size();
        double totalDeposits = bank.getTotalDeposits();

        // Подсчет по типам клиентов
        int vipCount = 0, pensionerCount = 0, regularCount = 0;
        double vipTotal = 0, pensionerTotal = 0, regularTotal = 0;

        for (Client client : bank.getAllClients()) {
            String type = client.getType();
            double deposit = client.getDeposit();

            switch (type) {
                case "Вип":
                    vipCount++;
                    vipTotal += deposit;
                    break;
                case "Пенсионер":
                    pensionerCount++;
                    pensionerTotal += deposit;
                    break;
                default:
                    regularCount++;
                    regularTotal += deposit;
            }
        }

        String message = String.format("""
                📊 СТАТИСТИКА БАНКА
                ====================
                
                📈 ОБЩАЯ ИНФОРМАЦИЯ:
                • Всего клиентов: %d
                • Общая сумма вкладов: %.2f руб.
                • Средний вклад: %.2f руб.
                
                👥 РАСПРЕДЕЛЕНИЕ ПО ТИПАМ:
                • VIP клиентов: %d (%.1f%%) - %.2f руб.
                • Пенсионеров: %d (%.1f%%) - %.2f руб.
                • Обычных клиентов: %d (%.1f%%) - %.2f руб.
                
                💰 СРЕДНИЕ ВКЛАДЫ:
                • Средний VIP вклад: %.2f руб.
                • Средний пенсионерский вклад: %.2f руб.
                • Средний обычный вклад: %.2f руб.
                """,
                clientCount, totalDeposits,
                clientCount > 0 ? totalDeposits / clientCount : 0,

                vipCount, clientCount > 0 ? (vipCount * 100.0 / clientCount) : 0, vipTotal,
                pensionerCount, clientCount > 0 ? (pensionerCount * 100.0 / clientCount) : 0, pensionerTotal,
                regularCount, clientCount > 0 ? (regularCount * 100.0 / clientCount) : 0, regularTotal,

                vipCount > 0 ? vipTotal / vipCount : 0,
                pensionerCount > 0 ? pensionerTotal / pensionerCount : 0,
                regularCount > 0 ? regularTotal / regularCount : 0
        );

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Статистика банка", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addOrUpdateClient(Client client, boolean isNew) {
        try {
            if (isNew) {
                // Проверяем уникальность паспорта
                if (clientDAO.clientExists(client.getPassport())) {
                    JOptionPane.showMessageDialog(this,
                            "Клиент с паспортом " + client.getPassport() + " уже существует!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                clientDAO.saveClient(client);
                bank.addClient(client);

                JOptionPane.showMessageDialog(this,
                        "Новый клиент успешно добавлен:\n" +
                                client.getName() + " (" + client.getPassport() + ")",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);

            } else {
                clientDAO.updateClient(client);
                bank.updateClient(client.getPassport(), client);

                JOptionPane.showMessageDialog(this,
                        "Данные клиента обновлены:\n" +
                                client.getName() + " (" + client.getPassport() + ")",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            }

            refreshTable();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка сохранения в БД: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean clientExistsInDatabase(String passport) {
        try {
            return clientDAO.clientExists(passport);
        } catch (SQLException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            MainForm mainForm = new MainForm();
            mainForm.setVisible(true);
        });
    }
}