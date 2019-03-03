package com.allandroidprojects.ecomsample.product;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.fragments.ImageListFragment;
import com.allandroidprojects.ecomsample.notification.NotificationCountSetClass;
import com.allandroidprojects.ecomsample.options.CartListActivity;
import com.allandroidprojects.ecomsample.startup.MainActivity;
import com.allandroidprojects.ecomsample.utility.ImageUrlUtils;
import com.allandroidprojects.ecomsample.utility.PrefManager;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.ITEM_ID;

public class ItemDetailsActivity extends AppCompatActivity {
    String imageUri;
    String itemName;
    String productId;
    String productPrice;
    String productDesc;
    String prodouctStock;
    String product_url;
    int quantity = 2;
    ArrayList<ProductInfo> similar_products_list;
    RecyclerView similar_products;
    SimpleDraweeView mImageView;
    TextView textViewItemName, textViewItemPrice, textViewItemDesc, textViewAddToCart, textViewBuyNow, textViewItemStock;
    LinearLayout linearLayoutWishList;
    ImageView imageWishListActive;

    private static Context mContext;
    SimilarProductsAdapter adapter;

    PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);
        mContext = ItemDetailsActivity.this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        similar_products_list = new ArrayList<>();
//        similar_products = (RecyclerView) findViewById(R.id.similar_products);

        mImageView = (SimpleDraweeView) findViewById(R.id.image1);
        textViewItemName = (TextView) findViewById(R.id.item_name);
        textViewItemPrice = (TextView) findViewById(R.id.item_price);
//        linearLayoutWishList = (LinearLayout) findViewById(R.id.wish_list_layout);
//        imageWishListActive = (ImageView) linearLayoutWishList.findViewById(R.id.wish_list_active);
        textViewItemDesc = (TextView) findViewById(R.id.item_desc);
        textViewAddToCart = (TextView) findViewById(R.id.add_to_cart_button);
        textViewBuyNow = (TextView) findViewById(R.id.buy_now_button);
        textViewItemStock = (TextView) findViewById(R.id.item_stock);

        prefManager = new PrefManager(this);
        //Getting image uri from previous screen
        if (getIntent() != null) {
            productId = getIntent().getStringExtra(ITEM_ID);
        }

        new ProductAsync().execute();

        // Api call for add to cart
        textViewAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                System.out.println("\n\n"+product_url+"   "+quantity+" \n\n");
//                product_url = "products/105/";

                Call<ResponseBody> call = RetrofitClient
                        .getInstance()
                        .getApi()
                        .add_to_cart(productId, quantity, prefManager.getUserName());


                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        System.out.println(response.toString());
                        Toast.makeText(ItemDetailsActivity.this, "Item added to cart.", Toast.LENGTH_SHORT).show();
                        MainActivity.notificationCountCart++;
                        NotificationCountSetClass.setNotifyCount(MainActivity.notificationCountCart);

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(ItemDetailsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //api call for buy
        textViewBuyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Call<ResponseBody> call = RetrofitClient
                        .getInstance()
                        .getApi()
                        .add_to_cart(productId, quantity, prefManager.getUserName());

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        System.out.println(response.toString());
                        Toast.makeText(ItemDetailsActivity.this, "Item added to cart.", Toast.LENGTH_SHORT).show();

                        MainActivity.notificationCountCart++;
                        NotificationCountSetClass.setNotifyCount(MainActivity.notificationCountCart);
                        startActivity(new Intent(ItemDetailsActivity.this, CartListActivity.class));

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(ItemDetailsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        //api call for wishlist
//        linearLayoutWishList.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
//                imageUrlUtils.addWishlistImageUri(imageUri);
//                imageWishListActive.setImageResource(R.drawable.ic_favorite_black_18dp);
//                Toast.makeText(ItemDetailsActivity.this, "Item added to wishlist.", Toast.LENGTH_SHORT).show();
//            }
//        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.similar_products);
        RecyclerView.LayoutManager recyclerViewLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        adapter = new ItemDetailsActivity.SimilarProductsAdapter(recyclerView, similar_products_list);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    class ProductAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Call<ResponseBody> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    .get_product_details(productId, prefManager.getUserName());
            try {
                JSONArray jsonArray = (JSONArray) new JSONArray(call.execute().body().string());
                JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                product_url = jsonObject.getString("url");
                itemName = jsonObject.getString("title");
                productDesc = jsonObject.getString("description");
                if (jsonObject.getJSONArray("images").length() != 0) {
                    JSONObject temp1 = (JSONObject) jsonObject.getJSONArray("images").get(0);
                    imageUri = temp1.getString("original");
                } else
                    imageUri = "https://www.azfinesthomes.com/assets/images/image-not-available.jpg";
                productPrice = jsonObject.getJSONObject("price").getString("currency") + " " +
                        jsonObject.getJSONObject("price").getString("incl_tax");
                prodouctStock = jsonObject.getJSONObject("availability").getString("message");

                JSONArray similar_prod_json_array = jsonObject.getJSONArray("recommended_products");
                for (int i = 0; i < similar_prod_json_array.length(); i++) {
                    JSONObject temp = (JSONObject) similar_prod_json_array.get(i);
                    String temp_url = temp.getString("url");
                    int last_slash = temp_url.length() - 1;
                    int ls = last_slash;
                    while (temp_url.charAt(last_slash - 1) != '/') {
                        last_slash--;
                    }
                    temp_url = temp_url.substring(last_slash, ls);
                    Call<ResponseBody> temp_call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .get_product_details(temp_url, prefManager.getUserName());
                    JSONArray temp_array = (JSONArray) new JSONArray(temp_call.execute().body().string());
                    temp = (JSONObject) temp_array.get(0);
                    String pUrl, pName, pPrice;
                    pName = temp.getString("title");
                    if (temp.getJSONArray("images").length() != 0) {
                        JSONObject temp1 = (JSONObject) temp.getJSONArray("images").get(0);
                        pUrl = temp1.getString("original");
                    } else
                        pUrl = "https://www.azfinesthomes.com/assets/images/image-not-available.jpg";
                    pPrice = temp.getJSONObject("price").getString("currency") + " " +
                            temp.getJSONObject("price").getString("incl_tax");

                    similar_products_list.add(new ProductInfo(temp_url, pName, pUrl, pPrice));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Uri uri = Uri.parse(imageUri);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Uri uri = Uri.parse(imageUri);
            mImageView.setImageURI(uri);
            textViewItemName.setText(itemName);
            textViewItemDesc.setText(productDesc);
            textViewItemPrice.setText(productPrice);
            textViewItemStock.setText(prodouctStock);
            adapter.notifyDataSetChanged();
        }
    }

    public static class SimilarProductsAdapter
            extends RecyclerView.Adapter<ItemDetailsActivity.SimilarProductsAdapter.ViewHolder> {

        private ArrayList<ProductInfo> list;
        private RecyclerView mRecyclerView;

        public static class ViewHolder extends RecyclerView.ViewHolder {
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

        public SimilarProductsAdapter(RecyclerView recyclerView, ArrayList<ProductInfo> items) {
            list = items;
            mRecyclerView = recyclerView;
        }

        @Override
        public ItemDetailsActivity.SimilarProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ItemDetailsActivity.SimilarProductsAdapter.ViewHolder(view);
        }
//
//        @Override
//        public void onViewRecycled(ImageListFragment.SimpleStringRecyclerViewAdapter.ViewHolder holder) {
//            if (holder.mImageView.getController() != null) {
//                holder.mImageView.getController().onDetach();
//            }
//            if (holder.mImageView.getTopLevelDrawable() != null) {
//                holder.mImageView.getTopLevelDrawable().setCallback(null);
////                ((BitmapDrawable) holder.mImageView.getTopLevelDrawable()).getBitmap().recycle();
//            }
//        }

        @Override
        public void onBindViewHolder(final ItemDetailsActivity.SimilarProductsAdapter.ViewHolder holder, final int position) {
            if (list.size() > 0) {

                final Uri uri = Uri.parse(list.get(position).getImg_url());

                holder.mImageView.setImageURI(uri);
                holder.mProductName.setText(list.get(position).getProduct_title());
                holder.mProductPrice.setText(list.get(position).getProduct_price());

                holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ItemDetailsActivity.class);
                        intent.putExtra(ITEM_ID, list.get(position).getProduct_id());
                        mContext.startActivity(intent);
                    }
                });

            }

        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }
    }
}
