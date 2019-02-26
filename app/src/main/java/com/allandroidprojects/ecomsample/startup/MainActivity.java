package com.allandroidprojects.ecomsample.startup;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.fragments.ImageListFragment;
import com.allandroidprojects.ecomsample.miscellaneous.EmptyActivity;
import com.allandroidprojects.ecomsample.options.QRScannerActivity;
import com.allandroidprojects.ecomsample.product.ProductInfo;
import com.allandroidprojects.ecomsample.user.ProfileActivity;
import com.allandroidprojects.ecomsample.notification.NotificationCountSetClass;
import com.allandroidprojects.ecomsample.options.CartListActivity;
import com.allandroidprojects.ecomsample.options.SearchResultActivity;
import com.allandroidprojects.ecomsample.options.WishlistActivity;
import com.allandroidprojects.ecomsample.user.LoginActivity;
import com.allandroidprojects.ecomsample.utility.Api;
import com.allandroidprojects.ecomsample.utility.PrefManager;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static int notificationCountCart = 0;
    static ViewPager viewPager;
    static TabLayout tabLayout;
    private PrefManager prefManager;
    public static ArrayList<ProductInfo> lists[] = new ArrayList[6];
    public static String categoryList[] = {"the", "Book", "is", "the", "good", "other"};

    public static ArrayList<ProductInfo> book;
    public static String nextPageURL;


    public static int page[] = {2, 2, 2, 2, 2, 2};          //Category page

    static Dialog loadScreenDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for(int i=0; i<6; i++)
            lists[i] = new ArrayList<>();

        prefManager = new PrefManager(this);
        if (!prefManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        //Displaying user name
        View headerView = navigationView.getHeaderView(0);
        TextView user_name = (TextView) headerView.findViewById(R.id.userName);
        user_name.setText(prefManager.getUserName());

        navigationView.setNavigationItemSelectedListener(this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

//        showLoadScreen();

        for (int i=0; i<6; i++)
            callApi(i);

//        AsyncApiCalls api = new AsyncApiCalls();
//        api.execute();

    }


    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Get the notifications MenuItem and
        // its LayerDrawable (layer-list)
        MenuItem item = menu.findItem(R.id.action_cart);
        NotificationCountSetClass.setAddToCart(MainActivity.this, item, notificationCountCart);
        // force the ActionBar to relayout its MenuItems.
        // onCreateOptionsMenu(Menu) will be called again.
        invalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            startActivity(new Intent(MainActivity.this, SearchResultActivity.class));
            return true;
        } else if (id == R.id.action_cart) {

           /* NotificationCountSetClass.setAddToCart(MainActivity.this, item, notificationCount);
            invalidateOptionsMenu();*/
            startActivity(new Intent(MainActivity.this, CartListActivity.class));

            //test_logout
            prefManager.setIsLoggedIn(false);
            //test_logout_end

           /* notificationCount=0;//clear notification count
            invalidateOptionsMenu();*/
            return true;
        } else {
            startActivity(new Intent(MainActivity.this, EmptyActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());

        ImageListFragment fragment = new ImageListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", 1);
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, getString(R.string.item_1));

        fragment = new ImageListFragment();
        bundle = new Bundle();
        bundle.putInt("type", 2);
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, getString(R.string.item_2));

        fragment = new ImageListFragment();
        bundle = new Bundle();
        bundle.putInt("type", 3);
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, getString(R.string.item_3));

        fragment = new ImageListFragment();
        bundle = new Bundle();
        bundle.putInt("type", 4);
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, getString(R.string.item_4));

        fragment = new ImageListFragment();
        bundle = new Bundle();
        bundle.putInt("type", 5);
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, getString(R.string.item_5));

        fragment = new ImageListFragment();
        bundle = new Bundle();
        bundle.putInt("type", 6);
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, getString(R.string.item_6));

        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_item1) {
            viewPager.setCurrentItem(0);
        } else if (id == R.id.nav_item2) {
            viewPager.setCurrentItem(1);
        } else if (id == R.id.nav_item3) {
            viewPager.setCurrentItem(2);
        } else if (id == R.id.nav_item4) {
            viewPager.setCurrentItem(3);
        } else if (id == R.id.nav_item5) {
            viewPager.setCurrentItem(4);
        } else if (id == R.id.nav_item6) {
            viewPager.setCurrentItem(5);
        } else if (id == R.id.my_wishlist) {
            startActivity(new Intent(MainActivity.this, WishlistActivity.class));
        } else if (id == R.id.my_cart) {
            startActivity(new Intent(MainActivity.this, CartListActivity.class));
        } else if (id == R.id.my_account) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if (id == R.id.qr_scanner) {
            Intent intent = new Intent(MainActivity.this, QRScannerActivity.class);
            intent.putExtra("from", MainActivity.class.toString());
            startActivity(intent);
        } else {
            startActivity(new Intent(MainActivity.this, EmptyActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

    }

    private class AsyncApiCalls extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void ... aVoid) {
            for(int i=0; i<6; i++) {
                callApi(i);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
//            super.onPreExecute();
//            showLoadScreen();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (viewPager != null) {
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }

    public void callApi(final int position) {

        Call<ResponseBody> call = RetrofitClient
                .getInstance()
                .getApi()
                .get_products(1, categoryList[position]);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        try {
                            JSONObject jsonObject = (JSONObject) new JSONObject((response.body().string()));
                            JSONArray json = jsonObject.getJSONArray("results");
                            String url, name, id, price;
                            for (int i = 0; i < json.length(); i++) {

                                JSONObject temp = (JSONObject) json.get(i);
                                if (temp.getJSONArray("images").length() != 0) {
                                    JSONObject temp1 = (JSONObject) temp.getJSONArray("images").get(0);
                                    url = temp1.getString("original");
                                } else
                                    url = "https://www.azfinesthomes.com/assets/images/image-not-available.jpg";
                                id = temp.getString("id");
                                name = temp.getString("title");

                                price = temp.getJSONObject("price").getString("currency") + " " +
                                        temp.getJSONObject("price").getString("incl_tax");             //Get from API

                                ProductInfo product = new ProductInfo(id, name, url, price);

                                lists[position].add(product);
                            }
                            if (position == 0){
//                                loadScreenDialog.dismiss();
                                if (viewPager != null) {
                                    setupViewPager(viewPager);
                                    tabLayout.setupWithViewPager(viewPager);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }


    public void showLoadScreen() {
        View view = getLayoutInflater().inflate(R.layout.layout_splashscreen, null);
        loadScreenDialog = new Dialog(this, R.style.AppTheme);
        loadScreenDialog.setContentView(view);
        loadScreenDialog.show();
    }
}
