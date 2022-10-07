package net.veldor.pdf_archiver.utils;

import net.veldor.pdf_parser.model.selection.Conclusion;

import java.io.File;
import java.util.Locale;

public class FilesHandler {
    public static File getFile(String file) {
        return new File(file);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getPdfLocation(Conclusion conclusion, File pdfPath) {
        File destination;
        int executionNumberByInt;
        // буду хранить по 1000 файлов в папке
        if (conclusion.executionNumber.startsWith("A")) {
            destination = new File(pdfPath, "Аврора");
            executionNumberByInt = Integer.parseInt(conclusion.executionNumber.replaceAll("\\D", ""));
        } else if (conclusion.executionNumber.startsWith("T")) {
            destination = new File(pdfPath, "КТ");
            executionNumberByInt = Integer.parseInt(conclusion.executionNumber.replaceAll("\\D", ""));
        } else {
            destination = new File(pdfPath, "НВН");
            executionNumberByInt = Integer.parseInt(conclusion.executionNumber.replaceAll("\\D", ""));
        }
        if (!destination.isDirectory()) {
            destination.mkdir();
        }
        int dirRange = executionNumberByInt / 1000;
        if (dirRange < 1) {
            destination = new File(destination, "0-1000");
        } else {
            destination = new File(destination, String.format(Locale.ENGLISH, "%d-%d", dirRange * 1000, (dirRange + 1) * 1000));
        }
        if (!destination.isDirectory()) {
            destination.mkdir();
        }
        File destinationFile;
        // generate unique
        destinationFile = new File(destination, String.format(Locale.ENGLISH, "%s_%s_%s.pdf", GrammarHandler.getFname(conclusion.patientName), conclusion.executionNumber, conclusion.executionArea));
        return destinationFile;
    }
}
