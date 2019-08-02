package framel.inc.travelmantics;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class FirebaseUtil {

    private static final int RC_SIGN_IN = 102;
    static FirebaseDatabase mFirebaseDatabase;
    static DatabaseReference mDatabaseReference;
    static FirebaseAuth mFirebaseAuth;
    static FirebaseStorage mFirebaseStorage;
    static StorageReference mStorageReference;
    static FirebaseAuth.AuthStateListener mAuthStateListener;
    static ArrayList<TravelDeal> mDeals;
    static ListActivity mCaller;
    static boolean isAdmin;
    private static FirebaseUtil mFirebaseUtil;

    private FirebaseUtil() {
    }


    static void openFbReference(final ListActivity callerActivity) {
        if (mFirebaseUtil == null) {
            mFirebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();
            mCaller = callerActivity;
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null) {
                        FirebaseUtil.signIn();
                    } else {
                        String userId = firebaseAuth.getUid();
                        checkAdmin(userId);
                    }
                    Toast.makeText(callerActivity.getBaseContext(), "Welcome Back", Toast.LENGTH_SHORT).show();

                }
            };

            connectStorage();

        }
        mDeals = new ArrayList<TravelDeal>();
        mDatabaseReference = mFirebaseDatabase.getReference().child("TravelDeals");
    }

    private static void checkAdmin(String userId) {

        FirebaseUtil.isAdmin = false;
        DatabaseReference reference = mFirebaseDatabase.getReference("administrators").child(userId);
        ChildEventListener eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                mCaller.showMenu();
                Toast.makeText(mCaller.getApplicationContext(), "You are an administrator", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        reference.addChildEventListener(eventListener);


    }

    private static void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());


        // Create and launch sign-in intent
        mCaller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    static void attachListener() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    static void detachListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    private static void connectStorage() {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("DealsPictures");

    }
}
