package com.allandroidprojects.ecomsample.product;

import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
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
import com.allandroidprojects.ecomsample.fragments.ViewPagerActivity;
import com.allandroidprojects.ecomsample.notification.NotificationCountSetClass;
import com.allandroidprojects.ecomsample.options.CartListActivity;
import com.allandroidprojects.ecomsample.startup.MainActivity;
import com.allandroidprojects.ecomsample.utility.ImageUrlUtils;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailsActivity extends AppCompatActivity {
    int imagePosition;
    String imageUri;
    ProductInfo productInfo;
    String itemName;
    String productId;
    String productPrice = "Rs. 150";
    String productDesc;

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

        //Getting image uri from previous screen
        if (getIntent() != null) {
            productId = getIntent().getStringExtra(ImageListFragment.ITEM_ID);
            imagePosition = getIntent().getIntExtra(ImageListFragment.STRING_IMAGE_POSITION, 0);
        }

        Call<ResponseBody> call = RetrofitClient
                .getInstance()
                .getApi()
                .get_product_details(productId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() != 404) {
                    try {
                        JSONObject jsonObject = (JSONObject) new JSONObject(response.body().string());
                        itemName = jsonObject.getString("title");
                        productDesc = jsonObject.getString("description");
                        if (jsonObject.getJSONArray("images").length() != 0) {
                            JSONObject temp1 = (JSONObject) jsonObject.getJSONArray("images").get(0);
                            imageUri = temp1.getString("original");
                        } else
                            imageUri = "https://www.azfinesthomes.com/assets/images/image-not-available.jpg";

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
                } else {
                    Toast.makeText(ItemDetailsActivity.this, "Sorry! The product is not available", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ItemDetailsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ItemDetailsActivity.this, ViewPagerActivity.class);
                intent.putExtra("position", imagePosition);
                startActivity(intent);

            }
        });

        textViewAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
                imageUrlUtils.addCartListImageUri(imageUri);
                Toast.makeText(ItemDetailsActivity.this, "Item added to cart.", Toast.LENGTH_SHORT).show();
                MainActivity.notificationCountCart++;
                NotificationCountSetClass.setNotifyCount(MainActivity.notificationCountCart);
            }
        });

        textViewBuyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
                imageUrlUtils.addCartListImageUri(imageUri);
                MainActivity.notificationCountCart++;
                NotificationCountSetClass.setNotifyCount(MainActivity.notificationCountCart);
                startActivity(new Intent(ItemDetailsActivity.this, CartListActivity.class));

            }
        });

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
