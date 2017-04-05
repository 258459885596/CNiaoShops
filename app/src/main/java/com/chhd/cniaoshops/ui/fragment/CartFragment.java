package com.chhd.cniaoshops.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chhd.cniaoshops.R;
import com.chhd.cniaoshops.bean.ShoppingCart;
import com.chhd.cniaoshops.biz.CartBiz;
import com.chhd.cniaoshops.ui.adapter.CartAdapter;
import com.chhd.cniaoshops.ui.base.BaseFragment;
import com.chhd.cniaoshops.ui.widget.CnToolbar;
import com.chhd.per_library.util.UiUtils;
import com.liaoinstan.springview.container.AliHeader;
import com.liaoinstan.springview.container.DefaultHeader;
import com.liaoinstan.springview.container.MeituanHeader;
import com.liaoinstan.springview.widget.SpringView;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by CWQ onClickListener1 2016/10/24.
 */
public class CartFragment extends BaseFragment {

    @BindView(R.id.refresh_layout)
    SpringView springView;
    @BindView(R.id.rv_shopping_cart)
    RecyclerView rvShoppingcart;
    @BindView(R.id.check_box)
    CheckBox checkbox;
    @BindView(R.id.btn_settlement)
    Button btnSettlement;
    @BindView(R.id.tv_total_price)
    TextView tvTotalPrice;

    private final int ACTION_NORMAL = 0;
    private final int ACTION_EDIT = 1;

    private List<ShoppingCart> carts = new ArrayList<>();
    private CartAdapter adapter;
    private Button rightButton;

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_cart;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();

        initActionBar(false);

        showData();

        showTotalPrice();
    }

    private void initView() {
        DefaultHeader defaultHeader = new DefaultHeader(getActivity());
        springView.setHeader(defaultHeader);
        springView.setType(SpringView.Type.FOLLOW);
        springView.setListener(onFreshListener);
        springView.post(new Runnable() {
            @Override
            public void run() {
                springView.callFresh();
            }
        });

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiUtils.dp2px(50));
        View footerView = new View(getActivity());
        footerView.setLayoutParams(params);

        adapter = new CartAdapter(carts, onClickListener);
        adapter.addFooterView(footerView);
        adapter.setOnItemChildClickListener(onItemChildClickListener);

        rvShoppingcart.setAdapter(adapter);
        rvShoppingcart.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvShoppingcart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SwipeMenuLayout viewCache = new SwipeMenuLayout(getActivity()).getViewCache();
                if (viewCache != null) {
                    viewCache.smoothClose();
                }
                return false;
            }
        });
    }

    private SpringView.OnFreshListener onFreshListener = new SpringView.OnFreshListener() {
        @Override
        public void onRefresh() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    springView.onFinishFreshAndLoad();
                }
            }, DELAYMILLIS_FOR_RQUEST_FINISH);
        }

        @Override
        public void onLoadmore() {

        }
    };

    private BaseQuickAdapter.OnItemChildClickListener onItemChildClickListener = new BaseQuickAdapter.OnItemChildClickListener() {
        @Override
        public boolean onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
            switch (view.getId()) {
                case R.id.check_box:
                    ShoppingCart cart = carts.get(position);
                    cart.setChecked(!cart.isChecked());
                    adapter.notifyItemChanged(position);
                    checkbox.setChecked(isCheckAll());
                    break;
                case R.id.btn_delete:
                    new CartBiz().delete(carts.get(position));
                    adapter.remove(position);
                    break;
            }
            showTotalPrice();
            return false;
        }
    };

    private void showTotalPrice() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        float total = getTotalPrice();
        tvTotalPrice.setText(String.format(getString(R.string.total), numberFormat.format(total)));
    }

    private float getTotalPrice() {
        float sum = 0;
        for (ShoppingCart cart : carts) {
            if (cart.isChecked())
                sum += cart.getCount() * cart.getPrice();
        }
        return sum;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        initActionBar(hidden);
        if (!hidden) {
            showData();
        }
    }

    private void initActionBar(boolean hidden) {
        CnToolbar toolbar = (CnToolbar) getActivity().findViewById(R.id.cn_tool_bar);
        rightButton = toolbar.getRightButton();
        rightButton.setOnClickListener(onClickListener);
        rightButton.setText(R.string.edit);
        rightButton.setTag(ACTION_NORMAL);
        if (!hidden) {
            toolbar.hideSearchView();
            toolbar.setTitle(R.string.tab_shopping_cart);
            rightButton.setVisibility(View.VISIBLE);
        } else {
            toolbar.showSearchView();
            toolbar.hideTitleView();
            rightButton.setVisibility(View.INVISIBLE);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_right:
                    int action = (int) v.getTag();
                    if (action == ACTION_NORMAL) {
                        showEditControl();
                    } else {
                        hideEditControl();
                    }
                    break;
            }
            showTotalPrice();
        }
    };

    /**
     * 开启编辑操作
     */
    private void showEditControl() {
        rightButton.setTag(ACTION_EDIT);
        rightButton.setText(R.string.complete);
        checkbox.setChecked(false);
        btnSettlement.setText(R.string.delete);
        checkAll(false);
    }

    /**
     * 关闭编辑操作
     */
    private void hideEditControl() {
        rightButton.setTag(ACTION_NORMAL);
        rightButton.setText(R.string.edit);
        checkbox.setChecked(true);
        btnSettlement.setText(R.string.go_settlement);
        checkAll(true);
    }

    private void checkAll(boolean isChecked) {
        for (ShoppingCart cart : carts) {
            cart.setChecked(isChecked);
        }
        adapter.notifyDataSetChanged();
    }

    private void showData() {
        hideEditControl();
        carts.clear();
        carts.addAll(new CartBiz().getAll());
        adapter.notifyDataSetChanged();
    }

    @OnClick({R.id.check_box, R.id.btn_settlement})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_box:
                checkAll(checkbox.isChecked());
                break;
            case R.id.btn_settlement:
                int action = (int) rightButton.getTag();
                if (action == ACTION_NORMAL) {
                } else {
                    delCarts();
                }
                break;
        }
        showTotalPrice();
    }

    private boolean isCheckAll() {
        for (ShoppingCart cart : carts) {
            if (!cart.isChecked()) {
                return false;
            }
        }
        return true;
    }

    private void delCarts() {
        for (Iterator iterator = carts.iterator(); iterator.hasNext(); ) {
            ShoppingCart cart = (ShoppingCart) iterator.next();
            if (cart.isChecked()) {
                int position = carts.indexOf(cart);
                iterator.remove();
                adapter.notifyItemRemoved(position);
            }
        }
    }
}
