package lol.hana.timecapsule;

import org.apache.commons.io.FileUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncryptionUtils {
    public static void resetTempDir() {
        try {
            FileUtils.deleteDirectory(new File(MainActivity.workDir + "/temp/"));
            Files.deleteIfExists(Paths.get(MainActivity.workDir + "output.zip"));
            Files.deleteIfExists(Paths.get(MainActivity.workDir + "/Timestamp.txt"));
            Files.deleteIfExists(Paths.get(MainActivity.workDir + "/Title.txt"));
        } catch (IOException e) {
            //should be ok
        }
        try {
            Files.createDirectory(Paths.get(MainActivity.workDir + "/temp/"));
        } catch (Exception e) {
            //well then
        }
    }

    public static void decrypt_file(String workDir, String GUID) throws IOException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        //TODO: curl request to server with GUID to get private key and iv

        URL url = new URL("http://server.serv:2736/open=" + GUID);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String response;
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = readStream(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            urlConnection.disconnect();
        }

        String[] r_arr = response.split(":");
        //TODO: decrypt the file that matches GUID
        byte[] decoded = Base64.getDecoder().decode(r_arr[0]);
        byte[] iv = Base64.getDecoder().decode(r_arr[1]);


        SecretKey secretKey = new SecretKeySpec(decoded, 0, decoded.length, "AES");

        Cipher AesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        AesCipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);


        try (FileInputStream fis = new FileInputStream(workDir + "/" + GUID + "/timecapsule.bin");
             CipherInputStream ins = new CipherInputStream(fis, AesCipher);
             FileOutputStream fos = new FileOutputStream(workDir + "/temp")) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = ins.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        File x = new File(workDir + "/"+GUID+"/timecapsule.bin");
        x.delete();
        //TODO: display images or something, or onClick function that calls this can display the contents somehow
        //return null if server says "nah", upon which the UI will display an error toast with the date it should be unlocked
    }

    private static String readStream(InputStream in) throws Exception {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n"); // Append each line with a newline
            }
        }
        return response.toString().trim(); // Return the complete response as a string
    }

    public void encrypt_file(String workDir, long utc) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        URL url = new URL("http://10.0.2.2:2736/?encrypt=" + utc / 1000L);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String response;
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = readStream(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            urlConnection.disconnect();
        }

        String[] r_arr = response.split(":");

        String guid = r_arr[0];
        byte[] decoded = Base64.getDecoder().decode(r_arr[1]);
        byte[] iv = Base64.getDecoder().decode(r_arr[2]);


        SecretKey secretKey = new SecretKeySpec(decoded, 0, decoded.length, "AES");

        Cipher AesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        AesCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);


        byte[] zipBytes = Files.readAllBytes(Paths.get(workDir + "/output.zip"));

        File guidDir = Files.createDirectory(Paths.get(workDir + "/" + guid)).toFile();
        //TODO: store zipBytes in guidir
        byte[] c_text = AesCipher.doFinal(zipBytes);

        File encryptedFile = new File(guidDir, "timecapsule.bin");
        try (FileOutputStream os = new FileOutputStream((encryptedFile))) {
            os.write(c_text);
        }


        File x = new File(workDir + "/output.zip");
        x.delete();
        Files.move(Paths.get(workDir + "/Title.txt"), Paths.get(workDir + "/" + guid + "/Title.txt"));
        Files.move(Paths.get(workDir + "/Timestamp.txt"), Paths.get(workDir + "/" + guid + "/Timestamp.txt"));

        resetTempDir();

    }
}
