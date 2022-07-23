package ai.deepar.deepar_example;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class ProfileSetup extends AppCompatActivity {

    private Button btnSelect, btnUpload;
    private ImageView imageView;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;
    FirebaseStorage storage;
    StorageReference storageReference;
    private EditText userNameView;
    private ProgressBar progressBar;


    private FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
    private FirebaseAuth auth  = FirebaseAuth.getInstance();

    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);


        btnSelect = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        imageView = findViewById(R.id.imgView);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        userNameView = findViewById(R.id.userName);
        layout = findViewById(R.id.profileSetupLayout);
        layout.setVisibility(View.GONE);
        progressBar = findViewById(R.id.loadingBar);


        progressBar.setVisibility(View.VISIBLE);

        checkIfUserCompletedProfile();


        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SelectImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(userNameView.getText().toString()==""||userNameView.getText().toString().length()<5){
                    Toast.makeText(ProfileSetup.this,"UserName should be greater than 4 characters",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(ProfileSetup.this,"Upload process started",Toast.LENGTH_SHORT).show();
                    uploadImage();
                }

            }
        });



    }

    private void checkIfUserCompletedProfile(){
        firestoreDb.collection("users").document(auth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (documentSnapshot.exists()){
                    Intent i = new Intent(ProfileSetup.this,Home.class);
                    startActivity(i);
                }else{
                    Toast.makeText(ProfileSetup.this,"User dosnt esxist ! Create account",Toast.LENGTH_SHORT).show();
                    layout.setVisibility(View.VISIBLE);
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileSetup.this,"Failure",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void SelectImage()
    {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                imageView.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage()
    {
        if (filePath != null) {

            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(ProfileSetup.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    Task<Uri> downloadTask = taskSnapshot.getStorage().getDownloadUrl();



                                    downloadTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Toast.makeText(ProfileSetup.this,"Sucessfully uploaded",Toast.LENGTH_LONG).show();
                                            updateUserInfo(uri.toString(),userNameView.getText().toString());
                                        }
                                    });

                                   }


                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                            progressDialog.dismiss();
                            Toast
                                    .makeText(ProfileSetup.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int)progress + "%");


                                }
                            });
        }
    }


    private void updateUserInfo(String profileUrl , String userName){
        firestoreDb.collection("users").document(auth.getCurrentUser().getUid()).set(
                new FilterUser(userName,profileUrl)
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Intent i = new Intent(ProfileSetup.this,Home.class);
                startActivity(i);
            }
        });
    }
}