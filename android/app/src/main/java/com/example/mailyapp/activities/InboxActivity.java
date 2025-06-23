package com.example.mailyapp.activities;

import android.os.Bundle;

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

            if (id == R.id.nav_inbox) {
                // TODO: show inbox
            } else if (id == R.id.nav_starred) {
                // TODO: show starred
            } else if (id == R.id.nav_sent) {
                // TODO: show sent
            } else if (id == R.id.nav_drafts) {
                // TODO: show drafts
            } else if (id == R.id.nav_spam) {
                // TODO: show spam
            } else if (id == R.id.nav_trash) {
                // TODO: show trash
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
                .putString("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI2ODU5MmE0NmRhZTUzNDJiYzlkM2Q1ZmIiLCJpYXQiOjE3NTA2ODg0MzEsImV4cCI6MTc1MDY5NTYzMX0.80sHaEI19znzMTTbvHNRcFTPcWyZqPre5smTuA6BD7s")
                .putString("user_id", "68592a46dae5342bc9d3d5fb")
                .apply();

        // ViewModel setup
        mailViewModel = new ViewModelProvider(this).get(MailViewModel.class);
        mailViewModel.fetchRemoteInbox(1);
        mailViewModel.getRemoteInboxMails().observe(this, mails -> {
            if (mails != null) {
                mailAdapter = new MailAdapter(mails, this);
                mailRecyclerView.setAdapter(mailAdapter);
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
}
