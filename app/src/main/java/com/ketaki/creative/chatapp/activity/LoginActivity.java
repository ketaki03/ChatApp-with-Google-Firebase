package com.ketaki.creative.chatapp.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ketaki.creative.chatapp.R;
import com.ketaki.creative.chatapp.adapter.ChatRecylerViewAdapter;
import com.ketaki.creative.chatapp.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST_CODE = 0;
    String currentUser;
    TextView userDetails;
    private ChatRecylerViewAdapter chatRecylerViewAdapter;
    public static String TAG = LoginActivity.class.getSimpleName();
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        userDetails = (TextView) findViewById(R.id.user_details);

        //check for the internet connectivity
        if (isConnected()) {

            //set button listener
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText input = (EditText) findViewById(R.id.input);

                    if (!input.getText().toString().isEmpty()) {
                        ChatMessage chatMessage = new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

                        //push value to Firebase
                        FirebaseDatabase.getInstance().getReference("chat").push().setValue(chatMessage);
                        Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();

                        // Clear the input
                        input.setText("");
                    }
                }
            });

            // Start sign in/sign up activity
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),
                        SIGN_IN_REQUEST_CODE);
            } else {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    currentUser = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    userDetails.setText("Welcome " + currentUser);
                }
            }

        } else {
            Toast.makeText(this, "Internet is disconnected", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //set Firebase chat value listener
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setAdapter(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "OnCancelled called");
            }
        };
        FirebaseDatabase.getInstance().getReference("chat").addValueEventListener(valueEventListener);

    }

    @Override
    protected void onStop() {
        super.onStop();

        //remove listener
        FirebaseDatabase.getInstance().getReference("chat").removeEventListener(valueEventListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                //set current user after user sign up
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    currentUser = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    userDetails.setText("Welcome " + currentUser);
                }

                Toast.makeText(this, "Successfully signed in. Welcome " + currentUser, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "We couldn't sign you in. Please try again later.", Toast.LENGTH_LONG).show();

                // Close the app
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getApplicationContext(), "You have been signed out.", Toast.LENGTH_SHORT).show();

                    // Close activity
                    finish();
                }
            });
        }
        return true;
    }

    public void setAdapter(DataSnapshot dataSnapshot) {
        final RecyclerView chatListRecylerView = (RecyclerView) findViewById(R.id.list_of_messages);

        //set Linear layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        chatListRecylerView.setLayoutManager(layoutManager);

        //form a chatMessageList with all the chatMessages from Firebase db
        List<ChatMessage> chatMessageList = new ArrayList<>();
        Iterable<DataSnapshot> iterator = dataSnapshot.getChildren();
        ChatMessage chatMessage;
        for (DataSnapshot value : iterator) {
            chatMessage = value.getValue(ChatMessage.class);

            //set values here
            if (chatMessage != null) {
                chatMessageList.add(chatMessage);
            }

        }
        chatRecylerViewAdapter = new ChatRecylerViewAdapter(chatMessageList, getApplicationContext());
        chatListRecylerView.scrollToPosition(chatRecylerViewAdapter.getItemCount() - 1);
        chatListRecylerView.setAdapter(chatRecylerViewAdapter);

    }

    //get internet connectivity
    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}

