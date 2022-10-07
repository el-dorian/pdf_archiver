package net.veldor.pdf_archiver.utils;

import net.veldor.pdf_parser.model.handler.Handler;
import net.veldor.pdf_parser.model.selection.Conclusion;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Parser {
    static File destination;

    public static void parse(String file, String destinationDir) throws Exception {
        System.out.println(file);
        File sourceFile = FilesHandler.getFile(file);
        if (!sourceFile.isFile()) {
            System.out.println("Не найден файл");
        } else {
            destination = FilesHandler.getFile(destinationDir);
            if (!destination.isDirectory()) {
                System.out.println("Не найдена папка назначения");
            } else {
                handlePDF(sourceFile);
            }
        }
    }

    private static void handlePDF(File sourceFile) throws Exception {
        if (sourceFile.isFile() && destination.isDirectory()) {
            Conclusion conclusion = new Handler(sourceFile).parse();
            File destinationFile = FilesHandler.getPdfLocation(conclusion, destination);
            conclusion.filePath = destinationFile.getAbsolutePath().replace(destination.getAbsolutePath(), "").replace("\\", "\\\\");
            // save conclusion to DB
            try {
                Db.getInstance().handleConclusion(conclusion);
            } catch (ExistsInDbException e) {
                // просто удалю файл
                //noinspection unused
                // проверю наличие файла на сервере
                if (!destinationFile.isFile()) {
                    System.out.println(destinationFile.getAbsolutePath());
                    System.out.println(sourceFile.getAbsolutePath());
                    try {
                        boolean result = sourceFile.renameTo(destinationFile);
                        System.out.println(result);
                    } catch (Exception er) {
                        er.printStackTrace();
                    }
                }
                boolean ignored = sourceFile.delete();
                return;
            }
            // move file to destination
            @SuppressWarnings("unused") boolean ignored = sourceFile.renameTo(destinationFile);
            // copy file for future
            Files.copy(destinationFile.toPath(), new File(destination, destinationFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Архивировано " + conclusion.filePath);
        }
    }
}
