package br.com.intuitivecare.ans.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {
	
    public static void unzip(Path zipFile, Path destDir) throws IOException {

        // Cria o diretório de destino se não existir
        Files.createDirectories(destDir);

        try (InputStream fis = Files.newInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;

            // Percorre todos os arquivos dentro do ZIP
            while ((entry = zis.getNextEntry()) != null) {

                Path newFile = destDir.resolve(entry.getName());

                // Se for diretório, cria
                if (entry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    Files.createDirectories(newFile.getParent());
                    Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                }

                zis.closeEntry();
            }
        }
    }
}
