package framel.inc.travelmantics;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class DealActivity extends AppCompatActivity {

    public static final int PICTURE_RESULT = 42;
    TravelDeal deal;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private ImageView mImageView;
    private Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        mTitleEditText = findViewById(R.id.edit_text_title);
        mDescriptionEditText = findViewById(R.id.edit_text_description);
        mPriceEditText = findViewById(R.id.edit_text_price);
        mImageView = findViewById(R.id.image_deal);

        uploadButton = findViewById(R.id.button_upload);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                        PICTURE_RESULT);

            }
        });

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");

        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;

        mTitleEditText.setText(deal.getTitle());
        mDescriptionEditText.setText(deal.getDescription());
        mPriceEditText.setText(deal.getPrice());
        showImage(deal.getImageUrl());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_menu) {
            saveDeal();
            Toast.makeText(this, "Deal saved", Toast.LENGTH_SHORT).show();
            clean();
            backToList();
            return true;
        } else if (item.getItemId() == R.id.delete_menu) {
            deleteDeal();
            Toast.makeText(this, "Deal deleted", Toast.LENGTH_SHORT).show();
            backToList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveDeal() {

        deal.setTitle(mTitleEditText.getText().toString());
        deal.setPrice(mPriceEditText.getText().toString());
        deal.setDescription(mDescriptionEditText.getText().toString());

        if (deal.getId() == null) {

            mDatabaseReference.push().setValue(deal);

        } else {

            mDatabaseReference.child(deal.getId()).setValue(deal);
        }

    }

    private void deleteDeal() {
        if (deal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
        if (deal.getImageUrl() != null && deal.getImageUrl().isEmpty() == false) {
            StorageReference picReference = FirebaseUtil.mFirebaseStorage.getReferenceFromUrl(deal.getImageUrl());
            picReference.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads

                    Log.d("Delete image", exception.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Log.d("Delete image", "Image successfully deleted");
                }
            });
        }
    }

    private void clean() {

        mTitleEditText.setText("");
        mPriceEditText.setText("");
        mDescriptionEditText.setText("");

    }

    private void backToList() {
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data != null ? data.getData() : null;

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                mImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            final StorageReference reference = FirebaseUtil.mStorageReference.child(imageUri.getLastPathSegment());

            reference.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String url = downloadUri.toString();
                        deal.setImageUrl(url);
                        showImage(url);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            });


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditText(true);
            uploadButton.setEnabled(true);

        } else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditText(false);
            uploadButton.setEnabled(false);

        }
        return true;
    }

    private void enableEditText(boolean isEnabled) {
        mTitleEditText.setEnabled(isEnabled);
        mDescriptionEditText.setEnabled(isEnabled);
        mPriceEditText.setEnabled(isEnabled);
    }

    private void showImage(String url) {
        if (url != null && url.isEmpty() == false) {
            Picasso.get().load(url).centerCrop().into(mImageView);
        }
    }
}
