package alif.parking.qrscanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import alif.parking.qrscanner.models.Users;
import alif.parking.qrscanner.utils.Constants;

public class NavDrawerActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView mDisplayImageView;
    private TextView mNameTextView;
    private TextView mEmailTextView;
    boolean doubleBackToExitPressedOnce = false;

    Button scanbutt;
    TextView scanresult,tv_timer,tv_biaya;
    int CODE_CANCEL = 1;
    Handler handler = new Handler();
    long startTime = 0L, timeinMilliseconds=0L,timeSwapBuff=0L, updateTime=0L;

    int pressCounter = 0;

    int price = 3000;

    Runnable updateTimerThread = new Runnable() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void run() {
            timeinMilliseconds = SystemClock.uptimeMillis()-startTime;
            updateTime = timeSwapBuff+timeinMilliseconds;
            int secs = (int)(updateTime/1000);
            int mins = secs/60;
            int hours = mins/60;

            secs%=60;
            int milliseconds = (int)(updateTime%1000);
            tv_timer.setText(String.format("%2d",hours)+":"+String.format("%2d",mins)+":"+String.format("%2d",secs)+":"
                    +String.format("%3d",milliseconds));
            if (secs % 30 == 0){
                price++;
                tv_biaya.setText("Rp. "+price+",00");
            }
            handler.postDelayed(this,0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = navigationView.getHeaderView(0);

        mDisplayImageView = (ImageView) navHeaderView.findViewById(R.id.imageView_display);
        mNameTextView = (TextView) navHeaderView.findViewById(R.id.textView_name);
        mEmailTextView = (TextView) navHeaderView.findViewById(R.id.textView_email);

        FirebaseDatabase.getInstance().getReference(Constants.USER_KEY).child(mFirebaseUser.getEmail().replace(".", ","))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            Users users = dataSnapshot.getValue(Users.class);
                            Glide.with(NavDrawerActivity.this)
                                    .load(users.getPhotUrl())
                                    .into(mDisplayImageView);

                            mNameTextView.setText(users.getUser());
                            mEmailTextView.setText(users.getEmail());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        scanbutt = (Button)findViewById(R.id.scan_btn);
        scanresult = (TextView)findViewById(R.id.result_scan);
        tv_timer = (TextView)findViewById(R.id.tv_timer);
        tv_biaya = (TextView)findViewById(R.id.tv_biaya);
        tv_timer.setText("--:--:--:--");
        tv_biaya.setText("Rp.0000,00");


        final Activity activity = this;
        scanbutt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //counter
                pressCounter++;
                if(pressCounter%2==0){
                    onBayar();

                }else {

                    //start calculating the price and the time
                    IntentIntegrator integrator = new IntentIntegrator(activity);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                    integrator.setPrompt("Scan the barcode");
                    integrator.setCameraId(0);
                    integrator.setBeepEnabled(false);
                    integrator.setBarcodeImageEnabled(false);
                    integrator.initiateScan();

                    onParkir();

                    handler.postDelayed(updateTimerThread,0);
                }
            }
        });

    }

    public void onBayar(){
        //stop calculating the price and the time
        handler.postDelayed(updateTimerThread,0);
        scanresult.setText(tv_biaya.getText());
        scanbutt.setText("SCAN");

        tv_biaya.setVisibility(View.INVISIBLE);
        tv_timer.setVisibility(View.INVISIBLE);
    }

    public void onParkir(){

        scanbutt.setText("BAYAR");
        //timernya
        startTime = SystemClock.uptimeMillis();

        tv_biaya.setVisibility(View.VISIBLE);
        tv_timer.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
                CODE_CANCEL = 1;
                tv_biaya.setVisibility(View.INVISIBLE);
                tv_timer.setVisibility(View.INVISIBLE);
            }
            else {
                scanresult.setVisibility(View.VISIBLE);
                scanresult.setText(result.getContents());
                CODE_CANCEL = 0;
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    protected void signOut() {

        mAuth.signOut();

        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Tekan BACK sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;

                    finish();
                    System.exit(1);
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
            signOut();
            Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
