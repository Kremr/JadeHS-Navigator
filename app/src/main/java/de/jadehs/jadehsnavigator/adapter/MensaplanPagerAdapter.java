package de.jadehs.jadehsnavigator.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.model.MensaplanDay;
import de.jadehs.jadehsnavigator.model.MensaplanMeal;

public class MensaplanPagerAdapter extends PagerAdapter {

    private String TAG = "MensaplanPagerAdapter";
    private Context context;


    private ArrayList<MensaplanDay> mensaplanData = new ArrayList<>();

    private View view;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LayoutInflater mInflater;

    public MensaplanPagerAdapter(Context context, ArrayList<MensaplanDay> data) {
        this.context = context;
        this.mensaplanData = data;
    }

    @Override
    public int getCount() {
        return mensaplanData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        String tabTitle = "";
        switch(position+1){
            case 1:
                tabTitle =  context.getString(R.string.strWeekdayMonday);
                break;
            case 2:
                tabTitle =  context.getString(R.string.strWeekdayTuesday);
                break;
            case 3:
                tabTitle =  context.getString(R.string.strWeekdayWednesday);
                break;
            case 4:
                tabTitle =  context.getString(R.string.strWeekdayThursday);
                break;
            case 5:
                tabTitle =  context.getString(R.string.strWeekdayFriday);
        }
        return tabTitle;
    }



    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // Inflate a new layout from our resources
        ArrayList<MensaplanMeal> mensaplanMeals = mensaplanData.get(position).getMeals();
        mInflater = (LayoutInflater) this.context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        if(mensaplanMeals.isEmpty()) {
            view = mInflater.inflate(R.layout.mensaplan_emptystate,container,false);
            container.addView(view);
        } else {
            final SwipeRefreshLayout swipeRefreshLayout= (SwipeRefreshLayout) container.getParent();
            view = mInflater.inflate(R.layout.mensaplan_recycleview, container, false);

            container.addView(view);
            mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
            mRecyclerView.setHasFixedSize(true);

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                private int overallYScrol = 0;
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //overallYScrol = 0 gibt an, dass die Recycleview bis nach ganz oben gescrolled ist und somit
                    // der swipeRefresh aktiviert werden kann.
                    overallYScrol = overallYScrol + dy;
                    if(overallYScrol == 0) {
                        swipeRefreshLayout.setEnabled(true);
                    } else {
                        swipeRefreshLayout.setEnabled(false);
                    }
                }
            });


            mLayoutManager = new LinearLayoutManager(context);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new MensaplanRecycleAdapter(mensaplanMeals);

            List<MensaplanSectionedRecycleAdapter.Section> sections = new ArrayList<>();

            int sectionStartPosition = 0;
            int sectionType = 0;

            String [] headerTitles  = context.getResources().getStringArray(R.array.mensaplan_sectionheader);

            for(MensaplanMeal item: mensaplanMeals) {
                if(sectionType != item.getType())  {
                    sectionType = item.getType();
                    String sectionTitle = headerTitles[item.getType()-1];
                    sections.add(new MensaplanSectionedRecycleAdapter.Section(sectionStartPosition,sectionTitle));
                }
                sectionStartPosition++;
            }
            MensaplanSectionedRecycleAdapter.Section[] dummy = new MensaplanSectionedRecycleAdapter.Section[sections.size()];
            MensaplanSectionedRecycleAdapter mSectionedAdapter = new
                    MensaplanSectionedRecycleAdapter(context,R.layout.mensaplan_sectionheader, R.id.section_text,mAdapter);
            mSectionedAdapter.setSections(sections.toArray(dummy));

            mRecyclerView.setAdapter(mSectionedAdapter);

        }

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}