package com.example.mailyapp.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mailyapp.R;
import com.example.mailyapp.adapters.MailAdapter;
import com.example.mailyapp.models.Mail;
import com.example.mailyapp.viewmodels.MailViewModel;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class InboxActivity extends AppCompatActivity implements MailAdapter.OnMailClickListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView mailRecyclerView;
    private MailAdapter mailAdapter;
    private SearchView searchView;
    private MailViewModel mailViewModel;
    private String currentFolder = "inbox";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        // Setup Toolbar (upperBar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup Drawer (sideBar)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            String folder = null;
            if (id == R.id.nav_inbox) folder = "inbox";
            else if (id == R.id.nav_sent) folder = "sent";
            else if (id == R.id.nav_starred) folder = "starred";
            else if (id == R.id.nav_drafts) folder = "drafts";
            else if (id == R.id.nav_spam) folder = "spam";
            else if (id == R.id.nav_trash) folder = "trash";

            if (folder != null) {
                currentFolder = folder;
                mailViewModel.fetchFolder(folder, 1);
                getSupportActionBar().setTitle(capitalize(folder));
                item.setChecked(true);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // RecyclerView setup
        mailRecyclerView = findViewById(R.id.mailRecyclerView);
        mailRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Temporary hardcoded session
        getSharedPreferences("session", MODE_PRIVATE)
                .edit()
                .putString("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI2ODU5MmE0NmRhZTUzNDJiYzlkM2Q1ZmIiLCJpYXQiOjE3NTA3NjQ0MTcsImV4cCI6MTc1MDc3MTYxN30.deTSu5pN2Js6D0REzMm1BmCB_sLoj1fsbMjpoGAIeCU")
                .putString("user_id", "68592a46dae5342bc9d3d5fb")
                .apply();

        // ViewModel setup
        mailViewModel = new ViewModelProvider(this).get(MailViewModel.class);
        mailViewModel.fetchFolder(currentFolder, 1);
        mailViewModel.getRemoteMails().observe(this, mails -> {
            Log.d("InboxActivity", "🚨 Received " + (mails != null ? mails.size() : "null") + " mails");
            if (mailAdapter == null) {
                mailAdapter = new MailAdapter(mails, this);
                mailRecyclerView.setAdapter(mailAdapter);
            } else {
                mailAdapter.updateData(mails);
            }
        });

        // SearchView setup
        searchView = toolbar.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.setQueryHint("Search mail");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onMailClick(Mail mail) {
        // TODO: open MailViewActivity later
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
