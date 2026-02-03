package br.com.intuitivecare.ans.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {

    // Extrai um arquivo zeip para o diretório de destino
    // KISS: usa apenas API padrão do Java sem libs externas
    public static void unzip(Path zipFile, Path destDir) throws IOException {

        // Garante que o diretorio de destino exista
        Files.createDirectories(destDir);

        //aqui garante o fechamento automático dos streams
        try (InputStream fis = Files.newInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;

            // Percorre todos os arquivos/diretórios dentro do ZIP
            while ((entry = zis.getNextEntry()) != null) {

                // Resolve o caminho final do arquivo extraído
                Path newFile = destDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    // Se for diretorio, apenas cria
                    Files.createDirectories(newFile);
                } else {
                    // Garante que o diretorio pai exista antes de copiar o arquivo
                    Files.createDirectories(newFile.getParent());

                    // Copia o conteúdo do zip diretamente para o arquivo
                    Files.copy(zis, newFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }

                // Fecha a entrada atual do zip
                zis.closeEntry();
            }
        }

        System.out.println("Extraído: " + zipFile.getFileName());
    }
}


