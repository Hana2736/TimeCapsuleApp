package lol.hana.timecapsule;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static void zipFolder(File srcFolder, File destZipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(destZipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);


        File[] files = srcFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    addToZip(file, zos);
                }
            }
        }

        zos.close();
        fos.close();
    }

    public static void addToZip(File file, ZipOutputStream zos) throws IOException {
            FileInputStream fis = new FileInputStream(file);

            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }

            // Close the zip entry
            zos.closeEntry();
            fis.close();
    }



}
