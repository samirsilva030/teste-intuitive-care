package br.com.intuitivecare.ans.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utilitário para compactação de arquivos no formato zip.
 * Responsavel por gerar o pacote final de entrega (Requisito 3).
 */
public class ZipCompressor {

    public static void zip(Path file, Path zipDest) throws IOException {

        try (OutputStream fos = Files.newOutputStream(zipDest);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            ZipEntry entry = new ZipEntry(file.getFileName().toString());
            zos.putNextEntry(entry);

            Files.copy(file, zos);

            zos.closeEntry();
        }
    }
}
