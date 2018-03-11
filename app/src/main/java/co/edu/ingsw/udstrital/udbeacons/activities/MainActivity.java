package co.edu.ingsw.udstrital.udbeacons.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import co.edu.ingsw.udstrital.udbeacons.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WebFragment.OnFragmentInteractionListener {

    private TextView navUsername;
    private TextView navEmail;
    private TextView navRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        navUsername = (TextView) headerView.findViewById(R.id.txtName);
        navEmail = (TextView) headerView.findViewById(R.id.txtMail);
        navRole = (TextView) headerView.findViewById(R.id.txtRole);

        navUsername.setText(getIntent().getStringExtra("user_name"));
        navEmail.setText(getIntent().getStringExtra("user_email"));
        navRole.setText(getIntent().getStringExtra("user_role"));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment myFragment = null;

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_schedulle) {

        } else if (id == R.id.nav_condor) {
            myFragment = WebFragment.newInstance("https://estudiantes.portaloas.udistrital.edu.co/appserv/");
        } else if (id == R.id.nav_directory) {
            myFragment = WebFragment.newInstance("https://www.udistrital.edu.co/directorio");
        } else if (id == R.id.nav_Uwebsite) {
            myFragment = WebFragment.newInstance("https://www.udistrital.edu.co");
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_fb) {

        } else if (id == R.id.nav_twitter) {

        }

        if(myFragment != null){
            FragmentManager manager = getSupportFragmentManager();manager.beginTransaction().replace(R.id.mainLayout, myFragment, myFragment.getTag()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

