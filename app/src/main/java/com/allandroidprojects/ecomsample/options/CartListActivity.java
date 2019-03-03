package com.allandroidprojects.ecomsample.options;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.product.ItemDetailsActivity;
import com.allandroidprojects.ecomsample.product.ProductInfo;
import com.allandroidprojects.ecomsample.startup.MainActivity;
import com.allandroidprojects.ecomsample.utility.Api;
import com.allandroidprojects.ecomsample.utility.ImageUrlUtils;
import com.allandroidprojects.ecomsample.utility.PrefManager;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.ITEM_ID;
import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.STRING_IMAGE_POSITION;
import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.STRING_IMAGE_URI;
import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.adapter;

public class CartListActivity extends AppCompatActivity {
    private static Context mContext;
    LinearLayout layoutCartItems, layoutCartPayments, layoutCartNoItems;
    TextView textViewPayment;
    private static PrefManager prefManager;

    ArrayList<ProductInfo> cart_list;
    int list_id;
    String basket_url;
    String total_price;
    int count;
    private static CartListActivity.SimpleStringRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_list);
        mContext = CartListActivity.this;
        layoutCartItems = (LinearLayout) findViewById(R.id.layout_items);
        layoutCartPayments = (LinearLayout) findViewById(R.id.layout_payment);
        layoutCartNoItems = (LinearLayout) findViewById(R.id.layout_cart_empty);
        textViewPayment = (TextView) findViewById(R.id.payment);
        textViewPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prefManager.isAuthenticated()) {
                    Call<ResponseBody> call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .checkout(prefManager.getUserName(), basket_url);

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                MainActivity.notificationCountCart = 0;
                                prefManager.setAuthenticate(false);
                                Toast.makeText(mContext, "Payment Successful and Check-Out", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else
                    Toast.makeText(mContext, "Please Check-In First", Toast.LENGTH_SHORT).show();

            }
        });
        prefManager = new PrefManager(this);
        cart_list = new ArrayList<>();
//        setCartLayout(MainActivity.notificationCountCart);
        new GetCartTask().execute(prefManager.getUserName());

        //Show cart layout based on items
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.cart_recyclerview);
        RecyclerView.LayoutManager recyclerViewLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        adapter = new CartListActivity.SimpleStringRecyclerViewAdapter(recyclerView, cart_list);
        recyclerView.setAdapter(adapter);
    }

    protected void setCartLayout(int items) {
        layoutCartItems = (LinearLayout) findViewById(R.id.layout_items);
        layoutCartPayments = (LinearLayout) findViewById(R.id.layout_payment);
        layoutCartNoItems = (LinearLayout) findViewById(R.id.layout_cart_empty);

        if (items > 0) {
            layoutCartNoItems.setVisibility(View.GONE);
            layoutCartItems.setVisibility(View.VISIBLE);
            layoutCartPayments.setVisibility(View.VISIBLE);

        } else {
            layoutCartNoItems.setVisibility(View.VISIBLE);
            layoutCartItems.setVisibility(View.GONE);
            layoutCartPayments.setVisibility(View.GONE);

            Button bStartShopping = (Button) findViewById(R.id.shop_now_button);
            bStartShopping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    startActivity(new Intent(CartListActivity.this, MainActivity.class));
                    finish();
                }
            });
        }
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<CartListActivity.SimpleStringRecyclerViewAdapter.ViewHolder> {

        private ArrayList<ProductInfo> mCartlist;
        private RecyclerView mRecyclerView;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final SimpleDraweeView mImageView;
            public final LinearLayout mLayoutItem;
            public final TextView name, price, quantity;
//            mLayoutRemove, mLayoutEdit;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (SimpleDraweeView) view.findViewById(R.id.image_cartlist);
                mLayoutItem = (LinearLayout) view.findViewById(R.id.cart_item);
                name = (TextView) mLayoutItem.findViewById(R.id.cart_item_name);
                price = (TextView) mLayoutItem.findViewById(R.id.cart_item_price);
                quantity = (TextView) mLayoutItem.findViewById(R.id.cart_item_quantity);
//                mLayoutRemove = (LinearLayout) view.findViewById(R.id.layout_item_remove);
//                mLayoutEdit = (LinearLayout) view.findViewById(R.id.layout_item_edit);
            }
        }

        public SimpleStringRecyclerViewAdapter(RecyclerView recyclerView, ArrayList<ProductInfo> cartlist) {
            mCartlist = cartlist;
            mRecyclerView = recyclerView;
        }

        @Override
        public CartListActivity.SimpleStringRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_cartlist_item, parent, false);
            return new CartListActivity.SimpleStringRecyclerViewAdapter.ViewHolder(view);
        }

//        @Override
//        public void onViewRecycled(CartListActivity.SimpleStringRecyclerViewAdapter.ViewHolder holder) {
//            if (holder.mImageView.getController() != null) {
//                holder.mImageView.getController().onDetach();
//            }
//            if (holder.mImageView.getTopLevelDrawable() != null) {
//                holder.mImageView.getTopLevelDrawable().setCallback(null);
////                ((BitmapDrawable) holder.mImageView.getTopLevelDrawable()).getBitmap().recycle();
//            }
//        }

        @Override
        public void onBindViewHolder(final CartListActivity.SimpleStringRecyclerViewAdapter.ViewHolder holder, final int position) {

            //bind data to view not done

//            final Uri uri = Uri.parse(mCartlistImageUri.get(position));
//            holder.mImageView.setImageURI(uri);

            final Uri uri = Uri.parse(mCartlist.get(position).getImg_url());
            holder.mImageView.setImageURI(uri);
            holder.name.setText(mCartlist.get(position).getProduct_title());
            holder.price.setText(mCartlist.get(position).getProduct_price());
            holder.quantity.setText(holder.quantity.getText() + mCartlist.get(position).getCart_quantity());

            holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ItemDetailsActivity.class);
                    intent.putExtra(ITEM_ID, mCartlist.get(position).getProduct_id());
                    intent.putExtra(STRING_IMAGE_POSITION, position);
                    mContext.startActivity(intent);
                }
            });

            //Set click action
//            holder.mLayoutRemove.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    // api call to remove item for cart not done
//
////                    ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
////                    imageUrlUtils.removeCartListImageUri(position);
//                    notifyDataSetChanged();
//                    //Decrease notification count
//                    MainActivity.notificationCountCart--;
//
//                }
//            });
//
//            //Set click action
//            holder.mLayoutEdit.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                }
//            });
//        }
        }

        @Override
        public int getItemCount() {
            return mCartlist == null ? 0 : mCartlist.size();
        }
    }

    class GetCartTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String userName = strings[0];
            Call<ResponseBody> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    .get_cart(prefManager.getUserName());
            try {
                try {
                    JSONObject jsonObject = new JSONObject(call.execute().body().string());
                    list_id = jsonObject.getInt("id");
                    basket_url = jsonObject.getString("url");
                    total_price = jsonObject.getString("currency") + " " + jsonObject.getString("total_incl_tax_excl_discounts");
                    if (!jsonObject.getString("total_incl_tax_excl_discounts").equals("0.00")) {
                        Call<ResponseBody> call1 = RetrofitClient
                                .getInstance()
                                .getApi()
                                .get_cart_list(String.valueOf(list_id), prefManager.getUserName());
                        jsonObject = new JSONObject(call1.execute().body().string());
                        count = jsonObject.getInt("count");
                        if (count != 0) {
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            for (int i = 0; i < count; i++) {
                                JSONObject temp = (JSONObject) jsonArray.get(i);
                                final int quantity = temp.getInt("quantity");
                                final String price = temp.getString("price_currency") + " "
                                        + temp.getString("price_incl_tax_excl_discounts");

                                String product = temp.getString("product");
                                String pdid = "";
                                int last_slash = product.length() - 1;
                                int ls = last_slash;
                                while (product.charAt(last_slash - 1) != '/') {
                                    last_slash--;
                                }
                                pdid = product.substring(last_slash, ls);

                                // call to get product detail not done

                                Call<ResponseBody> call2 = RetrofitClient
                                        .getInstance()
                                        .getApi()
                                        .get_product_details(pdid, prefManager.getUserName());
                                String name = "", pid = "", imageUri = "";
                                JSONArray jsonArray2 = (JSONArray) new JSONArray(call2.execute().body().string());
                                JSONObject jsonObject2 = (JSONObject) jsonArray2.get(0);
                                name = jsonObject2.getString("title");
                                pid = jsonObject2.getString("id");
                                if (jsonObject2.getJSONArray("images").length() != 0) {
                                    JSONObject temp1 = (JSONObject) jsonObject2.getJSONArray("images").get(0);
                                    imageUri = temp1.getString("original");
                                } else
                                    imageUri = "https://www.azfinesthomes.com/assets/images/image-not-available.jpg";
                                ProductInfo prod = new ProductInfo(pid, name, imageUri, price, String.valueOf(quantity));
                                cart_list.add(prod);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setCartLayout(count);
            if (count != 0) {
                TextView textViewPrice = (TextView) findViewById(R.id.total_price);
                textViewPrice.setText(total_price);
                adapter.notifyDataSetChanged();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

}