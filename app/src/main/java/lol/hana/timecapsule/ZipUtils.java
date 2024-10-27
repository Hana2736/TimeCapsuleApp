package lol.hana.timecapsule;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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

    public static void unzip(File zip, File dest) throws IOException {

        FileInputStream fis = new FileInputStream(zip);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry zipEntry;

        while ((zipEntry = zis.getNextEntry()) != null) {
            File newFile = new File(dest, zipEntry.getName());
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = zis.read(buffer)) >= 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();

            zis.closeEntry();
        }


        zis.close();
        fis.close();
    }

    public static void copyFile(Context c, Uri input, Path output) {
        try {
            InputStream fis = c.getContentResolver().openInputStream(input);
            File dest = new File(output.toString());
            FileOutputStream fos = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                fos.write(buffer, 0, length);
            }
        } catch (Exception e) {
            Log.e("copy", "copy fail " + e);
        }

    }

}
