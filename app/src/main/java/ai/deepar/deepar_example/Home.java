package ai.deepar.deepar_example;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.remote.WatchChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Home extends AppCompatActivity {

    private ImageButton filtersButton;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private ArrayList<FeedPost> feedPosts;
    FeedAdapter feedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        filtersButton = findViewById(R.id.filtersButton);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        filtersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this,MainActivity.class));
            }
        });
        feedPosts = new ArrayList<FeedPost>();
        db = FirebaseFirestore.getInstance();

        feedAdapter= new FeedAdapter(Home.this,feedPosts);
        fetchFeedPosts();

        recyclerView.setAdapter(feedAdapter);
    }

    private void fetchFeedPosts(){
        db.collection("feedPosts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    Toast.makeText(Home.this,"An error occurred while fetching data",Toast.LENGTH_SHORT).show();

                }

                for (DocumentChange dc:value.getDocumentChanges()){
                    if(dc.getType()== DocumentChange.Type.ADDED){
                        Toast.makeText(Home.this, dc.getDocument().getData().toString(), Toast.LENGTH_SHORT).show();
                        String uid =(String) dc.getDocument().getData().get("uid");
                        db.collection("users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                              FilterUser  user =   documentSnapshot.toObject(FilterUser.class);
                              Map<String,Object> data = dc.getDocument().getData();
                              data.put("user",user);
                              Map<String,Object> newData = new HashMap<String,Object>();
                              newData.put("url",data.get("url"));
                              newData.put("caption",data.get("caption"));
                              newData.put("user",data.get("user"));

                                final ObjectMapper mapper = new ObjectMapper();
                                // jackson's objectmapper
                                final FeedPost post = mapper.convertValue(newData, FeedPost.class);
                            //    Toast.makeText(Home.this,post.caption,Toast.LENGTH_SHORT).show();
                                feedPosts.add(post);
                                feedAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Home.this,"Failed get user data",Toast.LENGTH_SHORT);
                            }
                        });



                    }
                }



            }
        });
    }
}