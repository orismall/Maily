package com.example.mailyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.lifecycle.ViewModelProvider;
import com.example.mailyapp.R;
import com.example.mailyapp.adapters.MailAdapter;
import com.example.mailyapp.data.AppDatabase;
import com.example.mailyapp.data.LabelDao;
import com.example.mailyapp.entities.LabelEntity;
import com.example.mailyapp.entities.MailEntity;
import com.example.mailyapp.models.Label;
import com.example.mailyapp.models.Mail;
import com.example.mailyapp.viewmodels.LabelViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.example.mailyapp.viewmodels.MailViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class InboxActivity extends AppCompatActivity implements MailAdapter.OnMailClickListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView mailRecyclerView;
    private MailAdapter mailAdapter;
    private SearchView searchView;
    private AppDatabase db;
    private View currentSelectedNavItem;
    private ArrayAdapter<LabelEntity> adapter;
    private ListView lvLabels;
    private LabelViewModel labelViewModel;
    List<LabelEntity> labels;
    private MailViewModel mailViewModel;
    private String currentFolder = "inbox";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                        "MailyDB").allowMainThreadQueries().build();

        // Setup Toolbar (upperBar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        labelViewModel = new ViewModelProvider(this).get(LabelViewModel.class);

        // Initially load labels from Room (no API call)
        labels = new ArrayList<>();

        labelViewModel.getAllLabels().observe(this, updatedLabels -> {
            labels.clear();
            labels.addAll(updatedLabels);
            adapter.notifyDataSetChanged();
            justifyListViewHeightBasedOnChildren(lvLabels);
        });

        // Setup Drawer (sideBar)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        // Find the ListView from the navigation drawer
        lvLabels = navigationView.findViewById(R.id.lv_labels);
        labels = new ArrayList<>();
        adapter = new ArrayAdapter<LabelEntity>(
                this, R.layout.item_label, R.id.label_name, labels
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);

                TextView labelText = row.findViewById(R.id.label_name);
                ImageView trashIcon = row.findViewById(R.id.label_trash_icon);
                ImageView editIcon = row.findViewById(R.id.label_edit_icon);
                LabelEntity label = getItem(position);

                if (label != null) {
                    labelText.setText(label.getName());

                    trashIcon.setOnClickListener(v -> {
                        new androidx.appcompat.app.AlertDialog.Builder(InboxActivity.this)
                                .setTitle("Delete Label")
                                .setMessage("Are you sure you want to delete this label?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    labelViewModel.deleteById(label.getId()); // 🔁 Triggers Room + LiveData update automatically

                                    justifyListViewHeightBasedOnChildren(lvLabels);
                                    Toast.makeText(InboxActivity.this, "Label deleted", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });

                    editIcon.setOnClickListener(v -> {
                        final android.widget.EditText input = new android.widget.EditText(InboxActivity.this);
                        input.setText(label.getName());
                        input.setSelection(label.getName().length());
                        new androidx.appcompat.app.AlertDialog.Builder(InboxActivity.this)
                                .setTitle("Edit Label")
                                .setView(input)
                                .setPositiveButton("Save", (dialog, which) -> {
                                    String newName = input.getText().toString().trim();
                                    if (!newName.isEmpty()) {
                                        label.setName(newName);

                                        Label updatedLabel = new Label(label.getName());
                                        updatedLabel.setId(label.getId());
                                        updatedLabel.setColor(label.getColor());
                                        updatedLabel.setMailIds(label.getMailIds());

                                        labelViewModel.updateLabel(label.getId(), updatedLabel, result -> {
                                            runOnUiThread(() -> {
                                                if (result != null) {
                                                    Toast.makeText(InboxActivity.this, "Label updated", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(InboxActivity.this, "Failed to update label", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        });

                                    } else {
                                        Toast.makeText(InboxActivity.this, "Label name can't be empty", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });


                }

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

        // create label
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
            currentFolder = "inbox";
            markSelectedItem(navInbox);
            drawerLayout.closeDrawer(GravityCompat.START);
            loadMailsForFolder(currentFolder);
        });

        findViewById(R.id.nav_starred).setOnClickListener(v -> {
            // TODO: handle starred
            LinearLayout navStarred = findViewById(R.id.nav_starred);
            currentFolder = "starred";
            markSelectedItem(navStarred);
            drawerLayout.closeDrawer(GravityCompat.START);
            loadMailsForFolder(currentFolder);
        });

        findViewById(R.id.nav_sent).setOnClickListener(v -> {
            // TODO: handle sent
            LinearLayout navSent = findViewById(R.id.nav_sent);
            currentFolder = "sent";
            markSelectedItem(navSent);
            drawerLayout.closeDrawer(GravityCompat.START);
            loadMailsForFolder(currentFolder);
        });

        findViewById(R.id.nav_drafts).setOnClickListener(v -> {
            // TODO: handle drafts
            LinearLayout navDrafts = findViewById(R.id.nav_drafts);
            currentFolder = "drafts";
            markSelectedItem(navDrafts);
            drawerLayout.closeDrawer(GravityCompat.START);
            loadMailsForFolder(currentFolder);
        });

        findViewById(R.id.nav_spam).setOnClickListener(v -> {
            // TODO: handle spam
            LinearLayout navSpam = findViewById(R.id.nav_spam);
            currentFolder = "spam";
            markSelectedItem(navSpam);
            drawerLayout.closeDrawer(GravityCompat.START);
            loadMailsForFolder(currentFolder);
        });

        findViewById(R.id.nav_trash).setOnClickListener(v -> {
            // TODO: handle trash
            LinearLayout navTrash = findViewById(R.id.nav_trash);
            currentFolder = "trash";
            markSelectedItem(navTrash);
            drawerLayout.closeDrawer(GravityCompat.START);
            loadMailsForFolder(currentFolder);
        });

        // Mail list setup
        mailRecyclerView = findViewById(R.id.mailRecyclerView);
        mailRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mailViewModel = new ViewModelProvider(this).get(MailViewModel.class);
        mailViewModel.fetchFolder(currentFolder, 1);
        mailViewModel.getLocalMailsByFolder(currentFolder).observe(this, entities -> {
            if (entities == null) return;

            List<Mail> mails = new ArrayList<>();
            for (MailEntity entity : entities) {
                mails.add(entity.toModel());
            }
            if (mailAdapter == null) {
                mailAdapter = new MailAdapter(mails, this);
                mailRecyclerView.setAdapter(mailAdapter);
            } else {
                mailAdapter.updateData(mails);
            }
        });

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            mailViewModel.refreshAllMails(() -> {
                runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
            });

            labelViewModel.refreshFromApi(); // This already works fine
        });



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
    @Override
    public void onMailClick(Mail mail) {
        Intent intent = new Intent(InboxActivity.this, MailViewActivity.class);
        intent.putExtra("mail", mail);

        startActivity(intent);
    }

    @Override
    public void onToggleStar(String mailId, boolean isStarred) {
        mailViewModel.updateStarredFlag(mailId, isStarred);
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

    private void loadMailsForFolder(String folderName) {
        mailViewModel.getLocalMailsByFolder(folderName).observe(this, entities -> {
            if (entities == null) return;

            List<Mail> mails = new ArrayList<>();
            for (MailEntity entity : entities) {
                mails.add(entity.toModel());
            }

            if (mailAdapter == null) {
                mailAdapter = new MailAdapter(mails, this);
                mailRecyclerView.setAdapter(mailAdapter);
            } else {
                mailAdapter.updateData(mails);
            }
        });
    }
}
