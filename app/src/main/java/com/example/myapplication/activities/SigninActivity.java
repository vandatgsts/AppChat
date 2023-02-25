package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivitySigninBinding;
import com.example.myapplication.utlities.Constants;
import com.example.myapplication.utlities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


public class SigninActivity extends AppCompatActivity {

    private ActivitySigninBinding binding;
    private PreferenceManager preferenceManager;

    private FirebaseAuth mAuth=FirebaseAuth.getInstance();;

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager =new PreferenceManager(getApplicationContext());
        binding =ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListenters();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setListenters()
    {
        binding.textCreatNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SingnUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v->{
            if(isValidSignInDetails()) {
                signIn();
            }});

    }

    private void signIn() {
        loading(true);
        mAuth.signInWithEmailAndPassword(binding.inputEmail.getText().toString(),binding.inputPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful() && task.getResult()!=null){
                            Log.d("SignIn", "signInWithEmail:success");
                            FirebaseUser user=mAuth.getCurrentUser();
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                            preferenceManager.putString(Constants.KEY_USER_ID,user.getUid().toString());
                            preferenceManager.putString(Constants.KEY_NAME,user.getDisplayName());
//                            preferenceManager.putString(Constants.KEY_IMAGE,user.getPhotoUrl().toString());
                            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                    }
                        else{
                            Log.d("SignIn", "signInWithEmail:Failue");
                            //Log.d("log1",task.getException().getMessage());
                            loading(false);
                            showToast("Unable to sign in");
                        }
                }

                });

    }
    private void loading(Boolean isLoading)
    {
        if(isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    //    private void addDataToFirestore(){
//        FirebaseFirestore database= FirebaseFirestore.getInstance();
//        HashMap<String,Object> data=new HashMap<>();
//        data.put("first_name","Van");
//        data.put("last_name","Dat");
//        database.collection("users")
//                .add(data)
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//    }
    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            showToast("Invalid Email");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter Password");
            return false;
        } else
            return true;
    }
}