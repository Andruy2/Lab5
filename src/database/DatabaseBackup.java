package database;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Comparator;

public class DatabaseBackup {

    // Папка для сохранения по умолчанию - Downloads
    private static final String DEFAULT_BACKUP_DIR = getDownloadsPath();

    // Получаем путь к папке Downloads
    private static String getDownloadsPath() {
        String userHome = System.getProperty("user.home");
        String downloads = userHome + File.separator + "Downloads";

        // Проверяем, существует ли папка Downloads
        File downloadsDir = new File(downloads);
        if (!downloadsDir.exists() || !downloadsDir.isDirectory()) {
            // Если Downloads нет, используем Desktop
            String desktop = userHome + File.separator + "Desktop";
            File desktopDir = new File(desktop);
            if (desktopDir.exists() && desktopDir.isDirectory()) {
                return desktop;
            } else {
                // Если и Desktop нет, используем домашнюю папку
                return userHome;
            }
        }
        return downloads;
    }

    public static void backupDatabase(JFrame parent) {
        JFileChooser fileChooser = createFileChooser("Сохранить резервную копию базы данных");

        // Устанавливаем имя файла по умолчанию с датой
        String defaultFileName = "bank_backup_" + getCurrentTimestamp() + ".db";
        fileChooser.setSelectedFile(new File(DEFAULT_BACKUP_DIR, defaultFileName));

        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File backupFile = fileChooser.getSelectedFile();

            // Добавляем расширение .db если его нет
            if (!backupFile.getName().toLowerCase().endsWith(".db")) {
                backupFile = new File(backupFile.getAbsolutePath() + ".db");
            }

            // Проверяем, можно ли записать в выбранное место
            if (!canWriteToLocation(backupFile)) {
                showErrorMessage(parent,
                        "Не удается сохранить файл в выбранное место.\n" +
                                "Попробуйте другую папку или проверьте права доступа.",
                        "Ошибка записи");
                return;
            }

            performBackup(parent, backupFile);
        }
    }

    public static void restoreDatabase(JFrame parent) {
        JFileChooser fileChooser = createFileChooser("Выберите файл резервной копии для восстановления");

        // Начинаем с папки Downloads
        fileChooser.setCurrentDirectory(new File(DEFAULT_BACKUP_DIR));

        // Устанавливаем фильтр файлов
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".db") || name.endsWith(".sqlite") ||
                        name.endsWith(".sqlite3") || name.endsWith(".backup");
            }

            @Override
            public String getDescription() {
                return "Файлы базы данных (*.db, *.sqlite, *.sqlite3, *.backup)";
            }
        });

        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File backupFile = fileChooser.getSelectedFile();

            // Проверяем файл
            String validationError = validateBackupFile(backupFile);
            if (validationError != null) {
                showErrorMessage(parent, validationError, "Ошибка файла");
                return;
            }

            // Подтверждение восстановления
            if (!confirmRestore(parent, backupFile)) {
                return;
            }

            performRestore(parent, backupFile);
        }
    }

    private static JFileChooser createFileChooser(String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setCurrentDirectory(new File(DEFAULT_BACKUP_DIR));
        return chooser;
    }

    private static boolean canWriteToLocation(File file) {
        try {
            // Проверяем родительскую папку
            File parentDir = file.getParentFile();
            if (parentDir == null || !parentDir.exists()) {
                return false;
            }

            // Пробуем создать тестовый файл
            File testFile = new File(parentDir, "test_write.tmp");
            if (testFile.createNewFile()) {
                testFile.delete();
                return true;
            }
            return false;

        } catch (Exception e) {
            return false;
        }
    }

    private static String validateBackupFile(File file) {
        if (!file.exists()) {
            return "Файл не существует: " + file.getName();
        }

        if (!file.canRead()) {
            return "Нет прав на чтение файла: " + file.getName();
        }

        if (file.length() == 0) {
            return "Файл пуст: " + file.getName();
        }

        // Минимальный размер для SQLite файла
        if (file.length() < 100) {
            return "Файл слишком маленький для базы данных SQLite: " + file.getName();
        }

        return null; // Файл валиден
    }

    private static boolean confirmRestore(JFrame parent, File backupFile) {
        String fileInfo = String.format(
                "Имя файла: %s\n" +
                        "Размер: %s\n" +
                        "Дата изменения: %s",
                backupFile.getName(),
                formatFileSize(backupFile.length()),
                new Date(backupFile.lastModified()).toString()
        );

        int choice = JOptionPane.showConfirmDialog(parent,
                "⚠️  ВНИМАНИЕ: ВОССТАНОВЛЕНИЕ БАЗЫ ДАННЫХ\n\n" +
                        "Это действие ПЕРЕЗАПИШЕТ текущую базу данных!\n" +
                        "Все текущие данные будут УДАЛЕНЫ и заменены данными из резервной копии.\n\n" +
                        "Информация о файле восстановления:\n" +
                        fileInfo + "\n\n" +
                        "Вы уверены, что хотите продолжить?",
                "Подтверждение восстановления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        return choice == JOptionPane.YES_OPTION;
    }

    private static void performBackup(JFrame parent, File backupFile) {
        try {
            Path source = Paths.get("bank_database.db");

            // Проверяем, существует ли исходная БД
            if (!Files.exists(source) || Files.size(source) == 0) {
                showWarningMessage(parent,
                        "База данных пуста или не существует.\n" +
                                "Сначала добавьте клиентов для создания резервной копии.",
                        "База данных пуста");
                return;
            }

            // Создаем резервную копию текущей БД (если существует)
            createPreBackupIfNeeded();

            // Копируем файл
            Files.copy(source, backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Проверяем, что копия создана успешно
            if (!backupFile.exists() || backupFile.length() == 0) {
                throw new IOException("Не удалось создать резервную копию");
            }

            showSuccessMessage(parent,
                    "✅ РЕЗЕРВНАЯ КОПИЯ УСПЕШНО СОЗДАНА\n\n" +
                            "Файл: " + backupFile.getName() + "\n" +
                            "Размер: " + formatFileSize(backupFile.length()) + "\n" +
                            "Путь: " + backupFile.getParent() + "\n\n" +
                            "Резервная копия сохранена в папке Downloads.",
                    "Резервное копирование завершено");

        } catch (Exception e) {
            showErrorMessage(parent,
                    "❌ ОШИБКА СОЗДАНИЯ РЕЗЕРВНОЙ КОПИИ\n\n" +
                            "Причина: " + getFriendlyErrorMessage(e) + "\n\n" +
                            "Рекомендации:\n" +
                            "1. Закройте базу данных в других программах\n" +
                            "2. Проверьте свободное место на диске\n" +
                            "3. Попробуйте другую папку для сохранения",
                    "Ошибка резервного копирования");
        }
    }

    private static void performRestore(JFrame parent, File backupFile) {
        try {
            Path source = backupFile.toPath();
            Path target = Paths.get("bank_database.db");

            // Создаем резервную копию текущей БД перед заменой
            createPreRestoreBackup();

            // Копируем файл резервной копии
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            // Проверяем, что восстановление прошло успешно
            if (!Files.exists(target) || Files.size(target) == 0) {
                throw new IOException("Не удалось восстановить базу данных");
            }

            showSuccessMessage(parent,
                    "✅ БАЗА ДАННЫХ УСПЕШНО ВОССТАНОВЛЕНА\n\n" +
                            "Информация:\n" +
                            "• Файл: " + backupFile.getName() + "\n" +
                            "• Размер: " + formatFileSize(backupFile.length()) + "\n" +
                            "• Старая БД сохранена как: bank_database_pre_restore.db\n\n" +
                            "⚠️  ПЕРЕЗАПУСТИТЕ ПРИЛОЖЕНИЕ для применения изменений!",
                    "Восстановление завершено");

        } catch (Exception e) {
            showErrorMessage(parent,
                    "❌ ОШИБКА ВОССТАНОВЛЕНИЯ\n\n" +
                            "Причина: " + getFriendlyErrorMessage(e) + "\n\n" +
                            "Попробуйте:\n" +
                            "1. Проверить целостность файла резервной копии\n" +
                            "2. Освободить место на диске\n" +
                            "3. Запустить приложение от имени администратора",
                    "Ошибка восстановления");
        }
    }

    private static void createPreBackupIfNeeded() throws IOException {
        Path currentDb = Paths.get("bank_database.db");
        if (Files.exists(currentDb) && Files.size(currentDb) > 0) {
            Path backup = Paths.get("bank_database_autobackup.db");
            Files.copy(currentDb, backup, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void createPreRestoreBackup() throws IOException {
        Path currentDb = Paths.get("bank_database.db");
        if (Files.exists(currentDb) && Files.size(currentDb) > 0) {
            Path backup = Paths.get("bank_database_pre_restore.db");
            Files.copy(currentDb, backup, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // Автоматическое резервное копирование при запуске
    public static void autoBackupOnStart() {
        try {
            Path source = Paths.get("bank_database.db");
            if (!Files.exists(source) || Files.size(source) == 0) {
                return; // БД пуста или не существует
            }

            // Создаем папку для автоматических бэкапов
            Path autoBackupDir = Paths.get("auto_backups");
            if (!Files.exists(autoBackupDir)) {
                Files.createDirectories(autoBackupDir);
            }

            // Имя файла с датой и временем
            String timestamp = getCurrentTimestamp();
            String backupFileName = "auto_backup_" + timestamp + ".db";
            Path backupPath = autoBackupDir.resolve(backupFileName);

            // Копируем
            Files.copy(source, backupPath, StandardCopyOption.REPLACE_EXISTING);

            // Удаляем старые бэкапы (оставляем последние 5)
            cleanupOldBackups(autoBackupDir, 5);

        } catch (Exception e) {
            // Игнорируем ошибки авто-бэкапа
        }
    }

    private static void cleanupOldBackups(Path backupDir, int keepCount) throws IOException {
        try (var stream = Files.list(backupDir)) {
            var backups = stream
                    .filter(p -> p.getFileName().toString().startsWith("auto_backup_"))
                    .sorted(Comparator.comparingLong(p -> {
                        try {
                            return Files.getLastModifiedTime(p).toMillis();
                        } catch (IOException e) {
                            return 0;
                        }
                    }))
                    .toList();

            // Удаляем все, кроме последних keepCount файлов
            for (int i = 0; i < backups.size() - keepCount; i++) {
                Files.deleteIfExists(backups.get(i));
            }
        }
    }

    // Вспомогательные методы
    private static String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " Б";
        if (bytes < 1024 * 1024) return String.format("%.1f КБ", bytes / 1024.0);
        return String.format("%.1f МБ", bytes / (1024.0 * 1024.0));
    }

    private static String getFriendlyErrorMessage(Exception e) {
        String message = e.getMessage();

        if (message == null) return "Неизвестная ошибка";

        if (message.contains("OneDrive") || message.contains("onedrive")) {
            return "Проблема с доступом к OneDrive. Используйте локальную папку.";
        }

        if (message.contains("permission") || message.contains("denied")) {
            return "Отсутствуют права доступа. Запустите программу от имени администратора.";
        }

        if (message.contains("being used") || message.contains("used by another process")) {
            return "Файл используется другой программой. Закройте все программы, работающие с БД.";
        }

        if (message.contains("disk") || message.contains("space")) {
            return "Недостаточно места на диске.";
        }

        if (message.contains("path") || message.contains("invalid")) {
            return "Некорректный путь к файлу. Убедитесь, что путь не содержит недопустимых символов.";
        }

        return message;
    }

    private static void showErrorMessage(JFrame parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private static void showWarningMessage(JFrame parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private static void showSuccessMessage(JFrame parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // Метод для проверки доступности папки Downloads
    public static boolean isDownloadsAccessible() {
        File downloads = new File(DEFAULT_BACKUP_DIR);
        return downloads.exists() && downloads.canRead() && downloads.canWrite();
    }

    // Получить путь к папке по умолчанию для сохранения
    public static String getDefaultBackupPath() {
        return DEFAULT_BACKUP_DIR;
    }
}