package com.example.mailyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.mailyapp.R;
import com.example.mailyapp.adapters.MailAdapter;
import com.example.mailyapp.data.AppDatabase;
import com.example.mailyapp.data.LabelDao;
import com.example.mailyapp.entities.LabelEntity;
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
    private AppDatabase db;
    private LabelDao labelDao;
    private View currentSelectedNavItem;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                        "MailyDB").allowMainThreadQueries().build();
        labelDao = db.labelDao();
        /* -------------- for cleanup ---------------- */

        /*labelDao.deleteAll();*/

        /* -------------- for cleanup ---------------- */

        // Setup Toolbar (upperBar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide "MailyApp" title

        // Setup Drawer (sideBar)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        // Find the ListView from the navigation drawer
        ListView lvLabels = navigationView.findViewById(R.id.lv_labels);
        List<LabelEntity> labels = labelDao.index();

        ArrayAdapter<LabelEntity> adapter = new ArrayAdapter<LabelEntity>(
                this, R.layout.item_label, R.id.label_name, labels
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                ImageView icon = row.findViewById(R.id.label_icon);
                // Optional: Change icon or tint dynamically
                return row;
            }
        };

        lvLabels.setAdapter(adapter);
        lvLabels.setOnItemClickListener((parent, view, position, id) -> {
            markSelectedItem(view); // Now shared logic
            LabelEntity clickedLabel = (LabelEntity) parent.getItemAtPosition(position);
            // Handle label click (e.g., filter mail list)
            drawerLayout.closeDrawer(GravityCompat.START);
        });



        justifyListViewHeightBasedOnChildren(lvLabels);

        LinearLayout createLabelView = navigationView.findViewById(R.id.nav_create_label);
        if (createLabelView != null) {
            createLabelView.setOnClickListener(v -> {
                Intent intent = new Intent(InboxActivity.this, CreateLabelActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle menu item clicks
        // Set up manual click listeners for nav items
        findViewById(R.id.nav_inbox).setOnClickListener(v -> {
            // TODO: handle inbox selection
            LinearLayout navInbox = findViewById(R.id.nav_inbox);
            markSelectedItem(navInbox);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.nav_starred).setOnClickListener(v -> {
            // TODO: handle starred
            LinearLayout navStarred = findViewById(R.id.nav_starred);
            markSelectedItem(navStarred);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.nav_sent).setOnClickListener(v -> {
            // TODO: handle sent
            LinearLayout navSent = findViewById(R.id.nav_sent);
            markSelectedItem(navSent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.nav_drafts).setOnClickListener(v -> {
            // TODO: handle drafts
            LinearLayout navDrafts = findViewById(R.id.nav_drafts);
            markSelectedItem(navDrafts);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.nav_spam).setOnClickListener(v -> {
            // TODO: handle spam
            LinearLayout navSpam = findViewById(R.id.nav_spam);
            markSelectedItem(navSpam);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.nav_trash).setOnClickListener(v -> {
            // TODO: handle trash
            LinearLayout navTrash = findViewById(R.id.nav_trash);
            markSelectedItem(navTrash);
            drawerLayout.closeDrawer(GravityCompat.START);
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

    private void markSelectedItem(View selected) {
        if (currentSelectedNavItem != null) {
            currentSelectedNavItem.setSelected(false); // Unmark previous
        }
        selected.setSelected(true);
        currentSelectedNavItem = selected;
    }

    public static void justifyListViewHeightBasedOnChildren(ListView listView) {
        ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
        if (adapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


}
