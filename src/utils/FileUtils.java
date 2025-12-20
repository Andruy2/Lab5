package utils;

import models.*;
import database.ClientDAO;
import javax.swing.*;
import java.io.*;
import java.util.regex.*;

public class FileUtils {

    public static void exportToFile(Bank bank, JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Экспорт данных в файл");

        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("=== ЭКСПОРТ ДАННЫХ ИЗ БАНКОВСКОЙ СИСТЕМЫ ===");
                writer.println("Дата экспорта: " + new java.util.Date());
                writer.println("Всего клиентов: " + bank.getAllClients().size());
                writer.println("Общая сумма вкладов: " + bank.getTotalDeposits() + " руб.");
                writer.println("=============================================\n");

                for (Client client : bank.getAllClients()) {
                    writer.println(client.getClientInfo());
                    writer.println("---");
                }

                JOptionPane.showMessageDialog(parent,
                        "Данные успешно экспортированы в файл:\n" + file.getAbsolutePath(),
                        "Успех", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent,
                        "Ошибка при экспорте в файл: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void importFromFile(JFrame parent, ClientDAO clientDAO, Bank bank) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Импорт данных из файла");

        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            int importedCount = 0;
            int errorCount = 0;
            StringBuilder errors = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                StringBuilder clientBlock = new StringBuilder();
                int lineNumber = 0;

                System.out.println("\n=== НАЧАЛО ИМПОРТА ФАЙЛА: " + file.getName() + " ===");

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    System.out.println("Строка " + lineNumber + ": '" + line + "'");

                    // Если находим разделитель или конец файла - обрабатываем блок
                    if (line.equals("---") || line.equals("==========") || line.equals("===") || line.isEmpty()) {
                        if (clientBlock.length() > 0) {
                            System.out.println("\nОбрабатываем блок клиента:");
                            System.out.println(clientBlock.toString());

                            Client client = parseClientData(clientBlock.toString());
                            if (client != null) {
                                try {
                                    // Проверяем уникальность паспорта
                                    if (!clientDAO.clientExists(client.getPassport())) {
                                        clientDAO.saveClient(client);
                                        bank.addClient(client);
                                        importedCount++;
                                        System.out.println("✅ Импортирован: " + client.getName() +
                                                " (паспорт: " + client.getPassport() + ")");
                                    } else {
                                        errorCount++;
                                        errors.append("• Паспорт уже существует: ").append(client.getPassport()).append("\n");
                                        System.out.println("❌ Паспорт уже существует: " + client.getPassport());
                                    }
                                } catch (Exception e) {
                                    errorCount++;
                                    errors.append("• Ошибка БД: ").append(e.getMessage()).append("\n");
                                    System.out.println("❌ Ошибка БД: " + e.getMessage());
                                }
                            } else {
                                errorCount++;
                                errors.append("• Не удалось распознать данные клиента\n");
                                System.out.println("❌ Не удалось распознать данные клиента");
                            }
                            clientBlock.setLength(0); // Очищаем блок
                        }
                    }
                    // Игнорируем служебные строки
                    else if (!line.startsWith("===") &&
                            !line.startsWith("Дата экспорта:") &&
                            !line.startsWith("Всего клиентов:") &&
                            !line.startsWith("Общая сумма вкладов:") &&
                            !line.startsWith("=================================")) {
                        clientBlock.append(line).append("\n");
                    }
                }

                // Обработка последнего блока
                if (clientBlock.length() > 0) {
                    System.out.println("\nОбрабатываем последний блок:");
                    System.out.println(clientBlock.toString());

                    Client client = parseClientData(clientBlock.toString());
                    if (client != null) {
                        try {
                            if (!clientDAO.clientExists(client.getPassport())) {
                                clientDAO.saveClient(client);
                                bank.addClient(client);
                                importedCount++;
                                System.out.println("✅ Импортирован (последний): " + client.getName());
                            } else {
                                errorCount++;
                                System.out.println("❌ Паспорт уже существует (последний): " + client.getPassport());
                            }
                        } catch (Exception e) {
                            errorCount++;
                            System.out.println("❌ Ошибка БД (последний): " + e.getMessage());
                        }
                    } else {
                        errorCount++;
                        System.out.println("❌ Не удалось распознать данные (последний)");
                    }
                }

                System.out.println("=== КОНЕЦ ИМПОРТА ===");
                System.out.println("Импортировано: " + importedCount + ", Ошибок: " + errorCount);

                // Показываем результат импорта
                showImportResult(parent, importedCount, errorCount, errors.toString());

            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent,
                        "Ошибка при чтении файла: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // УЛУЧШЕННЫЙ ПАРСЕР - теперь более гибкий
    private static Client parseClientData(String data) {
        System.out.println("\n=== ПАРСИНГ ДАННЫХ КЛИЕНТА ===");
        System.out.println("Исходные данные:\n" + data);

        try {
            String[] lines = data.split("\n");
            String name = "";
            String passport = "";
            double deposit = 0;
            String type = "Обычный клиент";

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                System.out.println("Анализ строки: '" + line + "'");

                // Поиск имени
                if (line.startsWith("Клиент:")) {
                    name = line.substring("Клиент:".length()).trim();
                    System.out.println("Найдено имя: " + name);
                }
                else if (line.startsWith("ФИО:")) {
                    name = line.substring("ФИО:".length()).trim();
                    System.out.println("Найдено ФИО: " + name);
                }
                else if (name.isEmpty() && !line.contains(":") && !line.matches(".*\\d.*")) {
                    // Если строка не содержит двоеточий и цифр, возможно это имя
                    name = line;
                    System.out.println("Предполагаем что это имя: " + name);
                }

                // Поиск паспорта
                if (line.startsWith("Паспорт:")) {
                    passport = line.substring("Паспорт:".length()).trim();
                    passport = passport.replaceAll("[^\\d]", "");
                    System.out.println("Найден паспорт: " + passport);
                }
                else if (line.matches(".*\\d{10}.*")) {
                    // Ищем 10 цифр подряд в любой строке
                    java.util.regex.Matcher matcher = Pattern.compile("\\d{10}").matcher(line);
                    if (matcher.find()) {
                        passport = matcher.group();
                        System.out.println("Найден паспорт в строке: " + passport);
                    }
                }

                // Поиск типа клиента
                if (line.startsWith("Категория:")) {
                    type = line.substring("Категория:".length()).trim();
                    System.out.println("Найдена категория: " + type);
                }
                else if (line.startsWith("Тип клиента:")) {
                    type = line.substring("Тип клиента:".length()).trim();
                    System.out.println("Найден тип: " + type);
                }
                else if (line.startsWith("Тип:")) {
                    type = line.substring("Тип:".length()).trim();
                    System.out.println("Найден тип (короткий): " + type);
                }
                else if (line.toLowerCase().contains("вип")) {
                    type = "Вип";
                    System.out.println("Определен как Вип");
                }
                else if (line.toLowerCase().contains("пенсион")) {
                    type = "Пенсионер";
                    System.out.println("Определен как Пенсионер");
                }
                else if (line.toLowerCase().contains("обычн")) {
                    type = "Обычный клиент";
                    System.out.println("Определен как Обычный");
                }

                // Поиск суммы вклада
                if (line.contains("Вклад") || line.contains("Сумма") || line.contains("руб") || line.matches(".*\\d+.*")) {
                    // Извлекаем все числа из строки
                    String[] words = line.split(" ");
                    for (String word : words) {
                        // Пробуем найти число
                        String numStr = word.replaceAll("[^\\d.,]", "").replace(',', '.');
                        if (!numStr.isEmpty() && numStr.matches("[\\d.]+")) {
                            try {
                                double candidate = Double.parseDouble(numStr);
                                if (candidate > 0 && candidate < 1000000000) {
                                    deposit = candidate;
                                    System.out.println("Найдена сумма: " + deposit + " в строке: '" + line + "'");
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                // Игнорируем
                            }
                        }
                    }
                }
            }

            // Проверка обязательных полей
            System.out.println("\n=== РЕЗУЛЬТАТ ПАРСИНГА ===");
            System.out.println("Имя: " + name + " (валидно: " + (!name.isEmpty()) + ")");
            System.out.println("Паспорт: " + passport + " (валидно: " + (passport.length() == 10) + ")");
            System.out.println("Сумма: " + deposit + " (валидно: " + (deposit > 0) + ")");
            System.out.println("Тип: " + type);

            if (name.isEmpty()) {
                System.out.println("❌ ОШИБКА: Не указано имя");
                return null;
            }
            if (passport.isEmpty() || passport.length() != 10) {
                System.out.println("❌ ОШИБКА: Неправильный паспорт");
                return null;
            }
            if (deposit <= 0) {
                System.out.println("❌ ОШИБКА: Неправильная сумма");
                return null;
            }

            // Создание клиента
            type = normalizeClientType(type);
            Client client;

            switch (type.toLowerCase()) {
                case "вип":
                    client = new models.VIPClient(name, passport, deposit);
                    break;
                case "пенсионер":
                    client = new models.PensionerClient(name, passport, deposit);
                    break;
                default:
                    client = new models.SimpleClient(name, passport, deposit);
            }

            System.out.println("✅ УСПЕХ: Создан клиент " + client.getName());
            return client;

        } catch (Exception e) {
            System.out.println("❌ КРИТИЧЕСКАЯ ОШИБКА ПАРСИНГА: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Вспомогательный метод для извлечения значения
    private static String extractValue(String line, String[] prefixes) {
        for (String prefix : prefixes) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return line.trim();
    }

    // Нормализация типа клиента
    private static String normalizeClientType(String type) {
        if (type == null) return "Обычный клиент";

        String lowerType = type.trim().toLowerCase();

        if (lowerType.contains("вип") || lowerType.equals("vip")) {
            return "Вип";
        } else if (lowerType.contains("пенсион") || lowerType.equals("pensioner")) {
            return "Пенсионер";
        } else if (lowerType.contains("обычн") || lowerType.equals("regular")) {
            return "Обычный клиент";
        }

        return "Обычный клиент"; // значение по умолчанию
    }

    // Показ результата импорта
    private static void showImportResult(JFrame parent, int imported, int errors, String errorDetails) {
        StringBuilder message = new StringBuilder();
        message.append("Результат импорта:\n\n");
        message.append("✅ Успешно импортировано: ").append(imported).append(" клиентов\n");

        if (errors > 0) {
            message.append("❌ Ошибок/пропущено: ").append(errors).append("\n\n");
            if (!errorDetails.isEmpty()) {
                message.append("Детали ошибок:\n").append(errorDetails);
            }
        }

        JOptionPane.showMessageDialog(parent, message.toString(),
                "Импорт завершен",
                errors > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    public static void exportToCSV(Bank bank, JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Экспорт в CSV");

        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Паспорт;ФИО;Тип клиента;Вклад;Бонус");

                for (Client client : bank.getAllClients()) {
                    writer.println(String.format("%s;%s;%s;%.2f;%s",
                            client.getPassport(),
                            client.getName(),
                            client.getType(),
                            client.getDeposit(),
                            client.getBonusStrategy().getDescription()));
                }

                JOptionPane.showMessageDialog(parent,
                        "Данные успешно экспортированы в CSV файл",
                        "Экспорт CSV", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent,
                        "Ошибка экспорта: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}