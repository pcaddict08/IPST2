/*
 * ****************************************************************************
 * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                  *
 * Permission is hereby granted, free of charge, to any person obtaining a    *
 * copy of this software and associated documentation files (the "Software"), *
 * to deal in the Software without restriction, including without limitation  *
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,   *
 * and/or sell copies of the Software, and to permit persons to whom the      *
 * Software is furnished to do so, subject to the following conditions:       *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included in *
 * all copies or substantial portions of the Software.                        *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE*
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER     *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING    *
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER        *
 * DEALINGS IN THE SOFTWARE.                                                  *
 ******************************************************************************/

package com.einzig.ipst2.billing;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.Sku;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.einzig.ipst2.CheckoutApplication;
import com.einzig.ipst2.R;
import com.einzig.ipst2.util.Logger;
import com.einzig.ipst2.util.ThemeHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Created by Steven Foskett on 7/25/2017.
 */

/**
 * Shows a list of SKUs available for purchase. User can purchase/consume an item by selecting it
 * from the list. Purchased items are strikethroughed.
 */
public class SkusActivity extends AppCompatActivity {

    @BindView(R.id.recycler)
    RecyclerView mRecycler;
    private ActivityCheckout mCheckout;
    private InventoryCallback mInventoryCallback;
    private ProgressDialog progressDialog;
    private static List<String> getInAppSkus() {
        final List<String> skus = new ArrayList<>();
        skus.addAll(Arrays.asList("000001", "000003", "000005"));
        Logger.d("Skus: " + skus.toString());
        return skus;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_skus);
        ButterKnife.bind(this);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null){
            supportActionBar.setTitle(R.string.donate);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        ThemeHelper.initActionBar(getSupportActionBar());
        progressDialog = new ProgressDialog(this, ThemeHelper.getDialogTheme(this));
        progressDialog.setTitle(getString(R.string.loadingproducts));
        progressDialog.show();
        final Adapter adapter = new Adapter();
        mInventoryCallback = new InventoryCallback(adapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(adapter);

        final Billing billing = CheckoutApplication.get(this).getBilling();
        mCheckout = Checkout.forActivity(this, billing);
        mCheckout.start();
        reloadInventory();
    }

    private void reloadInventory() {
        final Inventory.Request request = Inventory.Request.create();
        request.loadAllPurchases();
        request.loadSkus(ProductTypes.IN_APP, getInAppSkus());
        mCheckout.loadInventory(request, mInventoryCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCheckout.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mCheckout.stop();
        super.onDestroy();
    }

    private void purchase(Sku sku) {
        final RequestListener<Purchase> listener = makeRequestListener();
        mCheckout.startPurchaseFlow(sku, null, listener);
    }

    /**
     * @return {@link RequestListener} that reloads inventory when the action is finished
     */
    private <T> RequestListener<T> makeRequestListener() {
        return new RequestListener<T>() {
            @Override
            public void onSuccess(@Nonnull T result) {
                reloadInventory();
            }

            @Override
            public void onError(int response, @Nonnull Exception e) {
                reloadInventory();
            }
        };
    }

    private void consume(final Purchase purchase) {
        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(@Nonnull BillingRequests requests) {
                requests.consume(purchase.token, makeRequestListener());
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final Adapter mAdapter;
        @BindView(R.id.sku_title)
        TextView mTitle;
        @BindView(R.id.sku_description)
        TextView mDescription;
        @BindView(R.id.sku_price)
        TextView mPrice;
        @BindView(R.id.sku_icon)
        ImageView mIcon;

        @Nullable
        private Sku mSku;

        ViewHolder(View view, Adapter adapter) {
            super(view);
            mAdapter = adapter;
            ButterKnife.bind(this, view);

            view.setOnClickListener(this);
        }

        private static void strikeThrough(TextView view, boolean strikeThrough) {
            int flags = view.getPaintFlags();
            if (strikeThrough) {
                flags |= Paint.STRIKE_THRU_TEXT_FLAG;
            } else {
                flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
            }
            view.setPaintFlags(flags);
        }

        void onBind(Sku sku, boolean purchased) {
            mSku = sku;
            mTitle.setText(getTitle(sku));
            mDescription.setText(sku.description);
            strikeThrough(mTitle, purchased);
            strikeThrough(mDescription, purchased);
            mPrice.setText(sku.price);
            mIcon.setImageDrawable(ContextCompat.getDrawable(mIcon.getContext(), R.drawable.ic_monetization_on_black_24dp));
        }

        /**
         * @return SKU title without application name that is automatically added by Play Services
         */
        private String getTitle(Sku sku) {
            final int i = sku.title.indexOf("(");
            if (i > 0) {
                return sku.title.substring(0, i);
            }
            return sku.title;
        }

        @Override
        public void onClick(View v) {
            if (mSku == null) {
                return;
            }
            mAdapter.onClick(mSku);
        }
    }

    /**
     * Updates {@link Adapter} when {@link Inventory.Products} are loaded.
     */
    private class InventoryCallback implements Inventory.Callback {
        private final Adapter mAdapter;

        InventoryCallback(Adapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onLoaded(@Nonnull Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            if (!product.supported) {
                return;
            }
            mAdapter.update(product);
            SkusActivity.this.progressDialog.dismiss();
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private final LayoutInflater mInflater = LayoutInflater.from(SkusActivity.this);
        private Inventory.Product mProduct = Inventory.Products.empty().get(ProductTypes.IN_APP);

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.sku, parent, false);
            return new ViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Sku sku = mProduct.getSkus().get(position);
            holder.onBind(sku, mProduct.isPurchased(sku));
        }

        @Override
        public int getItemCount() {
            return mProduct.getSkus().size();
        }

        void update(Inventory.Product product) {
            mProduct = product;
            notifyDataSetChanged();
        }

        public void onClick(Sku sku) {
            final Purchase purchase = mProduct.getPurchaseInState(sku, Purchase.State.PURCHASED);
            if (purchase != null) {
                consume(purchase);
            } else {
                purchase(sku);
            }
        }
    }
}