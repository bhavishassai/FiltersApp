package ai.deepar.deepar_example;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FilterPreview extends AppCompatActivity {

    private ImageView previewImage;
    private Button postButton,discardButton;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    String[] downloadUrl = {""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_preview);
        previewImage  = findViewById(R.id.previewImage);
        postButton = findViewById(R.id.postButton);
        discardButton = findViewById(R.id.discardButton);

        File imageFile = MainActivity.getImageFile();
        Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        previewImage.setImageBitmap(imageBitmap);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(this);
        alert.setMessage("Caption");
        alert.setTitle("Specify your caption ");
        alert.setView(edittext);

        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if(edittext.getParent()!=null)
                    ((ViewGroup)edittext.getParent()).removeView(edittext);

                Toast.makeText(FilterPreview.this,"Uploading...",Toast.LENGTH_SHORT).show();


                DocumentReference docRef =  db.collection("feedPosts").document();
                StorageReference storageRef = storage.getReference("/feedImages/"+ docRef.getId()+".png");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = storageRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                        Toast.makeText(FilterPreview.this,exception.toString(),Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl[0] = uri.toString();
                                HashMap<String,String> data = new HashMap<String,String>();
                                data.put("uid", FirebaseAuth.getInstance().getUid());
                                data.put("url", downloadUrl[0]);
                                data.put("caption", edittext.getText().toString());
                                docRef.set(data);
                                startActivity(new Intent(FilterPreview.this,Home.class));
                            }
                        });

                    }
                });
            }
        });

        alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(edittext.getParent()!=null)
                    ((ViewGroup)edittext.getParent()).removeView(edittext);

               }
        });
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        final DocumentReference[] docRef = new DocumentReference[1];
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.show();
            }

        });

    }

}