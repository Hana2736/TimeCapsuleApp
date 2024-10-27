package lol.hana.timecapsule;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CreateCapsuleActivity extends AppCompatActivity {
    public static boolean successfullyCreated;
    long dateStamp;
    List<Uri> selectedMedia;
    String capsTitle;
    String capsStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        selectedMedia = new LinkedList<>();
        capsStory = "";
        capsTitle = "";
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.cap_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.create), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button dateSel = findViewById(R.id.dateSelector);
        dateSel.setOnClickListener(v -> {

            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            //request today onwards
            CalendarConstraints constr = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build();
            builder.setCalendarConstraints(constr);
            MaterialDatePicker pick = builder.build();

            //when we select OK on the calendar, do this
            pick.addOnPositiveButtonClickListener(o -> {
                //Toast.makeText(getApplicationContext(),"Selected date is "+o,Toast.LENGTH_LONG).show();
                dateStamp = (long) o;


                //convert to midnight on the day of the opening
                ZoneId zoneId = ZoneId.systemDefault();
                ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
                ZoneOffset offset = zonedDateTime.getOffset();
                dateStamp -= 1000L * offset.getTotalSeconds();

                Date date = new Date(dateStamp);
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
                String formattedDate = formatter.format(date);
                dateSel.setText("Decrypts " + formattedDate);
            });
            pick.show(getSupportFragmentManager(), "DATE_PICKER");
        });

// Define the contract to pick multiple images
        ActivityResultContracts.PickMultipleVisualMedia pickMultipleVisualMedia = new ActivityResultContracts.PickMultipleVisualMedia(5);
        Button pickImagesButton = findViewById(R.id.uploadFiles);
// Register the launcher for the activity result
        ActivityResultLauncher<PickVisualMediaRequest> pickMultipleImagesLauncher = registerForActivityResult(pickMultipleVisualMedia, uris -> {
            if (uris != null && !uris.isEmpty()) {
                // Handle the list of selected Uris here
                //Log.d("CreateCapsuleActivity", "Selected Image URI: " + uri.toString());
                // You can display the images or perform other actions with the URIs
                selectedMedia.addAll(uris);

                if (selectedMedia != null && !selectedMedia.isEmpty()) {
                    // Handle the list of selected Uris here
                    for (int i = 0; i < selectedMedia.size(); i++) {
                        Uri uri = selectedMedia.get(i);
                        Log.d("CreateCapsuleActivity", "Selected Image URI: " + uri.toString());
                        // You can display the images or perform other actions with the URIs

                        if (i == 0) { // If it's the first image
                            // Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            // BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                            pickImagesButton.setVisibility(View.INVISIBLE);
                            pickImagesButton.setEnabled(false);
                            ImageView img = findViewById(R.id.firstPic);
                            // img.setForeground(bitmapDrawable);
                            img.setImageURI(selectedMedia.get(0));
                            img.setVisibility(View.VISIBLE);
                        }
                    }

                }
            }
        });

// Set up a button to trigger the image picker

        pickImagesButton.setOnClickListener(v -> {
            // Create a request with max items
            PickVisualMediaRequest request = new PickVisualMediaRequest.Builder()
                    .build();

            // Launch the image picker
            pickMultipleImagesLauncher.launch(request);
        });

        Button sealBtn = findViewById(R.id.submitButton);
        sealBtn.setOnClickListener(v -> {
            //find the text boxes and store their contents
            EditText nameEntry = findViewById(R.id.inputTitle);
            EditText storyEntry = findViewById(R.id.inputSummary);
            capsTitle = nameEntry.getText().toString();
            capsStory = storyEntry.getText().toString();

            //check that theyve added photos and text
            if (!checkFields()) {
                Snackbar.make(getWindow().getDecorView().getRootView(), "Please fill all fields.", 5000).show();
                return;
            }
            Snackbar.make(getWindow().getDecorView().getRootView(), "Hang on, sealing the time capsule...", 5000).show();

            //prevent editing while the app is busy
            lockAllFields();
            new Thread(() -> {
                //prepare the environment
                EncryptionUtils.resetTempDir();
                try {
                    int i = 0;
                    //copy all of the images the user selected from their gallery into the app private storage
                    for (Uri media : selectedMedia) {
                        ZipUtils.copyFile(this, media, Paths.get(MainActivity.workDir + "/temp/" + i));
                        i++;
                    }

                    //store their story and the title and unlock dates on the disk
                    Files.write(Paths.get(MainActivity.workDir + "/temp/Story.txt"), capsStory.getBytes(StandardCharsets.UTF_8));
                    Files.write(Paths.get(MainActivity.workDir + "/Title.txt"), capsTitle.getBytes(StandardCharsets.UTF_8));
                    Files.write(Paths.get(MainActivity.workDir + "/Timestamp.txt"), String.valueOf(dateStamp).getBytes(StandardCharsets.UTF_8));


                    ZoneId systemZone = ZoneId.systemDefault();
                    ZonedDateTime currentTime = ZonedDateTime.now(systemZone);


                    Files.write(Paths.get(MainActivity.workDir + "/TimeCreated.txt"), String.valueOf(currentTime.toInstant().toEpochMilli()).getBytes(StandardCharsets.UTF_8));
                    //Get the temporary working space
                    File temp = new File(String.valueOf(Path.of(MainActivity.workDir + "/temp/")));//Files.createDirectory(Paths.get(MainActivity.workDir.toString() + "/temp/")).toFile();

                    //Toast.makeText(getBaseContext(),temp.toPath().toAbsolutePath().toString(),Toast.LENGTH_LONG).show();

                    //compress the story and photos inside a zip
                    File outputZipFile = new File(MainActivity.workDir.toString() + "/output.zip");
                    ZipUtils.zipFolder(temp, outputZipFile);

                    //encrypt the zip file containing the user data. this will get fresh keys from the server and
                    //store the opening time on the server
                    EncryptionUtils encryptionUtils = new EncryptionUtils();
                    encryptionUtils.encrypt_file(MainActivity.workDir.toString(), dateStamp);

                    //notify the homepage we added a capsule and close this view
                    successfullyCreated = true;
                    runOnUiThread(this::finish);

                } catch (Exception e) {
                    Log.e("Writing", "We seriously broke something: " + e);
                }
            }).start();
        });
        DynamicColors.applyToActivitiesIfAvailable(getApplication());
    }

    boolean checkFields() {
        if (dateStamp == 0)
            return false;
        if (capsTitle.isEmpty())
            return false;
        if (capsStory.isEmpty())
            return false;
        return !selectedMedia.isEmpty();
    }

    void lockAllFields() {
        findViewById(R.id.dateSelector).setEnabled(false);
        findViewById(R.id.inputTitle).setEnabled(false);
        findViewById(R.id.inputSummary).setEnabled(false);
        findViewById(R.id.submitButton).setEnabled(false);
    }

}