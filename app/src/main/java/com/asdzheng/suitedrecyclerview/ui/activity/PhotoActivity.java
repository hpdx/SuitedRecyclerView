package com.asdzheng.suitedrecyclerview.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.asdzheng.layoutmanager.SuitUrlUtil;
import com.asdzheng.layoutmanager.SuitedItemDecoration;
import com.asdzheng.layoutmanager.SuitedLayoutManager;
import com.asdzheng.suitedrecyclerview.R;
import com.asdzheng.suitedrecyclerview.bean.NewChannelInfoDetailDto;
import com.asdzheng.suitedrecyclerview.bean.NewChannelInfoDto;
import com.asdzheng.suitedrecyclerview.http.GsonRequest;
import com.asdzheng.suitedrecyclerview.http.UrlUtil;
import com.asdzheng.suitedrecyclerview.ui.adapter.PhotosAdapter;
import com.asdzheng.suitedrecyclerview.ui.view.waveswiperefreshlayout.WaveSwipeRefreshLayout;
import com.asdzheng.suitedrecyclerview.utils.DisplayUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class PhotoActivity extends BaseActivity implements WaveSwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.recycler_channel_view)
    RecyclerView recyclerView;
    @Bind(R.id.wave_channel)
    WaveSwipeRefreshLayout waveChannel;

    int page = 1;
    RequestQueue queue;
    List<NewChannelInfoDetailDto> list;
    @Bind(R.id.toolbar_channel_photo)
    Toolbar toolbarChannelPhoto;
    private String nextStr = UrlUtil.SIGHT;
    private PhotosAdapter mPhotosAdapter;


    @NonNull
    private void requestData(final String next) {
        GsonRequest<NewChannelInfoDto> request = new GsonRequest<>(Request.Method.GET, UrlUtil.getBaseUrl(next), NewChannelInfoDto.class,
                new Response.Listener<NewChannelInfoDto>() {

                    @Override
                    public void onResponse(NewChannelInfoDto response) {
                        if (response.getData().getResults() != null) {
                            if (page == 1) {
                                mPhotosAdapter.clear();
                                mPhotosAdapter.bind(filterEmptyPhotos(response.getData().getResults()));
                            } else {
                                mPhotosAdapter.bind(filterEmptyPhotos(response.getData().getResults()));
                            }
                        }
                        nextStr = response.getData().getNext();
                        if (page == 1) {
                            waveChannel.setRefreshing(false);
                        } else {
                            waveChannel.setLoading(false);
                        }

                        page++;

                        if (mPhotosAdapter.getItemCount() > 0) {
                            waveChannel.setCanLoadMore(true);
                        } else {
                            waveChannel.setCanLoadMore(false);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (mPhotosAdapter.getItemCount() == 0) {
                    waveChannel.setCanLoadMore(false);
                }

                Toast.makeText(PhotoActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                Log.w("main", error.toString());
                waveChannel.setRefreshing(false);
                waveChannel.setLoading(false);

            }
        });
        request.setTag(this);
        queue.add(request);
    }

    private List<NewChannelInfoDetailDto> filterEmptyPhotos(List<NewChannelInfoDetailDto> results) {
        List<NewChannelInfoDetailDto> infos = new ArrayList<>();
        for (NewChannelInfoDetailDto info : results) {
            if (SuitUrlUtil.isNotEmpty(info.photo)) {
                infos.add(info);
            }
        }
        return infos;
    }

    private void setupRecyclerView() {
        mPhotosAdapter = new PhotosAdapter(list, this);
//        recyclerView.setAdapter(new ScaleInAnimationAdapter(mPhotosAdapter));
        recyclerView.setAdapter(mPhotosAdapter);
        SuitedLayoutManager layoutManager = new SuitedLayoutManager(mPhotosAdapter);
        recyclerView.setLayoutManager(layoutManager);
        //设置最大的图片显示高度，默认为600px
        layoutManager.setMaxRowHeight(getResources().getDisplayMetrics().heightPixels / 3);
        recyclerView.addItemDecoration(new SuitedItemDecoration(DisplayUtils.dpToPx(4.0f, this)));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Picasso.with(PhotoActivity.this).resumeTag(PhotoActivity.this);
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Picasso.with(PhotoActivity.this).pauseTag(PhotoActivity.this);
                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    Picasso.with(PhotoActivity.this).pauseTag(PhotoActivity.this);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        page = 1;
        nextStr = UrlUtil.SIGHT;
        requestData(nextStr);
    }

    @Override
    public void onLoad() {
        requestData(nextStr);
    }

    @Override
    protected int setLayout() {
        return R.layout.activity_channel_photo;
    }

    @Override
    protected void initViews() {
        int homepage_refresh_spacing = 40;

        waveChannel.setProgressViewOffset(false, -homepage_refresh_spacing * 2, homepage_refresh_spacing);
        waveChannel.setOnRefreshListener(this);
        waveChannel.setCanRefresh(true);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        setSupportActionBar(toolbarChannelPhoto);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarChannelPhoto.setTitle("美图");

        queue = Volley.newRequestQueue(this);
        list = new ArrayList<>();
        setupRecyclerView();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                waveChannel.setRefreshing(true);
                requestData(nextStr);
            }
        });
    }

    @Override
    protected void onDestroy() {
        queue.cancelAll(this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
