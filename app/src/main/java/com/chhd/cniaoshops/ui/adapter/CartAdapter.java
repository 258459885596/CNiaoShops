package com.chhd.cniaoshops.ui.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chhd.cniaoshops.R;
import com.chhd.cniaoshops.bean.ShoppingCart;
import com.chhd.cniaoshops.biz.CartBiz;
import com.chhd.cniaoshops.ui.base.SimpleAdapter;
import com.chhd.cniaoshops.ui.widget.NumberAddSubView;
import com.chhd.per_library.ui.base.SimpleHolder;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;

import butterknife.BindView;

/**
 * Created by CWQ on 2017/4/4.
 */

public class CartAdapter extends SimpleAdapter<ShoppingCart, BaseViewHolder> {


    private NumberFormat numberFormat;
    private View.OnClickListener numberViewButtonClickListener;

    public CartAdapter(List<ShoppingCart> data, View.OnClickListener numberViewButtonClickListener) {
        super(R.layout.list_item_cart, data);

        this.numberViewButtonClickListener = numberViewButtonClickListener;

        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    @Override
    protected void convert(BaseViewHolder helper, final ShoppingCart item) {

        Holder holder = new Holder(helper.itemView);
        holder.checkBox.setChecked(item.isChecked());
        Glide
                .with(mContext)
                .load(item.getImgUrl())
                .into(holder.ivPic);
        holder.tvTitle.setText(item.getName());
        holder.tvPrice.setText(String.format("￥ %s", numberFormat.format(item.getPrice()).toString()));
        holder.numberView.setValue(item.getCount());
        holder.numberView.setOnButtonClickListener(new NumberAddSubView.OnButtonClickListener() {
            @Override
            public void onButtonAddClick(View view, int value) {
                item.setCount(value);
                new CartBiz().update(item);
                numberViewButtonClickListener.onClick(view);
            }

            @Override
            public void onButtonSubClick(View view, int value) {
                item.setCount(value);
                new CartBiz().update(item);
                numberViewButtonClickListener.onClick(view);
            }
        });
        helper.addOnClickListener(R.id.check_box);
        helper.addOnClickListener(R.id.btn_delete);
    }

    class Holder extends SimpleHolder {

        @BindView(R.id.check_box)
        CheckBox checkBox;
        @BindView(R.id.iv_pic)
        ImageView ivPic;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_price)
        TextView tvPrice;
        @BindView(R.id.number_view)
        NumberAddSubView numberView;

        public Holder(View itemView) {
            super(itemView);
        }
    }
}
