package com.example.blip.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;


import com.example.blip.databinding.ActivityMainBinding;
import com.example.blip.databinding.ActivityRegisterBinding;
import com.example.blip.utilities.Constants;
import com.example.blip.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding registerBinding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(registerBinding.getRoot());
        SetListeners();
    }

    private void SetListeners() {

        registerBinding.loginText.setOnClickListener(v -> onBackPressed());
        registerBinding.registerBtn.setOnClickListener(v -> {
            if (isValidRegisterDetails()) {
                Register();
            }
        });
        registerBinding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

    }

    private String encodedImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            registerBinding.imageProfile.setImageBitmap(bitmap);
                            registerBinding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );


    private void ShowToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void Register() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, registerBinding.edtUserId.getText().toString());
        user.put(Constants.KEY_EMAIL, registerBinding.edtEmailId.getText().toString());
        user.put(Constants.KEY_PASSWORD, registerBinding.edtPasswd.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, registerBinding.edtUserId.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    ShowToast(exception.getMessage());
                });

    }

    private Boolean isValidRegisterDetails() {

        if (encodedImage == null) {
            ShowToast("Select profile image");
            return false;
        } else if (registerBinding.edtUserId.getText().toString().trim().isEmpty()) {
            ShowToast("Enter Name");
            return false;
        } else if (registerBinding.edtEmailId.getText().toString().trim().isEmpty()) {
            ShowToast("Enter Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(registerBinding.edtEmailId.getText().toString()).matches()) {
            ShowToast("Enter valid Email");
            return false;
        } else if (registerBinding.edtPasswd.getText().toString().trim().isEmpty()) {
            ShowToast("Enter password");
            return false;
        } else if (registerBinding.edtConfirmPasswd.getText().toString().trim().isEmpty()) {
            ShowToast("Confirm Password");
            return false;
        } else if (!registerBinding.edtPasswd.getText().toString().equals(registerBinding.edtConfirmPasswd.getText().toString())) {
            ShowToast("Password & Confirm Password must be same");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            registerBinding.registerBtn.setVisibility(View.INVISIBLE);
            registerBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            registerBinding.progressBar.setVisibility(View.INVISIBLE);
            registerBinding.registerBtn.setVisibility(View.VISIBLE);
        }
    }

}