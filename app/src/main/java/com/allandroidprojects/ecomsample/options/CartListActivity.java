package com.allandroidprojects.ecomsample.options;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.product.ItemDetailsActivity;
import com.allandroidprojects.ecomsample.product.ProductInfo;
import com.allandroidprojects.ecomsample.startup.MainActivity;
import com.allandroidprojects.ecomsample.utility.ImageUrlUtils;
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

import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.ITEM_ID;
import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.STRING_IMAGE_POSITION;
import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.STRING_IMAGE_URI;

public class CartListActivity extends AppCompatActivity {
    private static Context mContext;
    LinearLayout layoutCartItems, layoutCartPayments, layoutCartNoItems;

    ArrayList<ProductInfo> cart_list;
    int list_id;
    String total_price;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_list);
        mContext = CartListActivity.this;
        cart_list = new ArrayList<>();
        setCartLayout(MainActivity.notificationCountCart);

//        Call<ResponseBody> call = RetrofitClient
//                .getInstance()
//                .getApi()
//                .get_cart();
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                try {
//                    final JSONObject jsonObject = (JSONObject) new JSONObject(response.body().string());
//                    list_id = jsonObject.getInt("id");
//                    total_price = jsonObject.getString("currency") + " " + jsonObject.getString("total_incl_tax_excl_discounts");
//
//                    Call<ResponseBody> call1 = RetrofitClient
//                            .getInstance()
//                            .getApi()
//                            .get_cart_list(String.valueOf(list_id));
//
//                    call1.enqueue(new Callback<ResponseBody>() {
//                        @Override
//                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                            try {
//                                JSONObject jsonObject1 = (JSONObject) new JSONObject(response.body().string());
//                                count = jsonObject1.getInt("count");
//                                JSONArray jsonArray = jsonObject1.getJSONArray("results");
//                                for (int i = 0; i < jsonArray.length(); i++) {
//                                    JSONObject temp = (JSONObject) jsonArray.get(i);
//                                    int quantity = temp.getInt("quantity");
//                                    String price = temp.getString("price_currency") + " "
//                                            + temp.getString("price_incl_tax_excl_discounts");
//
//                                    String product = temp.getString("product");
//
//                                    // call to get product detail not done
//                                    int id = Integer.parseInt(product.substring(0));
//                                    Call<ResponseBody> call2 = RetrofitClient
//                                            .getInstance()
//                                            .getApi()
//                                            .get_product_details(String.valueOf(id));
//
//                                    call2.enqueue(new Callback<ResponseBody>() {
//                                        @Override
//                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//
//                                        }
//
//                                        @Override
//                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
//                                            Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                }
//
//                                setCartLayout(count);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<ResponseBody> call, Throwable t) {
//                            Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
//
//                        }
//                    });
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
//
//            }
//        });


//        ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
//        ArrayList<String> cartlistImageUri =imageUrlUtils.getCartListImageUri();
        //Show cart layout based on items


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.cart_recyclerview);
        RecyclerView.LayoutManager recyclerViewLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(new CartListActivity.SimpleStringRecyclerViewAdapter(recyclerView, cart_list));
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
            public final LinearLayout mLayoutItem, mLayoutRemove, mLayoutEdit;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (SimpleDraweeView) view.findViewById(R.id.image_cartlist);
                mLayoutItem = (LinearLayout) view.findViewById(R.id.cart_item);
                mLayoutRemove = (LinearLayout) view.findViewById(R.id.layout_item_remove);
                mLayoutEdit = (LinearLayout) view.findViewById(R.id.layout_item_edit);
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
            holder.mLayoutRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // api call to remove item for cart not done

//                    ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
//                    imageUrlUtils.removeCartListImageUri(position);
                    notifyDataSetChanged();
                    //Decrease notification count
                    MainActivity.notificationCountCart--;

                }
            });

            //Set click action
            holder.mLayoutEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCartlist == null ? 0 : mCartlist.size();
        }
    }

}