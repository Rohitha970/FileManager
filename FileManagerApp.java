import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileManagerApp {

    // ANSI Console Colors
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            showMenu();
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> readFile();
                case "2" -> writeFile();
                case "3" -> modifyFile();
                case "4" -> deleteFile();
                case "5" -> fileInfo();
                case "6" -> {
                    System.out.println(GREEN + "Exiting FileManagerApp. Goodbye!" + RESET);
                    return;
                }
                default -> System.out.println(RED + "Invalid option. Please try again." + RESET);
            }
        }
    }

    private static void showMenu() {
        System.out.println(PURPLE + "\n==== FILE MANAGER APP ====" + RESET);
        System.out.println("1. 📖 Read a File");
        System.out.println("2. ✍️ Write a New File");
        System.out.println("3. 🔄 Modify File (Append/Overwrite/Insert)");
        System.out.println("4. ❌ Delete a File");
        System.out.println("5. ℹ️ View File Info");
        System.out.println("6. 🚪 Exit");
        System.out.print(YELLOW + "Choose an option (1–6): " + RESET);
    }

    private static void readFile() {
        System.out.print(YELLOW + "Enter file path to read: " + RESET);
        String path = scanner.nextLine();
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            System.out.println(RED + "❌ File not found." + RESET);
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            System.out.println(GREEN + "\n--- FILE CONTENT ---" + RESET);
            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                System.out.printf(PURPLE + "%3d | " + RESET, lineNum++);
                System.out.println(highlightSyntax(line));
            }
        } catch (IOException e) {
            System.out.println(RED + "Error reading file: " + e.getMessage() + RESET);
        }
    }

    private static void writeFile() {
        System.out.print(YELLOW + "Enter file path to create/write: " + RESET);
        String path = scanner.nextLine();
        Path filePath = Paths.get(path);
        if (Files.exists(filePath)) {
            System.out.print(RED + "File exists. Overwrite? (y/n): " + RESET);
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) return;
        }

        System.out.println(YELLOW + "Enter text (type 'END' to save):" + RESET);
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append(System.lineSeparator());
        }

        try {
            Files.write(filePath, content.toString().getBytes());
            System.out.println(GREEN + "✅ File saved successfully!" + RESET);
        } catch (IOException e) {
            System.out.println(RED + "❌ Error writing file: " + e.getMessage() + RESET);
        }
    }

    private static void modifyFile() {
        System.out.print(YELLOW + "Enter file path to modify: " + RESET);
        Path path = Paths.get(scanner.nextLine());

        if (!Files.exists(path)) {
            System.out.println(RED + "❌ File does not exist." + RESET);
            return;
        }

        System.out.println(YELLOW + """
            Choose modification type:
            1. Append
            2. Overwrite
            3. Insert at Line
            """ + RESET);
        String option = scanner.nextLine();

        switch (option) {
            case "1" -> appendToFile(path);
            case "2" -> overwriteFile(path);
            case "3" -> insertAtLine(path);
            default -> System.out.println(RED + "Invalid choice." + RESET);
        }
    }

    private static void appendToFile(Path path) {
        System.out.println(YELLOW + "Enter text to append (type 'END' to finish):" + RESET);
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append(System.lineSeparator());
        }

        try {
            Files.write(path, content.toString().getBytes(), StandardOpenOption.APPEND);
            System.out.println(GREEN + "✅ Content appended!" + RESET);
        } catch (IOException e) {
            System.out.println(RED + "❌ Error appending: " + e.getMessage() + RESET);
        }
    }

    private static void overwriteFile(Path path) {
        System.out.println(RED + "⚠️ WARNING: This will erase all content." + RESET);
        System.out.print("Proceed? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) return;

        System.out.println(YELLOW + "Enter new content (type 'END' to finish):" + RESET);
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append(System.lineSeparator());
        }

        try {
            Files.write(path, content.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println(GREEN + "✅ File overwritten!" + RESET);
        } catch (IOException e) {
            System.out.println(RED + "❌ Error overwriting file: " + e.getMessage() + RESET);
        }
    }

    private static void insertAtLine(Path path) {
        System.out.print(YELLOW + "Enter line number to insert at: " + RESET);
        int lineNum = Integer.parseInt(scanner.nextLine());

        System.out.println(YELLOW + "Enter text to insert (type 'END' to finish):" + RESET);
        StringBuilder insertText = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            insertText.append(line).append(System.lineSeparator());
        }

        try {
            List<String> lines = Files.readAllLines(path);
            if (lineNum < 1 || lineNum > lines.size() + 1) {
                System.out.println(RED + "Invalid line number!" + RESET);
                return;
            }
            lines.add(lineNum - 1, insertText.toString());
            Files.write(path, lines);
            System.out.println(GREEN + "✅ Content inserted at line " + lineNum + RESET);
        } catch (IOException e) {
            System.out.println(RED + "❌ Error inserting content: " + e.getMessage() + RESET);
        }
    }

    private static void deleteFile() {
        System.out.print(YELLOW + "Enter file path to delete: " + RESET);
        Path path = Paths.get(scanner.nextLine());
        if (!Files.exists(path)) {
            System.out.println(RED + "❌ File not found!" + RESET);
            return;
        }

        System.out.print(RED + "Are you sure you want to delete this file? (y/n): " + RESET);
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) return;

        try {
            Files.delete(path);
            System.out.println(GREEN + "✅ File deleted successfully." + RESET);
        } catch (IOException e) {
            System.out.println(RED + "❌ Error deleting file: " + e.getMessage() + RESET);
        }
    }

    private static void fileInfo() {
        System.out.print(YELLOW + "Enter file path: " + RESET);
        Path path = Paths.get(scanner.nextLine());
        if (!Files.exists(path)) {
            System.out.println(RED + "❌ File not found." + RESET);
            return;
        }

        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            System.out.println(BLUE + "\n--- FILE INFO ---" + RESET);
            System.out.println("Name: " + path.getFileName());
            System.out.println("Size: " + attr.size() + " bytes");
            System.out.println("Created: " + formatter.format(attr.creationTime().toInstant().atZone(ZoneId.systemDefault())));
            System.out.println("Last Modified: " + formatter.format(attr.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault())));
        } catch (IOException e) {
            System.out.println(RED + "Error retrieving info: " + e.getMessage() + RESET);
        }
    }

    private static String highlightSyntax(String line) {
        String[] keywords = {"public", "class", "void", "static", "if", "else", "import", "try", "catch"};
        for (String keyword : keywords) {
            line = line.replace(keyword, YELLOW + keyword + RESET);
        }
        line = line.replaceAll("\".*?\"", GREEN + "$0" + RESET);
        line = line.replaceAll("\\b\\d+\\b", BLUE + "$0" + RESET);
        return line;
    }
}
