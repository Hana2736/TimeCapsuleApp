package lol.hana.timecapsule;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.color.DynamicColors;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OpenCapsuleActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle b = getIntent().getExtras();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.image_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewerPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView dateView = findViewById(R.id.dateLabel);
        dateView.setText("Sealed on "+b.getString("dateCreated"));

        TextView title = findViewById(R.id.imageTitleName);
        title.setText(b.getString("capsName"));

        String uID = b.getString("uuid");
        String story = "";
        try {
            story = new String(Files.readAllBytes(Paths.get(MainActivity.workDir+"/temp/Story.txt")));
        } catch (IOException e) {
            //womp womp
        }
        ((TextView)findViewById(R.id.textStory)).setText(story);


        ImageView photo = findViewById(R.id.photoContainer);

        int numPhotos = new File(MainActivity.workDir+"/temp").listFiles().length-2;
        final int[] curPhoto = {0};
        Button prevPhoto = findViewById(R.id.buttonPrevious);
        prevPhoto.setOnClickListener(v -> {
            curPhoto[0]--;
            if(curPhoto[0]<0){
                curPhoto[0]=numPhotos-1;
            }
            setPhoto(curPhoto[0],photo);
        });
        Button nextPhoto = findViewById(R.id.buttonNext);
        nextPhoto.setOnClickListener(v -> {
            curPhoto[0]++;
            if(curPhoto[0]>=numPhotos){
                curPhoto[0]=0;
            }
            setPhoto(curPhoto[0],photo);
        });
        setPhoto(0,photo);


        DynamicColors.applyToActivitiesIfAvailable(getApplication());
    }

    Uri setPhoto(int index, ImageView photo){
        Uri ret = Uri.parse(new File(MainActivity.workDir+"/temp/"+index).toURI().toString());
        photo.setImageURI(ret);
        return ret;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            EncryptionUtils.resetTempDir();
        } catch (Exception e) {
            //yknow, this really shouldnt be an issue, but just in case :)
        }
    }
}
