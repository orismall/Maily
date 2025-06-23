package com.example.mailyapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mailyapp.R;
import com.example.mailyapp.adapters.MailAdapter;
import com.example.mailyapp.models.Mail;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InboxActivity extends AppCompatActivity implements MailAdapter.OnMailClickListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView mailRecyclerView;
    private MailAdapter mailAdapter;
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        // Setup Toolbar (upperBar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide "MailyApp" title

        // Setup Drawer (sideBar)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle menu item clicks
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


        // Mail list setup
        mailRecyclerView = findViewById(R.id.mailRecyclerView);
        mailRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Mail> dummyMails = getDummyMails();
        mailAdapter = new MailAdapter(dummyMails, this);
        mailRecyclerView.setAdapter(mailAdapter);

        // SearchView setup (already inside the Toolbar)
        searchView = toolbar.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.setQueryHint("Search mail");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // handle search submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // filter mails while typing
                return false;
            }
        });
        MaterialButton fabCompose = findViewById(R.id.fabCompose);
        fabCompose.setOnClickListener(v -> {
            Intent intent = new Intent(InboxActivity.this, ComposeMailActivity.class);
            startActivity(intent);
        });

    }

    private List<Mail> getDummyMails() {
        List<Mail> mails = new ArrayList<>();
        mails.add(new Mail(1, "alice@example.com", Arrays.asList("you@example.com"),
                "Meeting tomorrow", "Don't forget our meeting at 10 AM!", "2025-06-19", new ArrayList<>()));
        mails.add(new Mail(2, "bob@example.com", Arrays.asList("you@example.com"),
                "Party Invite", "You're invited to my birthday party this weekend!", "2025-06-18", new ArrayList<>()));
        mails.add(new Mail(3, "carol@example.com", Arrays.asList("you@example.com"),
                "Job Opportunity", "We loved your resume. Let's set up an interview.", "2025-06-17", new ArrayList<>()));
        return mails;
    }

    @Override
    public void onMailClick(Mail mail) {
        Intent intent = new Intent(InboxActivity.this, MailViewActivity.class);
        intent.putExtra("mail", mail);
        startActivity(intent);
    }


}
