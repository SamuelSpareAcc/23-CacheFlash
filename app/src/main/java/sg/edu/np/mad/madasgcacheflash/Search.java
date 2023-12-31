package sg.edu.np.mad.madasgcacheflash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Search extends AppCompatActivity {
    private String username;
    private SearchView searchView;
    private SearchAdapter cuAdapter;
    ArrayList<Flashcard> flashcardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Intent intent = getIntent();

        username = intent.getStringExtra("Username"); //get username
        RecyclerView recyclerView = findViewById(R.id.searchRecyclerView);
        cuAdapter = new SearchAdapter(flashcardList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cuAdapter);

        //On click listener for the recycler view
        //________________________________________________________________
        cuAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(Flashcard flashcard){
                Log.v("hi", flashcard.getTitle());
                showAlert("Profile", flashcard);
            }
        });

        queryFlashcardsByCategory(username, cuAdapter); // Pass the adapter as a parameter

        //SearchView
        //____________________________________________________
        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.search);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item != null) {
                    int id = item.getItemId();

                    if (id == R.id.dashboard) {
                        Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                        intent.putExtra("Username", username); // Replace 'username' with your actual variable name
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    } else if (id == R.id.search) {
                        return true;
                    } else if (id == R.id.home) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("Username", username); // Replace 'username' with your actual variable name
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;

                    } else if (id == R.id.leaderboard) {
                        Intent intent = new Intent(getApplicationContext(), Leaderboard.class);
                        intent.putExtra("Username", username); // Replace 'username' with your actual variable name
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    } else if (id == R.id.about) {
                        Intent intent = new Intent(getApplicationContext(), Profile.class);
                        intent.putExtra("Username", username); // Replace 'username' with your actual variable name
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void queryFlashcardsByCategory(String username, SearchAdapter adapter) {
        //String capitalizedUsername = username.substring(0, 1).toUpperCase() + username.substring(1);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = usersRef.child(username).child("categories");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot flashcardSnapshot : categorySnapshot.getChildren()) {
                        Flashcard flashcard = flashcardSnapshot.getValue(Flashcard.class);
                        Log.v("Search", flashcard.getTitle());
                        adapter.addFlashcard(flashcard); // Update the adapter directly
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur
            }
        });
    }

    private void filterList(String text){
        ArrayList<Flashcard> filteredList = new ArrayList<>();
        for (Flashcard f: flashcardList){
            if (f.getTitle().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(f);
            }
            cuAdapter.setFilteredList(filteredList);

        }
    }

    public void showAlert(String title, Flashcard f) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage("Learn Yourself (no points)\nTest Yourself (Earn points)")
                .setPositiveButton("Test", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do something when the "OK" button is clicked
                        Intent intent = new Intent(Search.this, DifficultyLevelActivity.class);
                        intent.putExtra("Username", username);
                        intent.putExtra("flashcard", f);
                        startActivityForResult(intent, 1);
                    }
                })
                .setNegativeButton("Learn", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Search.this, LearnYourself.class);
                        intent.putExtra("Username", username);
                        intent.putExtra("flashcard", f);
                        startActivity(intent);
                    }
                })
                .show();
    }

    //When search class passes an intent to difficulty level activity class and comes back, this
    //part handles it.
    //____________________________________________________________________________________________
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // The result is from the DifficultyLevelActivity
                // Handle the result data here
                int difficultyLevel = data.getIntExtra("difficultyLevel", 0);
                int timerDuration = data.getIntExtra("timerDuration", -1);
                Flashcard flashcard = data.getParcelableExtra("flashcard");

                if (flashcard != null) {
                    Log.d("MainActivity", "Receiveds Flashcard: " + flashcard.getTitle());
                    // Now start the Testyourself activity with the selected difficulty level and timer duration
                    Intent intent = new Intent(Search.this, Testyourself.class);
                    intent.putExtra("flashcard", flashcard);
                    intent.putExtra("difficultyLevel", difficultyLevel);
                    intent.putExtra("timerDuration", timerDuration);
                    intent.putExtra("username", username);
                    startActivity(intent);

                    // Now you can use the data as needed or perform any actions based on the result
                    // For example, you can show a Toast message or update the UI with the selected data.
                    Toast.makeText(this, "Selected difficulty level: " + difficultyLevel, Toast.LENGTH_SHORT).show();
                } else {
                    // Handle the case where the result is not OK (e.g., user canceled the activity)
                    // For example, you can show a message or take appropriate actions.
                    Toast.makeText(this, "Activity result not OK", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}