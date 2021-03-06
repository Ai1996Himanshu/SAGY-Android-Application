package dynamicdrillers.sagy;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {
    CustomSwipeAdapter customSwipeAdapter;
    ViewPager viewPager;
    RecyclerView rv=null,mp=null;
    LinearLayout sliderpanel;
    private int dotscount;
    private ImageView[] dots;
    private DatabaseReference mDatabase,mpdatabase;
    private FirebaseAuth mAuth,mpAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        viewPager = view.findViewById(R.id.viewpager);
        sliderpanel =view.findViewById(R.id.sliderdots);

        customSwipeAdapter = new CustomSwipeAdapter(this.getActivity());
        viewPager.setAdapter(customSwipeAdapter);
        dotscount = customSwipeAdapter.getCount();
        dots = new ImageView[dotscount];

        for (int i=0;i<dotscount;i++){
            dots[i]= new ImageView(this.getActivity());
            dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.nonactive_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8,0,8,0);
            sliderpanel.addView(dots[i],params);
        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i=0;i<dotscount;i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.nonactive_dot));

                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.active_dot));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DashboardFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(viewPager.getCurrentItem()==0){
                            viewPager.setCurrentItem(1);
                        }
                        else if (viewPager.getCurrentItem()==1){
                            viewPager.setCurrentItem(2);
                        }
                        else if(viewPager.getCurrentItem()==2) {
                            viewPager.setCurrentItem(3);
                        }
                        else if (viewPager.getCurrentItem()==3){
                            viewPager.setCurrentItem(4);
                        }
                        else {
                            viewPager.setCurrentItem(0);
                        }
                    }
                });
            }
        }, 200, 4000);

        rv = (RecyclerView) view.findViewById(R.id.rv_recycler_view);
//        rv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rv.setLayoutManager(linearLayoutManager);


        mDatabase = FirebaseDatabase.getInstance().getReference().child("adopted_village");
        mAuth = FirebaseAuth.getInstance();

        //new recycler view

        mp = (RecyclerView)view.findViewById(R.id.mp_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        mp.setLayoutManager(llm);

        mpdatabase = FirebaseDatabase.getInstance().getReference().child("mp");


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        Query query = mDatabase;

        FirebaseRecyclerOptions<ModelVillage> options = new FirebaseRecyclerOptions.Builder<ModelVillage>()
                .setQuery(query,ModelVillage.class)
                .build();
        FirebaseRecyclerAdapter<ModelVillage,ModelVillageViewHolder>firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ModelVillage, ModelVillageViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ModelVillageViewHolder holder, int position, @NonNull ModelVillage model) {
                            holder.setVillage(model.getVillage());

                            FirebaseDatabase.getInstance().getReference().child("mp").child(model.getAdopted_by()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    holder.setConstituency("Constituency : "+dataSnapshot.child("constituency").getValue().toString());
                                    holder.setState(dataSnapshot.child("state").getValue(String.class));


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            final String name = model.getVillage().toString();
                            final  String adoptedby=model.getAdopted_by().toString();
                            final String villageid = getRef(position).getKey().toString();
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(getContext(),VillageInforamtionActivity.class);
                                    intent.putExtra("village",name);
                                    intent.putExtra("mp_id",adoptedby);
                                    intent.putExtra("village_id",villageid);
                                    startActivity(intent);
                                   // Toast.makeText(getContext(), villageid, Toast.LENGTH_SHORT).show();
                                }
                            });

                    }

                    @Override
                    public ModelVillageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view =  LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.village_item, parent, false);
                        return  new ModelVillageViewHolder(view);
                    }
                };
        rv.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

        //new Firebaserecycleradapter
        Query mpquery = mpdatabase;

        FirebaseRecyclerOptions<Modelmp> mpoptions = new FirebaseRecyclerOptions.Builder<Modelmp>()
                .setQuery(mpquery,Modelmp.class)
                .build();
        FirebaseRecyclerAdapter<Modelmp,ModelmpViewHolder> mpfirebaserecycleradapter = new FirebaseRecyclerAdapter<Modelmp, ModelmpViewHolder>(mpoptions) {
            @Override
            protected void onBindViewHolder(@NonNull ModelmpViewHolder holder, final int position, @NonNull final Modelmp model) {
                holder.setName(model.getName());
                holder.setImage(getContext(),model.getImage());



                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(),MemberProfileActivity.class);
                        intent.putExtra("mpid",getRef(position).getKey().toString());
                        startActivity(intent);
                    }
                });

            }

            @Override
            public ModelmpViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view =  LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.mp_item, parent, false);
                return  new ModelmpViewHolder(view);
            }
        };
        mp.setAdapter(mpfirebaserecycleradapter);
        mpfirebaserecycleradapter.startListening();

    }
    public  static class ModelVillageViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public ModelVillageViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }
        public void setVillage(String village) {
            TextView mvillage = mView.findViewById(R.id.village_name);
            mvillage.setText(village);

        }

        public void setConstituency(String constituency){
            TextView textView = mView.findViewById(R.id.constituency_name);
            textView.setText(constituency);
        }
        public void setState(String state){
            TextView textView = mView.findViewById(R.id.state_name);
            textView.setText(state);
        }

    }

    //new viewholder class
    public static class ModelmpViewHolder extends RecyclerView.ViewHolder{
        View mpView;

        public ModelmpViewHolder(View itemview) {
            super(itemview);
            this.mpView = itemview;

        }

        public void setName(String name) {
            TextView mmp_name = mpView.findViewById(R.id.mp_name);
            mmp_name.setText(name);
        }
        public void setImage(Context ctx, String image){
            ImageView mimageview = mpView.findViewById(R.id.mp_profile);
            Picasso.with(ctx).load(image).into(mimageview);
        }



    }

}
