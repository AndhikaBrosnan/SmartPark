package alif.parking.qrscanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import alif.parking.qrscanner.models.Users;
import alif.parking.qrscanner.utils.Constants;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView mDisplayImageView;
    private TextView mNameTextView;
    private TextView mEmailTextView;
    boolean doubleBackToExitPressedOnce = false;

    Button scanbutt;
    TextView scanresult,tv_timer,tv_biaya;
    int CODE_CANCEL = 1;
    Handler handler = new Handler();
    long startTime = 0L, timeinMilliseconds=0L, timeSwapBuff=0L, updateTime=0L;

    int pressCounter = 0;

    int price= 0;

    int secs = 0;
    int priceTrigg = 0;
    int mins = 0;
    int hours = 0;

    private Calendar calendar;
    private Date date;
    private DateFormat df;

    Runnable updateTimerThread = new Runnable() {
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void run() {
            timeinMilliseconds = SystemClock.uptimeMillis() - startTime;
            updateTime = timeSwapBuff+timeinMilliseconds;
            secs = (int)(updateTime/1000);
            priceTrigg = (int)(updateTime/1000);
            mins = secs/60;
            hours = mins/60;

            secs%=60;
            int milliseconds = (int)(updateTime%1000);
            tv_timer.setText(String.format("%2d",hours)+":"+String.format("%2d",mins)+":"+String.format("%2d",secs)+":"+String.format("%3d",milliseconds));

            handler.postDelayed(this,0);
        }
    };

    Runnable updatePriceThread = new Runnable() {
        @Override
        public void run() {
            if (priceTrigg % 120 == 0){ // Price trigger berdasarkan detik.
                price++;
                tv_biaya.setText("Rp. "+price+",00");
            }
            handler.postDelayed(this,0);
        }
    };
    private String currentDateFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        calendar = Calendar.getInstance(TimeZone.getDefault());
        date = calendar.getTime();
        df = new SimpleDateFormat("EEEE, d MMMM yyyy HH:mm:ss");

        currentDateFormat = df.format(date);
        Log.d("DATE FORMAT: ", currentDateFormat);

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
                            Glide.with(MainActivity.this)
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
                    handler.postDelayed(updatePriceThread,0);
                }
            }
        });

    }

    public void onBayar(){

        scanresult.setText("Silahkan bayar biaya parkir sejumlah "+tv_biaya.getText() + " di kasir.");
        scanbutt.setText("SCAN");

        tv_biaya.setVisibility(View.INVISIBLE);
        tv_timer.setVisibility(View.INVISIBLE);
    }

    public void onParkir(){
        secs = 0;
        mins = 0;
        hours = 0;
        startTime = 0L;
        timeinMilliseconds = 0L;
        timeSwapBuff = 0L;
        updateTime = 0L;
        price = 0;

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
//                pressCounter++;
                tv_biaya.setVisibility(View.INVISIBLE);
                tv_timer.setVisibility(View.INVISIBLE);
            }
            else {
                scanresult.setVisibility(View.VISIBLE);
                scanresult.setText("Halo "+mFirebaseUser.getDisplayName()+", anda parkir pada tanggal: "+ currentDateFormat + " \n\n "+result.getContents()); //ambil get yang perlu dari gate
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
                        Toast.makeText(MainActivity.this, "Anda telah logout", Toast.LENGTH_SHORT).show();
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
                    System.exit(0);
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

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
            startActivity(intent);
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
