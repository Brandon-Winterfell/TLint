package com.gzsll.hupu.ui.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.gzsll.hupu.Constants;
import com.gzsll.hupu.R;
import com.gzsll.hupu.bean.Thread;
import com.gzsll.hupu.components.storage.UserStorage;
import com.gzsll.hupu.db.Forum;
import com.gzsll.hupu.helper.ResourceHelper;
import com.gzsll.hupu.helper.SettingPrefHelper;
import com.gzsll.hupu.presenter.ThreadListPresenter;
import com.gzsll.hupu.ui.BaseFragment;
import com.gzsll.hupu.ui.activity.LoginActivity;
import com.gzsll.hupu.ui.activity.PostActivity;
import com.gzsll.hupu.ui.activity.ThreadListActivity;
import com.gzsll.hupu.ui.adapter.ThreadListAdapter;
import com.gzsll.hupu.ui.view.ThreadListView;
import com.gzsll.hupu.widget.LoadMoreRecyclerView;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sll on 2016/3/9.
 */
public class ThreadListFragment extends BaseFragment implements ThreadListView, SwipeRefreshLayout.OnRefreshListener, AppBarLayout.OnOffsetChangedListener, LoadMoreRecyclerView.LoadMoreListener {

    @Inject
    ThreadListPresenter mPresenter;
    @Inject
    SettingPrefHelper mSettingPrefHelper;
    @Inject
    ThreadListAdapter mAdapter;
    @Inject
    ResourceHelper mResourceHelper;
    @Inject
    Activity mActivity;
    @Inject
    UserStorage mUserStorage;


    @Bind(R.id.backdrop)
    SimpleDraweeView backdrop;
    @Bind(R.id.tvSubTitle)
    TextView tvSubTitle;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @Bind(R.id.appbar)
    AppBarLayout appbar;
    @Bind(R.id.recyclerView)
    LoadMoreRecyclerView recyclerView;
    @Bind(R.id.refreshLayout)
    SwipeRefreshLayout refreshLayout;
    @Bind(R.id.floatingAttention)
    FloatingActionButton floatingAttention;
    @Bind(R.id.floatingPost)
    FloatingActionButton floatingPost;
    @Bind(R.id.floatingSwitch)
    FloatingActionButton floatingSwitch;
    @Bind(R.id.floatingRefresh)
    FloatingActionButton floatingRefresh;
    @Bind(R.id.floatingMenu)
    FloatingActionMenu floatingMenu;
    @Bind(R.id.frameLayout)
    FrameLayout frameLayout;



    public static ThreadListFragment newInstance(String fid) {
        ThreadListFragment mFragment = new ThreadListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("fid", fid);
        mFragment.setArguments(bundle);
        return mFragment;
    }


    private String fid;
    private boolean isAttention;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    public int initContentView() {
        return R.layout.fragment_thread;
    }

    @Override
    public void getBundle(Bundle bundle) {
        fid = bundle.getString("fid");
    }

    @Override
    public void initUI(View view) {
        ButterKnife.bind(this, view);
        mPresenter.attachView(this);
        ((ThreadListActivity) mActivity).initToolBar(toolbar);
        initFloatingButton();
        attachPostButtonToRecycle();
        refreshLayout.setOnRefreshListener(this);
        appbar.addOnOffsetChangedListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLoadMoreListener(this);
    }


    private void initFloatingButton() {
        mResourceHelper.setFabBtnColor(mActivity, floatingPost);
        mResourceHelper.setFabBtnColor(mActivity, floatingSwitch);
        mResourceHelper.setFabBtnColor(mActivity, floatingRefresh);
        mResourceHelper.setFabBtnColor(mActivity, floatingAttention);
        mResourceHelper.setFabMenuColor(mActivity, floatingMenu);
        if (mSettingPrefHelper.getThreadSort().equals(Constants.THREAD_TYPE_HOT)) {
            floatingSwitch.setLabelText("按发帖时间排序");
        } else {
            floatingSwitch.setLabelText("按回帖时间排序");
        }
    }


    private void attachPostButtonToRecycle() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (Math.abs(dy) > 4) {
                    if (dy > 0) {
                        floatingMenu.hideMenuButton(true);
                    } else {
                        floatingMenu.showMenuButton(true);
                    }
                }
            }
        });
    }

    @Override
    public void initData() {
        mPresenter.onThreadReceive(fid, mSettingPrefHelper.getThreadSort(), null);
    }

    @Override
    public void showLoading() {
        showProgress(true);
    }

    @Override
    public void hideLoading() {
        showContent(true);
    }

    @Override
    public void renderThreadInfo(Forum forum) {
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(forum.getBackImg()))
                .setResizeOptions(
                        new ResizeOptions(500, 500))
                .build();
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(backdrop.getController())
                .setAutoPlayAnimations(true)
                .build();
        backdrop.setController(draweeController);
        collapsingToolbar.setTitle(forum.getName());
        tvSubTitle.setText(forum.getDescription());
    }

    @Override
    public void renderThreads(List<Thread> threads) {
        mAdapter.bind(threads);
    }

    @Override
    public void onLoadCompleted(boolean hasMore) {
        recyclerView.notifyMoreFinish(hasMore);
    }

    @Override
    public void onRefreshCompleted() {
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void attendStatus(boolean isAttention) {
        this.isAttention = isAttention;
        if (!isAttention) {
            floatingAttention.setImageResource(R.drawable.ic_menu_add);
            floatingAttention.setLabelText("添加关注");
        } else {
            floatingAttention.setImageResource(R.drawable.ic_minus);
            floatingAttention.setLabelText("取消关注");
        }

    }

    @Override
    public void onError(String error) {
        setErrorText(error);
        showError(true);
    }

    @Override
    public void onEmpty() {
        setEmptyText("暂无帖子");
        showEmpty(true);
    }

    @Override
    public void onScrollToTop() {
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onFloatingVisibility(int visibility) {
        floatingMenu.setVisibility(visibility);
    }



    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0 || verticalOffset == -appBarLayout.getTotalScrollRange()) {
            refreshLayout.setEnabled(true);
        } else {
            refreshLayout.setEnabled(false);
        }
    }

    @Override
    public void onRefresh() {
        mPresenter.onRefresh();
    }

    @Override
    public void onReloadClicked() {
        mPresenter.onReload();
    }


    @OnClick(R.id.floatingAttention)
    void floatingAttention() {
        if (isLogin()) {
            if (isAttention) {
                mPresenter.delAttention();
            } else {
                mPresenter.addAttention();
            }
        }
        floatingMenu.toggle(true);
    }

    @OnClick(R.id.floatingPost)
    void floatingPost() {
        if (isLogin()) {
            PostActivity.startActivity(mActivity, Constants.TYPE_POST, fid, "", "", "");
        }
        floatingMenu.toggle(true);
    }

    private boolean isLogin() {
        if (!mUserStorage.isLogin()) {
            LoginActivity.startActivity(mActivity);
            return false;
        }
        return true;
    }

    @OnClick(R.id.floatingRefresh)
    void floatingRefresh() {
        mPresenter.onRefresh();
        floatingMenu.toggle(true);
    }


    @OnClick(R.id.floatingSwitch)
    void floatingSwitch() {
        if (floatingSwitch.getLabelText().equals("按回帖时间排序")) {
            mPresenter.onThreadReceive(fid, Constants.THREAD_TYPE_HOT, null);
            floatingSwitch.setLabelText("按发帖时间排序");
        } else {
            mPresenter.onThreadReceive(fid, Constants.THREAD_TYPE_NEW, null);
            floatingSwitch.setLabelText("按回帖时间排序");
        }
        floatingMenu.toggleMenuButton(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_thread, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);//在菜单中找到对应控件的item
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mPresenter.onStartSearch(query, 1);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {//设置打开关闭动作监听
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mPresenter.onThreadReceive(fid, mSettingPrefHelper.getThreadSort(), null);
                return true;
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void onLoadMore() {
        mPresenter.onLoadMore();
    }
}
