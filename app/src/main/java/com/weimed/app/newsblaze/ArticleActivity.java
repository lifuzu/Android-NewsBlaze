package com.weimed.app.newsblaze;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import in.uncod.android.bypass.Bypass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

import android.text.Html;
import android.text.Html.ImageGetter;
import com.commonsware.cwac.anddown.AndDown;
import com.weimed.app.utils.URLImageParser;
import com.weimed.app.widgets.TextViewWithImages;

public class ArticleActivity extends FragmentActivity /*implements ActionBar.TabListener*/ {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * Create the activity. Sets up an {@link android.app.ActionBar} with tabs, and then configures the
     * {@link android.support.v4.view.ViewPager} contained inside R.layout.activity_main.
     *
     * <p>A {@link com.weimed.app.newsblaze.ArticleActivity.SectionsPagerAdapter} will be instantiated to hold the different pages of
     * fragments that are to be displayed. A
     * {@link android.support.v4.view.ViewPager.SimpleOnPageChangeListener} will also be configured
     * to receive callbacks when the user swipes between pages in the ViewPager.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the UI from res/layout/activity_main.xml
        setContentView(R.layout.sample_main);

        // Set up the action bar. The navigation mode is set to NAVIGATION_MODE_TABS, which will
        // cause the ActionBar to render a set of tabs. Note that these tabs are *not* rendered
        // by the ViewPager; additional logic is lower in this file to synchronize the ViewPager
        // state with the tab state. (See mViewPager.setOnPageChangeListener() and onTabSelected().)
        // BEGIN_INCLUDE (set_navigation_mode)
//        final ActionBar actionBar = getActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // END_INCLUDE (set_navigation_mode)

        // BEGIN_INCLUDE (setup_view_pager)
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // END_INCLUDE (setup_view_pager)

        // When swiping between different sections, select the corresponding tab. We can also use
        // ActionBar.Tab#select() to do this if we have a reference to the Tab.
        // BEGIN_INCLUDE (page_change_listener)
//        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                actionBar.setSelectedNavigationItem(position);
//            }
//        });
        // END_INCLUDE (page_change_listener)

        // BEGIN_INCLUDE (add_tabs)
        // For each of the sections in the app, add a tab to the action bar.
//        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//            // Create a tab with text corresponding to the page title defined by the adapter. Also
//            // specify this Activity object, which implements the TabListener interface, as the
//            // callback (listener) for when this tab is selected.
//            actionBar.addTab(
//                    actionBar.newTab()
//                            .setText(mSectionsPagerAdapter.getPageTitle(i))
//                            .setTabListener(this));
//        }
        // END_INCLUDE (add_tabs)
    }

    @Override
    public void onPause() {
        super.onPause();

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    /**
     * Update {@link android.support.v4.view.ViewPager} after a tab has been selected in the ActionBar.
     *
     * @param tab Tab that was selected.
     * @param fragmentTransaction A {@link android.app.FragmentTransaction} for queuing fragment operations to
     *                            execute once this method returns. This FragmentTransaction does
     *                            not support being added to the back stack.
     */
    // BEGIN_INCLUDE (on_tab_selected)
//    @Override
//    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
//        // When the given tab is selected, tell the ViewPager to switch to the corresponding page.
//        mViewPager.setCurrentItem(tab.getPosition());
//    }
    // END_INCLUDE (on_tab_selected)

    /**
     * Unused. Required for {@link android.app.ActionBar.TabListener}.
     */
//    @Override
//    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
//    }

    /**
     * Unused. Required for {@link android.app.ActionBar.TabListener}.
     */
//    @Override
//    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
//    }

    // BEGIN_INCLUDE (fragment_pager_adapter)
    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages. This provides the data for the {@link android.support.v4.view.ViewPager}.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
    // END_INCLUDE (fragment_pager_adapter)

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // BEGIN_INCLUDE (fragment_pager_adapter_getitem)
        /**
         * Get fragment corresponding to a specific position. This will be used to populate the
         * contents of the {@link android.support.v4.view.ViewPager}.
         *
         * @param position Position to fetch fragment for.
         * @return Fragment for specified position.
         */
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new DummySectionFragment();
            Bundle args = new Bundle();
            //args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
//            if (position == 0) {
//                args.putString(DummySectionFragment.ARG_SECTION_FILENAME, "test.md");
//            } else if (position == 1) {
//                args.putString(DummySectionFragment.ARG_SECTION_FILENAME, "news_test1.md");
//            } else {
//                args.putString(DummySectionFragment.ARG_SECTION_FILENAME, "test.md");
//            }
            // Get the message from the intent
            Intent intent = getIntent();
            String content = intent.getStringExtra(MainActivity.EXTRA_NEWS_CONTENT);
            args.putString(DummySectionFragment.ARG_SECTION_CONTENT, content);
            fragment.setArguments(args);
            return fragment;
        }
        // END_INCLUDE (fragment_pager_adapter_getitem)

        // BEGIN_INCLUDE (fragment_pager_adapter_getcount)
        /**
         * Get number of pages the {@link android.support.v4.view.ViewPager} should render.
         *
         * @return Number of fragments to be rendered as pages.
         */
        @Override
        public int getCount() {
            // Show 3 total pages.
            return 1;
        }
        // END_INCLUDE (fragment_pager_adapter_getcount)

        // BEGIN_INCLUDE (fragment_pager_adapter_getpagetitle)
        /**
         * Get title for each of the pages. This will be displayed on each of the tabs.
         *
         * @param position Page to fetch title for.
         * @return Title for specified page.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
        // END_INCLUDE (fragment_pager_adapter_getpagetitle)
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     * This would be replaced with your application's content.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";
        public static final String ARG_SECTION_FILENAME = "section_filename";
        public static final String ARG_SECTION_CONTENT = "com.weimed.app.newblaze.CONTENT";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            //TextViewWithImages dummyTextView = (TextViewWithImages) rootView.findViewById(R.id.section_label);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);

            String markdownString = "###markdown!!!";
//            String fileName = getArguments().getString(ARG_SECTION_FILENAME);
//            try {
//                markdownString = LoadFile(fileName);
//            }catch (IOException e) {
//
//            }
            markdownString = getArguments().getString(ARG_SECTION_CONTENT);

            //Bypass bypass = new Bypass();
            //CharSequence string = bypass.markdownToSpannable(markdownString);

            AndDown converter=new AndDown();
            String cooked=converter.markdownToHtml(markdownString);
            //Log.i(ARG_SECTION_NUMBER, cooked);
            //CharSequence string=Html.fromHtml(cooked, getImageHTML(), null);
            URLImageParser p = new URLImageParser(dummyTextView, getActivity());
            Spanned string = Html.fromHtml(cooked, p, null);

            //dummyTextView.loadData(cooked, "text/html", "UTF-8");
            dummyTextView.setText(string);
            dummyTextView.setVerticalScrollBarEnabled(true);
            dummyTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

            //dummyTextView.setMovementMethod(LinkMovementMethod.getInstance());
            //dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }

        //load file from apps assets folder
        public String LoadFile(String fileName) throws IOException
        {
            //Create a InputStream to read the file into
            InputStream iS;

            //get the file as a stream
            iS = getResources().getAssets().open(fileName);

            //create a buffer that has the same size as the InputStream
            byte[] buffer = new byte[iS.available()];
            //read the text file as a stream, into the buffer
            iS.read(buffer);
            //create a output stream to write the buffer into
            ByteArrayOutputStream oS = new ByteArrayOutputStream();
            //write this buffer to the output stream
            oS.write(buffer);
            //Close the Input and Output streams
            oS.close();
            iS.close();

            //return the output stream as a String
            return oS.toString();
        }

        public ImageGetter getImageHTML() {
            ImageGetter imageGetter = new ImageGetter() {
                public Drawable getDrawable(String source) {
                    try {
                        Drawable drawable = Drawable.createFromStream(new URL(source).openStream(), "src name");
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                        return drawable;
                    } catch(IOException exception) {
                        Log.v("IOException",exception.getMessage());
                        return null;
                    }
                }
            };
            return imageGetter;
        }
    }

}
