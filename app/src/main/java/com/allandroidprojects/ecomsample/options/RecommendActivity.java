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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.product.ItemDetailsActivity;
import com.allandroidprojects.ecomsample.product.ProductInfo;
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

import static com.allandroidprojects.ecomsample.fragments.ImageListFragment.ITEM_ID;

public class RecommendActivity extends AppCompatActivity {

    ArrayList<ProductInfo> recently_view_list, mostly_viewed_list;
    RecyclerView recyclerView_recently, recyclerView_mostly;

    RecentlyProductsAdapter adapter1;
    MostlyProductsAdapter adapter2;

    PrefManager prefManager;

    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        mContext = RecommendActivity.this;
        prefManager = new PrefManager(mContext);

        recently_view_list = new ArrayList<>();
        mostly_viewed_list = new ArrayList<>();

        new RecommendActivity.RecentProductAsync().execute();
        new RecommendActivity.MostProductAsync().execute();

        recyclerView_recently = (RecyclerView) findViewById(R.id.recyclerview_recentlyviewed);
        RecyclerView.LayoutManager recyclerViewLayoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView_recently.setLayoutManager(recyclerViewLayoutManager1);
        adapter1 = new RecommendActivity.RecentlyProductsAdapter(recyclerView_recently, recently_view_list);
        recyclerView_recently.setAdapter(adapter1);

        recyclerView_mostly= (RecyclerView) findViewById(R.id.recyclerview_mostlyviewed);
        RecyclerView.LayoutManager recyclerViewLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView_mostly.setLayoutManager(recyclerViewLayoutManager2);
        adapter2 = new RecommendActivity.MostlyProductsAdapter(recyclerView_mostly, mostly_viewed_list);
        recyclerView_mostly.setAdapter(adapter2);
    }

    public static class RecentlyProductsAdapter
            extends RecyclerView.Adapter<RecommendActivity.RecentlyProductsAdapter.ViewHolder> {

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

        public RecentlyProductsAdapter(RecyclerView recyclerView, ArrayList<ProductInfo> items) {
            list = items;
            mRecyclerView = recyclerView;
        }

        @Override
        public RecommendActivity.RecentlyProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new RecommendActivity.RecentlyProductsAdapter.ViewHolder(view);
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
        public void onBindViewHolder(final RecommendActivity.RecentlyProductsAdapter.ViewHolder holder, final int position) {
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

    class RecentProductAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Call<ResponseBody> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    .recently_viewed(prefManager.getUserName());
            try {
                JSONObject jsonObject = (JSONObject) new JSONObject(call.execute().body().string());
                int count = jsonObject.getInt("count");
                if (count != 0) {
                    JSONArray recently_prod_json_array = jsonObject.getJSONArray("results");
                    for (int i = 0; i < recently_prod_json_array.length(); i++) {
                        JSONObject temp = (JSONObject) recently_prod_json_array.get(i);
                        String temp_url = String.valueOf(temp.getInt("product"));
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

                        recently_view_list.add(new ProductInfo(temp_url, pName, pUrl, pPrice));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            adapter1.notifyDataSetChanged();
        }
    }

    public static class MostlyProductsAdapter
            extends RecyclerView.Adapter<RecommendActivity.MostlyProductsAdapter.ViewHolder> {

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

        public MostlyProductsAdapter(RecyclerView recyclerView, ArrayList<ProductInfo> items) {
            list = items;
            mRecyclerView = recyclerView;
        }

        @Override
        public RecommendActivity.MostlyProductsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new RecommendActivity.MostlyProductsAdapter.ViewHolder(view);
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
        public void onBindViewHolder(final RecommendActivity.MostlyProductsAdapter.ViewHolder holder, final int position) {
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

    class MostProductAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Call<ResponseBody> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    .mostly_viewed();
            try {
                JSONObject jsonObject = (JSONObject) new JSONObject(call.execute().body().string());
                int count = jsonObject.getInt("count");
                if (count != 0) {
                    JSONArray recently_prod_json_array = jsonObject.getJSONArray("results");
                    for (int i = 0; i < recently_prod_json_array.length(); i++) {
                        JSONObject temp = (JSONObject) recently_prod_json_array.get(i);
                        String temp_url = String.valueOf(temp.getInt("product"));
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

                        mostly_viewed_list.add(new ProductInfo(temp_url, pName, pUrl, pPrice));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            adapter2.notifyDataSetChanged();
        }
    }

}
