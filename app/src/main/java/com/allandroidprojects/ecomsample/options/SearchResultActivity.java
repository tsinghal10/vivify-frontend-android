package com.allandroidprojects.ecomsample.options;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.product.ItemDetailsActivity;
import com.allandroidprojects.ecomsample.product.ProductInfo;
import com.allandroidprojects.ecomsample.startup.MainActivity;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.ITEM_ID;
import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.STRING_IMAGE_POSITION;

// On scroll lode more items pagination not done.

public class SearchResultActivity extends AppCompatActivity {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    public SearchView searchView;

    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private ArrayList<ProductInfo> productInfoArrayList;
    private String speechResult;
    static private String query;
    public int search_page = 1;
    public boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recylerview_list);

        productInfoArrayList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchAdapter(recyclerView, productInfoArrayList);
        recyclerView.setAdapter(adapter);
        initScrollListener();
        handleIntent(getIntent());
    }

    //pagination
    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    int arr[] = new int[2];
                    if (staggeredGridLayoutManager != null) {
                        arr = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(arr);
                        if (Math.max(arr[0], arr[1]) == productInfoArrayList.size() - 1) {
//                            Toast.makeText(this, "Load More", Toast.LENGTH_LONG).show();
                            callApi();
                            isLoading = true;
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.getItem(0);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.action_search1).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setFocusable(true);
        searchView.setSubmitButtonEnabled(true);
        searchItem.expandActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.qr_search) {
            Intent intent = new Intent(SearchResultActivity.this, QRScannerActivity.class);
            intent.putExtra("from", SearchResultActivity.class.toString());
            startActivity(intent);
            return true;
        } else if (id == R.id.voice_search) {
            promptSpeechInput();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null) {
//                search_page = 1;
//                isLoading = false;
                productInfoArrayList.clear();
                callApi();
            }
        }
    }

    private void callApi() {

        Call<ResponseBody> call = RetrofitClient
                .getInstance()
                .getApi()
                .get_products(search_page, query);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        JSONObject jsonObject = (JSONObject) new JSONObject((response.body().string()));
                        if (!jsonObject.getString("count").equals("0")) {
                            JSONArray json = jsonObject.getJSONArray("results");
                            String url, name, pclass, id, price;
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
                                        temp.getJSONObject("price").getString("incl_tax");

                                ProductInfo product = new ProductInfo(id, name, url, price);
                                productInfoArrayList.add(product);
                            }
                            Toast.makeText(SearchResultActivity.this, "Results Found", Toast.LENGTH_LONG).show();
                            adapter.notifyDataSetChanged();
                            search_page += 1;
                        } else {
                            adapter.notifyDataSetChanged();
                            Toast.makeText(SearchResultActivity.this, "Results not found", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(SearchResultActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say Something!");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Speech Not Supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    search_text_view.setText(result.get(0));
                    speechResult = result.get(0);
                    searchView.setQuery(speechResult, true);
                }
                break;
            }

        }
    }

    public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

        private ArrayList<ProductInfo> list;
        private RecyclerView recyclerView;

        public SearchAdapter(RecyclerView recyclerView, ArrayList<ProductInfo> list) {
            this.recyclerView = recyclerView;
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new SearchAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            if (list.size() > 0) {

                final Uri uri = Uri.parse(list.get(position).getImg_url());

                holder.mImageView.setImageURI(uri);
                holder.mProductName.setText(list.get(position).getProduct_title());
                holder.mProductPrice.setText(list.get(position).getProduct_price());

                holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SearchResultActivity.this, ItemDetailsActivity.class);
                        intent.putExtra(ITEM_ID, list.get(position).getProduct_id());
                        intent.putExtra(STRING_IMAGE_POSITION, position);
                        SearchResultActivity.this.startActivity(intent);

                    }
                });

            }

        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final SimpleDraweeView mImageView;
            public final LinearLayout mLayoutItem;
            public final TextView mProductName;
            public final TextView mProductPrice;
            public final ImageView mImageViewWishlist;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (SimpleDraweeView) view.findViewById(R.id.image1);
                mLayoutItem = (LinearLayout) view.findViewById(R.id.layout_item);
                mProductName = (TextView) mLayoutItem.findViewById(R.id.item_name);
                mProductPrice = (TextView) mLayoutItem.findViewById(R.id.item_price);
                mImageViewWishlist = (ImageView) view.findViewById(R.id.ic_wishlist);
            }
        }
    }

}
