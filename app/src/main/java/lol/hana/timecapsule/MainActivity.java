package lol.hana.timecapsule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static Path workDir;

    @Override
    protected void onResume() {
        super.onResume();
        if (CreateCapsuleActivity.successfullyCreated) {
            CreateCapsuleActivity.successfullyCreated = false;
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        workDir = getApplicationContext().getFilesDir().toPath();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout layout = findViewById(R.id.mainPageScrollLayout);

        Button createB = findViewById(R.id.createCapsuleBtn);
        createB.setOnClickListener(v -> {
            Intent i = new Intent(this, CreateCapsuleActivity.class);
            startActivity(i);
        });


        inflateCapsuleCards(this, layout);

        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        //getActionBar().hide();
        //WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }


    public void inflateCapsuleCards(Context ctx, LinearLayout layout) {

        File[] files = new File(workDir.toString()).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    try {
                        //UUID id = UUID.fromString(file.getName());
                        long openTimeEpoch = Long.parseLong(Files.readAllLines(file.toPath().resolve("Timestamp.txt")).get(0));
                        String title = Files.readAllLines(file.toPath().resolve("Title.txt")).get(0);

                        View newCard = View.inflate(ctx, R.layout.caps_view, null);
                        ((TextView) newCard.findViewById(R.id.capsNameLabel)).setText(title);

                        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
                        String openDateString = formatter.format(new Date(openTimeEpoch));
                        ((TextView) newCard.findViewById(R.id.openDateLabel)).setText("Opens on " + openDateString);
                        String createdTimeStr = formatter.format(new Date(createTimeEpoch));
                        ((TextView) newCard.findViewById(R.id.createdDateLabel)).setText("Sealed on " + createdTimeStr);


                        ZoneId systemZone = ZoneId.systemDefault();
                        ZonedDateTime currentTime = ZonedDateTime.now(systemZone);
                        //    ZonedDateTime openTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(openTimeEpoch), systemZone);

                        // Log.e("time","Current: "+currentTime+" target: "+openTimeEpoch);
                        // Log.e("time", "Current: " +  + " target: " + openTimeEpoch);

                        if (currentTime.toInstant().toEpochMilli() > openTimeEpoch) {
                            Button openBtn = newCard.findViewById(R.id.openCapBtn);
                            openBtn.setEnabled(true);
                            openBtn.setOnClickListener(v -> {
                                Snackbar.make(getWindow().getDecorView().getRootView(), "Unpacking... Sit tight!", 5000).show();

                            });
                        }
                        layout.addView(newCard);

                    } catch (Exception e) {
                        //:(
                    }
                }
            }
        }
    }
}