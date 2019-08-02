package framel.inc.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealRecyclerAdapter extends RecyclerView.Adapter<DealRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    ArrayList<TravelDeal> mDeals;
    private LayoutInflater mLayoutInflater;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private ImageView imageDeal;

    DealRecyclerAdapter(Context context) {
        this.mContext = context;

        mLayoutInflater = LayoutInflater.from(mContext);

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        mDeals = FirebaseUtil.mDeals;
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                TravelDeal travelDeal = dataSnapshot.getValue(TravelDeal.class);

                if (travelDeal != null) {
                    travelDeal.setId(dataSnapshot.getKey());
                }
                mDeals.add(travelDeal);
                notifyItemInserted(mDeals.size() - 1);

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
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = mLayoutInflater.inflate(R.layout.deal_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        TravelDeal deal = mDeals.get(i);
        viewHolder.bind(deal);

    }

    @Override
    public int getItemCount() {
        return mDeals.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView titleTextView;
        TextView descriptionTextView;
        TextView priceTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            titleTextView = itemView.findViewById(R.id.text_view_title);
            descriptionTextView = itemView.findViewById(R.id.text_view_description);
            priceTextView = itemView.findViewById(R.id.text_view_price);
            imageDeal = itemView.findViewById(R.id.image_deal);
        }

        void bind(TravelDeal deal) {
            titleTextView.setText(deal.getTitle());
            descriptionTextView.setText(deal.getDescription());
            priceTextView.setText(deal.getPrice());
            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            TravelDeal selectedDeal = mDeals.get(pos);
            Intent intent = new Intent(mContext, DealActivity.class);
            intent.putExtra("Deal", selectedDeal);
            mContext.startActivity(intent);

        }

        private void showImage(String url) {
            if (url != null && url.isEmpty() == false) {

                Picasso.get().load(url).resize(160, 160).centerCrop().into(imageDeal);
            }
        }
    }
}
