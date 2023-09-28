package com.example.blip.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blip.R;
import com.example.blip.databinding.ActivityLoginBinding;
import com.example.blip.databinding.ActivityMainBinding;
import com.example.blip.utilities.Constants;
import com.example.blip.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;


public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding loginBinding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        loginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(loginBinding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        // Initialize animations
        android.view.animation.Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        android.view.animation.Animation bottom_down = AnimationUtils.loadAnimation(this, R.anim.bottom_down);

        // Set the bottom down animation on top layout
        loginBinding.topLinearLayout.setAnimation(bottom_down);
        loginBinding.registerLayout.setAnimation(bottom_down);

        // Create a handler for other layouts
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Set fade in animation on other layouts
                loginBinding.cardView.setAnimation(fade_in);
                loginBinding.cardView2.setAnimation(fade_in);
                loginBinding.cardView3.setAnimation(fade_in);
                loginBinding.cardView5.setAnimation(fade_in);
                loginBinding.textView.setAnimation(fade_in);
                loginBinding.textView2.setAnimation(fade_in);
                loginBinding.cardView4.setAnimation(fade_in);
                loginBinding.cardView6.setAnimation(fade_in);
            }
        };
        handler.postDelayed(runnable, 1000);
        SetListeners();

    }

    private void SetListeners() {
        loginBinding.registerLayout.setOnClickListener(view -> {
            // Start the new activity (replace YourNewActivity.class with the actual activity class)
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        });
//        loginBinding.loginBtn.setOnClickListener(v -> addDataToFireStore());

        loginBinding.loginBtn.setOnClickListener(v -> {
            if (isValidLoginDetails()) {
                Login();
            }
        });
    }

    private void Login() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,loginBinding.edtEmailId.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, loginBinding.edtPasswd.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                    && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent =  new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else {
                        loading(false);
                        showToast("unable to login");
                    }
                });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            loginBinding.loginBtn.setVisibility(View.INVISIBLE);
            loginBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            loginBinding.loginBtn.setVisibility(View.VISIBLE);
            loginBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidLoginDetails() {
        if (loginBinding.edtEmailId.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(loginBinding.edtEmailId.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (loginBinding.edtPasswd.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;

        } else {
            return true;
        }
    }
//    private void addDataToFireStore(){
//        FirebaseFirestore database =FirebaseFirestore.getInstance();
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("first name", "Christy");
//        data.put("last name", "Terence");
//        database.collection("users")
//                .add(data)
//                .addOnSuccessListener(documentReference ->{
//                    Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(exception ->{
//                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
}
