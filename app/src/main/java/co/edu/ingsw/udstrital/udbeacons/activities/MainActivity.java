package co.edu.ingsw.udstrital.udbeacons.activities;

import android.content.Intent;
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
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import co.edu.ingsw.udstrital.udbeacons.fragments.HomeFragment;
import co.edu.ingsw.udstrital.udbeacons.R;
import co.edu.ingsw.udstrital.udbeacons.fragments.WebFragment;
import utils.CustomItemMenu;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WebFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener{

    private TextView navUsername;
    private TextView navEmail;
    private TextView navRole;
    private String menu;
    private ArrayList<CustomItemMenu> mainMenuActions;
    private ArrayList<CustomItemMenu> socialMenuActions;
    private String urlFacebook;
    private String urlTwitter;
    private String urlLinkedin;

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

        menu = getIntent().getStringExtra("user_menu");
        createDynamicMenu(navigationView);

        Fragment myFragment = null;
        myFragment = HomeFragment.newInstance(null,null);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.mainLayout, myFragment, myFragment.getTag()).commit();
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

    private void createDynamicMenu(NavigationView navigationView){
        Menu menu = navigationView.getMenu();
        JSONArray jsonMenu = null;
        try {
            jsonMenu = new JSONArray(this.menu);
            if(jsonMenu != null){
                mainMenuActions = new ArrayList<CustomItemMenu>();
                socialMenuActions = new ArrayList<CustomItemMenu>();
                for(int i = 0; i < jsonMenu.length(); i++){
                    JSONObject objItem = (JSONObject)jsonMenu.get(i);
                    if(objItem.getString("section").equals("main")){
                        mainMenuActions.add(new CustomItemMenu(objItem.getInt("order"),objItem.getString("name")
                                ,objItem.getString("url"),objItem.getString("activity")));
                    }else if(objItem.getString("section").equals("social")){
                        socialMenuActions.add(new CustomItemMenu(objItem.getInt("order"),objItem.getString("name")
                                ,objItem.getString("url"),objItem.getString("activity")));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Collections.sort(mainMenuActions);
        Collections.sort(socialMenuActions);

        //Fill main menu
        MenuItem menuItem= menu.getItem(0) ;
        SubMenu subMenu = menuItem.getSubMenu();
        for(CustomItemMenu item: mainMenuActions){
            if(item.getName().equals(getString(R.string.schedule))){
                subMenu.add(R.id.mainMenu,item.getOrder(), Menu.NONE, item.getName()).setIcon(R.drawable.ic_date_range_black_24dp);
            }else if(item.getName().equals(getString(R.string.condor))){
                subMenu.add(R.id.mainMenu,item.getOrder(), Menu.NONE, item.getName()).setIcon(R.drawable.ic_perm_identity_black_24dp);
            }else if(item.getName().equals(getString(R.string.directory))){
                subMenu.add(R.id.mainMenu,item.getOrder(), Menu.NONE, item.getName()).setIcon(R.drawable.ic_import_contacts_black_24dp);
            }else if(item.getName().equals(getString(R.string.uWebsite))){
                subMenu.add(R.id.mainMenu,item.getOrder(), Menu.NONE, item.getName()).setIcon(R.drawable.ic_domain_black_24dp);
            }else {
                subMenu.add(R.id.mainMenu,item.getOrder(), Menu.NONE, item.getName()).setIcon(R.drawable.ic_star_border_black_24dp);
            }
        }

        //Update social menu
        for(CustomItemMenu item: socialMenuActions){
            if(item.getName().equals(getString(R.string.fb_id))){
                menu.findItem(R.id.nav_fb).setVisible(true);
                if(item.getUrl() != null && !item.getUrl().isEmpty())
                    urlFacebook = item.getUrl();
            }else if(item.getName().equals(getString(R.string.twitter_id))){
                menu.findItem(R.id.nav_twitter).setVisible(true);
                if(item.getUrl() != null && !item.getUrl().isEmpty())
                    urlTwitter = item.getUrl();
            }else if(item.getName().equals(getString(R.string.ldn_id))){
                menu.findItem(R.id.nav_ldin).setVisible(true);
                if(item.getUrl() != null && !item.getUrl().isEmpty())
                    urlLinkedin = item.getUrl();
            }
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
            myFragment = HomeFragment.newInstance(null,null);
        } else if(isUrlFromMainMenuItem(item.getTitle().toString())){
            myFragment = WebFragment.newInstance(getUrlFromMainMenuItem(item.getTitle().toString()));
        } else if(isActivityFromMainMenuItem(item.getTitle().toString())){
            String activityToStart = "co.edu.ingsw.udstrital.udbeacons.activities."+getActivityFromMainMenuItem(item.getTitle().toString());
            try {
                Class<?> c = Class.forName(activityToStart);
                Intent menuItemIntent = new Intent(this, c);
                startActivity(menuItemIntent);
            } catch (ClassNotFoundException ignored) {
            }

        } else if (id == R.id.nav_ldin) {
            if(urlLinkedin != null){
                myFragment = WebFragment.newInstance(urlLinkedin);
            }else{
                //Actvity
            }
        } else if (id == R.id.nav_fb) {
            if(urlFacebook != null){
                myFragment = WebFragment.newInstance(urlFacebook);
            }else{
                //Actvity
            }
        } else if (id == R.id.nav_twitter) {
            if(urlTwitter != null){
                myFragment = WebFragment.newInstance(urlTwitter);
            }else{
                //Actvity
            }
        } else if (id == R.id.nav_exit){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        if(myFragment != null){
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.mainLayout, myFragment, myFragment.getTag()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * This method return the url to redirect defined for an item
     * @param name is the title of item to search
     * @return The url to redirect defined from menu retrieved by login service. If the name of item
     * does not exists in menu, return null
     */
    private String getUrlFromMainMenuItem(String name){
        for(CustomItemMenu item: mainMenuActions){
            if(item.getName().equals(name))
                return item.getUrl();
        }
        return null;
    }

    /**
     * This method return the activity defined for an item
     * @param name is the title of item to search
     * @return The activity name defined from menu retrieved by login service. If the name of item
     * does not exists in menu, return null
     */
    private String getActivityFromMainMenuItem(String name){
        for(CustomItemMenu item: mainMenuActions){
            if(item.getName().equals(name))
                return item.getActivity();
        }
        return null;
    }

    /**
     * Allow to knmow if an item has defined an url to redirect
     * @param name is the title of the item to search
     * @return True if the item has an url defined, otherwise return false
     */
    private boolean isUrlFromMainMenuItem(String name){
        for(CustomItemMenu item: mainMenuActions){
            if(item.getName().equals(name))
                return item.getUrl() != null && !item.getUrl().isEmpty();
        }
        return false;
    }

    /**
     * Allow to knmow if an item has defined an activity
     * @param name is the title of the item to search
     * @return True if the item has an activity defined, otherwise return false
     */
    private boolean isActivityFromMainMenuItem(String name){
        for(CustomItemMenu item: mainMenuActions){
            if(item.getName().equals(name))
                return item.getActivity() != null && !item.getActivity().isEmpty();
        }
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

