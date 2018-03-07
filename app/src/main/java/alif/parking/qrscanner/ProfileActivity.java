package alif.parking.qrscanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import alif.parking.qrscanner.models.Users;

public class ProfileActivity extends AppCompatActivity {

    private EditText platnumb;
    private Button saveprofile;
    private FirebaseDatabase mDatabase;
    DatabaseReference mPlatRef;
    DatabaseReference mRef;
    Users userPojo;
    private FirebaseUser currentUser;
    private EditText userName;
    private EditText userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userPojo = new Users();

        userName = (EditText)findViewById(R.id.et_userName);
        userEmail = (EditText)findViewById(R.id.et_email);
        platnumb = (EditText)findViewById(R.id.et_platnumb);
        saveprofile = (Button)findViewById(R.id.b_saveprofile);
        platnumb.setText(userPojo.getPlatNumb());

        //dapetin datanya dari main
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("users").child(currentUser.getEmail().replace(".", ","));//childnya si userEmail berikut
        mPlatRef = mRef.child("plat_numb");//childnya si userEmail berikut

        userEmail.setEnabled(false);

        mPlatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    platnumb.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRef.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRef.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userEmail.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        saveprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plat_numb = platnumb.getText().toString();
                String namauser = userName.getText().toString();
                mRef.child("user").setValue(namauser);
                mPlatRef.setValue(plat_numb);
                Toast.makeText(ProfileActivity.this, "Data telah tersimpan", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
