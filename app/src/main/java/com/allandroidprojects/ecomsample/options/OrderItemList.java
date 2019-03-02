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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.product.ItemDetailsActivity;
import com.allandroidprojects.ecomsample.product.ProductInfo;
import com.allandroidprojects.ecomsample.utility.Api;
import com.allandroidprojects.ecomsample.utility.PrefManager;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;
import com.facebook.drawee.view.SimpleDraweeView;

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
import retrofit2.converter.gson.GsonConverterFactory;

import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.ITEM_ID;
import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.STRING_IMAGE_POSITION;
import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.adapter;

public class OrderItemList extends AppCompatActivity {

    static String ORDER_LIST = "OrderList";
    static int count;
    static String quantity, price, productUrl;
    static String urlLines;
    static Context mContext;
    static String lines_id;
    ArrayList<ProductInfo> productList;

    private static OrderItemList.SimpleStringRecyclerViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        mContext = getApplicationContext();
        productList = new ArrayList<>();
        urlLines = getIntent().getStringExtra(ORDER_LIST);
        int last_slash = urlLines.length() - 7;
        int ls = last_slash;
        while (urlLines.charAt(last_slash - 1) != '/') {
            last_slash--;
        }
        lines_id = urlLines.substring(last_slash, ls);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.order_recyclerview);
        RecyclerView.LayoutManager recyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        adapter = new OrderItemList.SimpleStringRecyclerViewAdapter(recyclerView, productList);
        recyclerView.setAdapter(adapter);

        new GetDetailsTask().execute();

    }

    class GetDetailsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Call<ResponseBody> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    .get_orders(lines_id, new PrefManager(mContext).getUserName());
            try {
                JSONObject jsonObject = new JSONObject(call.execute().body().string());
                count = jsonObject.getInt("count");
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < count; i++) {
                    JSONObject temp = (JSONObject) jsonArray.get(i);
                    quantity = String.valueOf(temp.getInt("quantity"));
                    price = temp.getString("price_currency") + " " + temp.getString("price_incl_tax_excl_discounts");
                    productUrl = temp.getString("product");
                    String pdid;
                    int last_slash = productUrl.length() - 1;
                    int ls = last_slash;
                    while (productUrl.charAt(last_slash - 1) != '/') {
                        last_slash--;
                    }
                    pdid = productUrl.substring(last_slash, ls);
                    Call<ResponseBody> call2 = RetrofitClient
                            .getInstance()
                            .getApi()
                            .get_product_details(pdid, new PrefManager(mContext).getUserName());
                    JSONArray product_object = (JSONArray) new JSONArray(call2.execute().body().string());
                    JSONObject prod_obj = (JSONObject) product_object.get(0);
                    String name = "", pid = "", imageUri = "";
                    name = prod_obj.getString("title");
                    pid = String.valueOf(prod_obj.getInt("id"));
                    if (prod_obj.getJSONArray("images").length() != 0) {
                        JSONObject temp1 = (JSONObject) prod_obj.getJSONArray("images").get(0);
                        imageUri = temp1.getString("original");
                    } else
                        imageUri = "https://www.azfinesthomes.com/assets/images/image-not-available.jpg";
                    ProductInfo productInfo = new ProductInfo(pid, name, imageUri, price, quantity);
                    productList.add(productInfo);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
        }
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<OrderItemList.SimpleStringRecyclerViewAdapter.ViewHolder> {

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
                mImageView = (SimpleDraweeView) view.findViewById(R.id.image_orderlist);
                mLayoutItem = (LinearLayout) view.findViewById(R.id.order_item);
                name = (TextView) mLayoutItem.findViewById(R.id.order_item_name);
                price = (TextView) mLayoutItem.findViewById(R.id.order_item_price);
                quantity = (TextView) mLayoutItem.findViewById(R.id.order_item_quantity);
            }
        }

        public SimpleStringRecyclerViewAdapter(RecyclerView recyclerView, ArrayList<ProductInfo> cartlist) {
            mCartlist = cartlist;
            mRecyclerView = recyclerView;
        }

        @Override
        public OrderItemList.SimpleStringRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_order_list_item, parent, false);
            return new OrderItemList.SimpleStringRecyclerViewAdapter.ViewHolder(view);
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
        public void onBindViewHolder(final OrderItemList.SimpleStringRecyclerViewAdapter.ViewHolder holder, final int position) {

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
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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


}
