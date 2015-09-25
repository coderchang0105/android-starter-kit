/**
 * Created by YuGang Yang on September 25, 2015.
 * Copyright 2007-2015 Laputapp.com. All rights reserved.
 */
package com.smartydroid.android.starter.kit.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.carlosdelachica.easyrecycleradapters.adapter.EasyRecyclerAdapter;
import com.smartydroid.android.starter.kit.R;
import com.smartydroid.android.starter.kit.contracts.Pagination.PageCallback;
import com.smartydroid.android.starter.kit.contracts.Pagination.Paginator;
import com.smartydroid.android.starter.kit.network.PagePaginator;
import com.smartydroid.android.starter.kit.network.Result;
import com.smartydroid.android.starter.kit.utilities.RecyclerViewHandler;
import com.smartydroid.android.starter.kit.utilities.ViewHandler;
import com.smartydroid.android.starter.kit.utilities.ViewUtils;
import com.smartydroid.android.starter.kit.widget.LoadMoreView;
import com.smartydroid.android.starter.kit.widget.LoadingLayout;
import java.util.List;

public abstract class RecyclerViewFragment<E> extends BaseFragment
    implements PageCallback<List<E>>, Paginator.Emitter<E>,
    SwipeRefreshLayout.OnRefreshListener, ViewHandler.OnScrollBottomListener, View.OnClickListener {

  private LoadingLayout mLoadingLayout;
  private SwipeRefreshLayout mSwipeRefreshLayout;
  private RecyclerView mRecyclerView;
  private EasyRecyclerAdapter mRecyclerAdapter;

  private PagePaginator<E> mPagePaginator;

  private LoadMoreView mLoadMoreView;
  private RecyclerViewHandler mRecyclerViewHandler;

  public abstract void bindViewHolders(EasyRecyclerAdapter adapter);

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mPagePaginator = new PagePaginator.Builder<E>()
        .setEmitter(this)
        .setPageCallback(this).build();

    mLoadMoreView = new LoadMoreView();
    mRecyclerViewHandler = new RecyclerViewHandler();
  }

  @Override protected int getFragmentLayout() {
    return R.layout.content_recycler_view;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mLoadingLayout = ViewUtils.getView(view, R.id.container_loading_layout);
    mSwipeRefreshLayout = mLoadingLayout.getContentView();
    mRecyclerView = ViewUtils.getView(mSwipeRefreshLayout, android.R.id.list);
    mSwipeRefreshLayout.setOnRefreshListener(this);

    setupRecyclerView();
    updateView();
  }

  @Override public void onResume() {
    super.onResume();
    if (! mPagePaginator.dataHasLoaded()) {
      mPagePaginator.refresh();
    }
  }

  @Override public void onPause() {
    super.onPause();
    if (mPagePaginator.isLoading()) {
      mPagePaginator.cancel();
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
  }

  /**
   * setup
   */
  private void setupRecyclerView() {
    mRecyclerAdapter = new EasyRecyclerAdapter(getContext());
    bindViewHolders(mRecyclerAdapter);

    mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    mRecyclerViewHandler.handleSetAdapter(mRecyclerView, mRecyclerAdapter, mLoadMoreView, this);
    mRecyclerViewHandler.setOnScrollBottomListener(mRecyclerView, this);
  }

  private void updateView() {
    if (!isEmpty()) {
      mLoadingLayout.showContentView();
    } else if (! mPagePaginator.dataHasLoaded()) {
      mLoadingLayout.showLoadingView();
    } else if (mPagePaginator.hasError()) {
      mLoadingLayout.showErrorView();
    } else {
      mLoadingLayout.showEmptyView();
    }
  }

  public boolean isEmpty() {
    return mPagePaginator.isEmpty();
  }

  @Override public void onRefresh() {
    if (! mPagePaginator.isLoading()) {
      mPagePaginator.refresh();
    } else {
      mSwipeRefreshLayout.setRefreshing(false);
    }
  }

  @Override public E register(E item) {
    return item;
  }

  @Override public void beforeRefresh() {
  }

  @Override public void beforeLoadMore() {
  }

  @Override public void onRequestComplete(Result<List<E>> result) {
    mRecyclerAdapter.addAll(mPagePaginator.items());
  }

  @Override public void onRequestComplete(int code, String error) {

  }

  @Override public void onRequestFailure(Result<List<E>> result) {
  }

  @Override public void onRequestFailure(Throwable error) {

  }

  @Override public void onFinish() {
    if (mPagePaginator.isRefresh()) {
      mSwipeRefreshLayout.setRefreshing(false);
    }
    updateView();
  }

  /**
   * load more click
   * @param v
   */
  @Override public void onClick(View v) {
    mPagePaginator.loadMore();
  }

  @Override public void onScorllBootom() {
    if (mPagePaginator.canLoadMore()) {
      mPagePaginator.loadMore();
    }
  }
}
