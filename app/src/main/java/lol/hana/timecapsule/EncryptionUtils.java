package lol.hana.timecapsule;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.net.HttpURLConnection;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.FileUtils;

public class EncryptionUtils {
    public void encrypt_file(String workDir) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        Long utc = 324L;
        URL url = new URL("http://server.serv:2736/encrypt="+Long.toString(utc));
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


        byte[] zipBytes = Files.readAllBytes(Paths.get(workDir +  "/output.zip"));

        File guidDir = Files.createDirectory(Paths.get(workDir + "/" + guid)).toFile();
        //TODO: store zipBytes in guidir
        byte[] c_text = AesCipher.doFinal(zipBytes);

        File encryptedFile = new File(guidDir, "timecapsule.bin");
        try (FileOutputStream os = new FileOutputStream((encryptedFile))){
            os.write(c_text);
        }


        File x = new File(workDir +  "/output.zip");
        x.delete();
        FileUtils.deleteDirectory(new File(workDir +  "/temp"));

    }

    public static void decrypt_file(String workDir, String GUID) {
        //TODO: curl request to server with GUID to get private key and iv
        //TODO: decrypt the file that matches GUID
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
}
