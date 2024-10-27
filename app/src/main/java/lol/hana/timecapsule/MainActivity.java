package lol.hana.timecapsule;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.color.DynamicColors;

import java.nio.file.Path;

public class MainActivity extends AppCompatActivity {
public static Path workDir;


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
        for(int i =0; i < 50; i++)
            layout.addView(View.inflate(this, R.layout.caps_view,null));

        Button createB = findViewById(R.id.createCapsuleBtn);
        createB.setOnClickListener(v -> {
            Intent i = new Intent(this, CreateCapsuleActivity.class);
            startActivity(i);
        });

        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        //getActionBar().hide();
        //WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }
}