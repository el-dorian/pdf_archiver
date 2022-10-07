package net.veldor.pdf_archiver.model;

import net.veldor.pdf_archiver.utils.Db;
import net.veldor.pdf_archiver.utils.FilesHandler;
import net.veldor.pdf_parser.model.exception.ArgumentNotFoundException;
import net.veldor.pdf_parser.model.handler.Handler;
import net.veldor.pdf_parser.model.selection.Conclusion;

import java.io.File;

public class Archiver {
    public boolean archive(File conclusionFile, File archiveRoot) throws Exception {
        // get parsed conclusion info
        Conclusion conclusion = new Handler(conclusionFile).parse();
        conclusion.file = FilesHandler.getPdfLocation(conclusion, archiveRoot);
        conclusion.filePath = conclusion.file.getAbsolutePath().replace(
                archiveRoot.getAbsolutePath(),
                ""
        ).replace("\\", "\\\\");
        Db.getInstance().handleConclusion(conclusion);
        if (!conclusionFile.renameTo(conclusion.file)) {
            throw new ArgumentNotFoundException("Не удалось переместить файл в архив!");
        }
        return true;
    }
}
