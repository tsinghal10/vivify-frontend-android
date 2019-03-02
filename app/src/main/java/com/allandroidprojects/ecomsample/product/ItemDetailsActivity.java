package com.allandroidprojects.ecomsample.product;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailsActivity extends AppCompatActivity {
    String imageUri;
    String itemName;
    String productId;
    String productPrice;
    String productDesc;
    String prodouctStock;
    String product_url;
    int quantity = 2;

    PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final SimpleDraweeView mImageView = (SimpleDraweeView) findViewById(R.id.image1);
        final TextView textViewItemName = (TextView) findViewById(R.id.item_name);
        final TextView textViewItemPrice = (TextView) findViewById(R.id.item_price);
        final LinearLayout linearLayoutWishList = (LinearLayout) findViewById(R.id.wish_list_layout);
        final ImageView imageWishListActive = (ImageView) linearLayoutWishList.findViewById(R.id.wish_list_active);
        final TextView textViewItemDesc = (TextView) findViewById(R.id.item_desc);
        final TextView textViewAddToCart = (TextView) findViewById(R.id.add_to_cart_button);
        final TextView textViewBuyNow = (TextView) findViewById(R.id.buy_now_button);
        final TextView textViewItemStock = (TextView) findViewById(R.id.item_stock);

        prefManager = new PrefManager(this);
        //Getting image uri from previous screen
        if (getIntent() != null) {
            productId = getIntent().getStringExtra(ImageListFragment.ITEM_ID);
//            Toast.makeText(this,"Produnct Found id= "+productId,Toast.LENGTH_LONG).show();
        }

        Call<ResponseBody> call = RetrofitClient
                .getInstance()
                .getApi()
                .get_product_details(productId, prefManager.getUserName());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response.code() != 404) {
                try {
                    JSONArray jsonArray = (JSONArray) new JSONArray(response.body().string());
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

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Uri uri = Uri.parse(imageUri);
                mImageView.setImageURI(uri);
                textViewItemName.setText(itemName);
                textViewItemPrice.setText(productPrice);
                textViewItemDesc.setText(productDesc);
                textViewItemPrice.setText(productPrice);
                textViewItemStock.setText(prodouctStock);
//                } else {
//                    Toast.makeText(ItemDetailsActivity.this, "Sorry! The product is not available", Toast.LENGTH_LONG).show();
//                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ItemDetailsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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
        linearLayoutWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
                imageUrlUtils.addWishlistImageUri(imageUri);
                imageWishListActive.setImageResource(R.drawable.ic_favorite_black_18dp);
                Toast.makeText(ItemDetailsActivity.this, "Item added to wishlist.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
}
