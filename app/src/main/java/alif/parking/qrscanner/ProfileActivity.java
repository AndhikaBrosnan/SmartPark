package alif.parking.qrscanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import alif.parking.qrscanner.models.Users;

public class ProfileActivity extends AppCompatActivity {

    private EditText platnumb;
    private Button saveprofile;
    private FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    Users userPojo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        platnumb = (EditText)findViewById(R.id.et_platnumb);
        saveprofile = (Button)findViewById(R.id.b_saveprofile);

        userPojo = new Users();

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //dapetin datanya dari main
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("users").child(currentUser.getEmail().replace(".", ",")).child("plat_numb");//childnya si email berikut

        saveprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plat_numb = platnumb.getText().toString();
                mRef.setValue(plat_numb);
                Toast.makeText(ProfileActivity.this, "Data telah tersimpan", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
