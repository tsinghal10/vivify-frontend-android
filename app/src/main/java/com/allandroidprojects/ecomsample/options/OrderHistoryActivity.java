package com.allandroidprojects.ecomsample.options;

import android.content.Context;
import android.content.Intent;
import android.gesture.Prediction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.product.ItemDetailsActivity;
import com.allandroidprojects.ecomsample.product.ProductInfo;
import com.allandroidprojects.ecomsample.utility.PrefManager;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {
    static Context mContext;
    PrefManager prefManager;
    ArrayList<ProductInfo> order_list;
    String id, price, order_list_id;
    static String ORDER_LIST = "OrderList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        mContext = OrderHistoryActivity.this;
        order_list = new ArrayList<>();
        prefManager = new PrefManager(this);

        Call<ResponseBody> call = RetrofitClient
                .getInstance()
                .getApi()
                .order_list(prefManager.getUserName());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = (JSONObject) new JSONObject(response.body().string());
                    int count = jsonObject.getInt("count");
                    if (count != 0) {
                        JSONArray jsonArray = (JSONArray) jsonObject.getJSONArray("results");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                            id = jsonObject1.getString("number");
                            price = jsonObject1.getString("currency") + " " + jsonObject1.getString("total_incl_tax");
                            order_list_id = jsonObject1.getString("lines");
                            ProductInfo productInfo = new ProductInfo(id, price, order_list_id);
                            order_list.add(productInfo);
                        }
                        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.order_recyclerview);
                        RecyclerView.LayoutManager recyclerViewLayoutManager = new LinearLayoutManager(mContext);
                        recyclerView.setLayoutManager(recyclerViewLayoutManager);
                        recyclerView.setAdapter(new OrderHistoryActivity.OrderListAdapter(recyclerView, order_list));
                    } else {
                        Toast.makeText(mContext, "No Orders Found", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static class OrderListAdapter extends RecyclerView.Adapter<OrderHistoryActivity.OrderListAdapter.ViewHolder> {

        private ArrayList<ProductInfo> mOrderList;
        private RecyclerView mRecyclerView;

        public OrderListAdapter(RecyclerView recyclerView, ArrayList<ProductInfo> orderlist) {
            mRecyclerView = recyclerView;
            mOrderList = orderlist;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_order_list, parent, false);
            return new OrderHistoryActivity.OrderListAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.id.setText(holder.id.getText() + " " + mOrderList.get(position).getOrder_id());
            holder.price.setText(holder.price.getText() + " " + mOrderList.get(position).getOrder_price());

            holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(mContext, OrderItemList.class);
//                    Intent intent = new Intent(this, ItemDetailsActivity.class);
                    i.putExtra(ORDER_LIST, mOrderList.get(position).getOrder_list());
                    mContext.startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mOrderList == null ? 0 : mOrderList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final LinearLayout mLayoutItem;
            public final TextView id, price;
//            mLayoutRemove, mLayoutEdit;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mLayoutItem = (LinearLayout) view.findViewById(R.id.order_item);
                id = (TextView) mLayoutItem.findViewById(R.id.order_id);
                price = (TextView) mLayoutItem.findViewById(R.id.order_price);
//                mLayoutRemove = (LinearLayout) view.findViewById(R.id.layout_item_remove);
//                mLayoutEdit = (LinearLayout) view.findViewById(R.id.layout_item_edit);
            }
        }
    }
}
