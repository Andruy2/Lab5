package utils;

import javax.swing.*;
import java.io.File;
import java.nio.file.*;

public class DirectoryUtils {

    // Основные папки для работы приложения
    public static class AppDirectories {
        public static final String USER_HOME = System.getProperty("user.home");
        public static final String DOWNLOADS = USER_HOME + File.separator + "Downloads";
        public static final String DESKTOP = USER_HOME + File.separator + "Desktop";
        public static final String DOCUMENTS = USER_HOME + File.separator + "Documents";

        // Папка приложения в Documents
        public static final String APP_DATA = DOCUMENTS + File.separator + "BankAppData";

        // Папка для логов
        public static final String LOGS = APP_DATA + File.separator + "logs";

        // Папка для временных файлов
        public static final String TEMP = System.getProperty("java.io.tmpdir") + File.separator + "BankApp";
    }

    // Инициализация папок приложения
    public static void initializeAppDirectories() {
        createDirectoryIfNotExists(AppDirectories.APP_DATA);
        createDirectoryIfNotExists(AppDirectories.LOGS);
        createDirectoryIfNotExists(AppDirectories.TEMP);
    }

    // Создание папки, если она не существует
    public static boolean createDirectoryIfNotExists(String path) {
        try {
            Path dir = Paths.get(path);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                System.out.println("Создана папка: " + path);
                return true;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка создания папки " + path + ": " + e.getMessage());
            return false;
        }
    }

    // Получить доступную папку для сохранения файлов
    public static String getAvailableSaveDirectory() {
        // Пробуем в порядке приоритета:
        // 1. Downloads
        if (isDirectoryWritable(AppDirectories.DOWNLOADS)) {
            return AppDirectories.DOWNLOADS;
        }

        // 2. Desktop
        if (isDirectoryWritable(AppDirectories.DESKTOP)) {
            return AppDirectories.DESKTOP;
        }

        // 3. Documents
        if (isDirectoryWritable(AppDirectories.DOCUMENTS)) {
            return AppDirectories.DOCUMENTS;
        }

        // 4. Папка приложения в Documents
        if (isDirectoryWritable(AppDirectories.APP_DATA)) {
            return AppDirectories.APP_DATA;
        }

        // 5. Временная папка
        return AppDirectories.TEMP;
    }

    // Проверка возможности записи в папку
    public static boolean isDirectoryWritable(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists() || !dir.isDirectory()) {
                return false;
            }

            // Пробуем создать тестовый файл
            File testFile = new File(dir, ".write_test.tmp");
            if (testFile.createNewFile()) {
                testFile.delete();
                return true;
            }
            return false;

        } catch (Exception e) {
            return false;
        }
    }

    // Получить свободное место в папке (в мегабайтах)
    public static long getFreeSpace(String path) {
        try {
            File dir = new File(path);
            return dir.getFreeSpace() / (1024 * 1024); // MB
        } catch (Exception e) {
            return 0;
        }
    }

    // Проверить, достаточно ли свободного места
    public static boolean hasEnoughSpace(String path, long requiredMB) {
        return getFreeSpace(path) >= requiredMB;
    }

    // Очистка временных файлов
    public static void cleanupTempFiles() {
        try {
            File tempDir = new File(AppDirectories.TEMP);
            if (tempDir.exists() && tempDir.isDirectory()) {
                File[] files = tempDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().endsWith(".tmp") ||
                                file.getName().startsWith("temp_")) {
                            file.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки очистки
        }
    }

    // Показать диалог выбора папки
    public static String selectDirectory(JFrame parent, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(getAvailableSaveDirectory()));

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    // Получить информацию о папке
    public static String getDirectoryInfo(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                return "Папка не существует";
            }

            long freeSpaceMB = getFreeSpace(path);
            long totalSpaceMB = dir.getTotalSpace() / (1024 * 1024);
            long usedSpaceMB = totalSpaceMB - freeSpaceMB;

            return String.format(
                    "Папка: %s\n" +
                            "Свободно: %d MB\n" +
                            "Занято: %d MB\n" +
                            "Всего: %d MB\n" +
                            "Доступна для записи: %s",
                    dir.getName(),
                    freeSpaceMB,
                    usedSpaceMB,
                    totalSpaceMB,
                    isDirectoryWritable(path) ? "Да" : "Нет"
            );

        } catch (Exception e) {
            return "Ошибка получения информации: " + e.getMessage();
        }
    }
}