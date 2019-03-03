/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.allandroidprojects.ecomsample.fragments;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.options.SearchResultActivity;
import com.allandroidprojects.ecomsample.product.ItemDetailsActivity;
import com.allandroidprojects.ecomsample.product.ProductInfo;
import com.allandroidprojects.ecomsample.startup.MainActivity;
import com.allandroidprojects.ecomsample.startup.WelcomeActivity;
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

import static com.allandroidprojects.ecomsample.startup.MainActivity.categoryList;


public class ImageListFragment extends Fragment {

    public static final String ITEM_ID = "ItemId";
    public static final String STRING_IMAGE_URI = "ImageUri";
    public static final String STRING_IMAGE_POSITION = "ImagePosition";
    private static MainActivity mActivity;
    private static RecyclerView rv;

    private static int category_position;
    public static Boolean isLoading[] = {false, false, false, false};
    public static SimpleStringRecyclerViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rv = (RecyclerView) inflater.inflate(R.layout.layout_recylerview_list, container, false);
        setupRecyclerView();    //Sets the recycler view for the first time
        initScrollListener();   //Listener to implement paging after reaching end of list in recyclerview
        return rv;
    }

    //pagination
    private void initScrollListener() {
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) rv.getLayoutManager();

                if (!isLoading[category_position]) {
                    int arr[] = new int[2];
                    if (staggeredGridLayoutManager != null) {
                        arr = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(arr);
                        if (Math.max(arr[0], arr[1]) == MainActivity.lists[category_position].size() - 1) {
                            callApi(true, category_position);
                            isLoading[category_position] = true;
                            Toast.makeText(mActivity, "Loading...", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
            }
        });
    }


    private void setupRecyclerView() {
        category_position = ImageListFragment.this.getArguments().getInt("type") - 1;
//        Toast.makeText(mActivity, ""+category_position, Toast.LENGTH_SHORT).show();
        adapter = new SimpleStringRecyclerViewAdapter(rv, MainActivity.lists[category_position]);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
    }

    public static void callApi(final Boolean isLoadMore, final int position) {

        Call<ResponseBody> call = RetrofitClient
                .getInstance()
                .getApi()
                .get_products(MainActivity.page[position], categoryList[position]);

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

                                MainActivity.lists[position].add(product);
                            }
                            adapter.notifyDataSetChanged();
                            MainActivity.page[position] += 1;
                            if (isLoadMore)
                                isLoading[position] = false;
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
                Toast.makeText(mActivity, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

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

        public SimpleStringRecyclerViewAdapter(RecyclerView recyclerView, ArrayList<ProductInfo> items) {
            list = items;
            mRecyclerView = recyclerView;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            if (holder.mImageView.getController() != null) {
                holder.mImageView.getController().onDetach();
            }
            if (holder.mImageView.getTopLevelDrawable() != null) {
                holder.mImageView.getTopLevelDrawable().setCallback(null);
//                ((BitmapDrawable) holder.mImageView.getTopLevelDrawable()).getBitmap().recycle();
            }
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            if (list.size() > 0) {

                final Uri uri = Uri.parse(list.get(position).getImg_url());

                holder.mImageView.setImageURI(uri);
                holder.mProductName.setText(list.get(position).getProduct_title());
                holder.mProductPrice.setText(list.get(position).getProduct_price());

                holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, ItemDetailsActivity.class);
                        intent.putExtra(ITEM_ID, list.get(position).getProduct_id());
                        intent.putExtra(STRING_IMAGE_POSITION, position);
                        mActivity.startActivity(intent);
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
