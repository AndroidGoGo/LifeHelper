package com.ycbjie.gank.view.fragment;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.ycbjie.gank.R;
import com.ycbjie.gank.api.GanKModel;
import com.ycbjie.library.base.mvp.BaseFragment;
import com.ycbjie.gank.view.activity.MyKnowledgeActivity;
import com.ycbjie.gank.view.adapter.GanKAndroidAdapter;
import com.ycbjie.gank.bean.bean.GanKIoDataBean;
import com.pedaily.yc.ycdialoglib.dialogMenu.CustomBottomDialog;
import com.pedaily.yc.ycdialoglib.dialogMenu.CustomItem;
import com.pedaily.yc.ycdialoglib.dialogMenu.OnItemClickListener;
import org.yczbj.ycrefreshviewlib.item.RecycleViewItemLine;
import org.yczbj.ycrefreshviewlib.YCRefreshView;
import org.yczbj.ycrefreshviewlib.adapter.RecyclerArrayAdapter;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * ================================================
 * 作    者：杨充
 * 版    本：1.0
 * 创建日期：2017/8/28
 * 描    述：我的干货页面  干活定制
 * 修订历史：
 * ================================================
 */
public class KnowledgeCustomFragment extends BaseFragment {


    YCRefreshView recyclerView;
    private MyKnowledgeActivity activity;
    private String mType = "all";
    private int mPage = 1;
    private int per_page_more = 20;
    private GanKAndroidAdapter adapter;


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MyKnowledgeActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public int getContentView() {
        return R.layout.base_easy_recycle;
    }

    @Override
    public void initView(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        initRecycleView();
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        recyclerView.showProgress();
        getGanKAll(mType,mPage,per_page_more);
    }

    private void initRecycleView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new GanKAndroidAdapter(activity);
        recyclerView.setAdapter(adapter);
        final RecycleViewItemLine line = new RecycleViewItemLine(activity, LinearLayout.HORIZONTAL,
                SizeUtils.px2dp(2), getResources().getColor(R.color.grayLine));
        recyclerView.addItemDecoration(line);
        AddHeader();
        //加载更多
        adapter.setMore(R.layout.view_recycle_more, new RecyclerArrayAdapter.OnMoreListener() {
            @Override
            public void onMoreShow() {
                if (NetworkUtils.isConnected()) {
                    if (adapter.getAllData().size() > 0) {
                        mPage++;
                        getGanKAll(mType,mPage,per_page_more);
                    } else {
                        adapter.pauseMore();
                    }
                } else {
                    adapter.pauseMore();
                    Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onMoreClick() {

            }
        });

        //设置没有数据
        adapter.setNoMore(R.layout.view_recycle_no_more, new RecyclerArrayAdapter.OnNoMoreListener() {
            @Override
            public void onNoMoreShow() {
                if (NetworkUtils.isConnected()) {
                    adapter.resumeMore();
                } else {
                    Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNoMoreClick() {
                if (NetworkUtils.isConnected()) {
                    adapter.resumeMore();
                } else {
                    Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //设置错误
        adapter.setError(R.layout.view_recycle_error, new RecyclerArrayAdapter.OnErrorListener() {
            @Override
            public void onErrorShow() {
                adapter.resumeMore();
            }

            @Override
            public void onErrorClick() {
                adapter.resumeMore();
            }
        });

        //刷新
        recyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkUtils.isConnected()) {
                    mPage = 1 ;
                    getGanKAll(mType,mPage,per_page_more);
                } else {
                    recyclerView.setRefreshing(false);
                    Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void AddHeader() {
        adapter.removeAllHeader();
        adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View view = LayoutInflater.from(activity).inflate(R.layout.head_gank_type_all, parent, false);
                LinearLayout ll_choose_catalogue = view.findViewById(R.id.ll_choose_catalogue);
                ll_choose_catalogue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startOpenBottomDialog();
                    }
                });
                return view;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
    }

    private void startOpenBottomDialog() {
        new CustomBottomDialog(activity)
                .title("选择分类")
                .setCancel(true,"取消选择")
                .orientation(CustomBottomDialog.VERTICAL)
                .inflateMenu(R.menu.gank_bottom_sheet, new OnItemClickListener() {
                    @Override
                    public void click(CustomItem item) {

                    }
                })
                .show();
    }

    private void getGanKAll(String id , int page , int pre_page) {
        GanKModel model = GanKModel.getInstance();
        model.getGanKData(id,page,pre_page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GanKIoDataBean>() {
                    @Override
                    public void onError(Throwable e) {
                        recyclerView.showError();
                        recyclerView.setErrorView(R.layout.view_custom_empty_data);
                    }

                    @Override
                    public void onComplete() {
                        if(recyclerView!=null){
                            recyclerView.showRecycler();
                        }
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(GanKIoDataBean ganKIoDataBean) {
                        if(adapter==null){
                            adapter = new GanKAndroidAdapter(activity);
                        }

                        if(mPage==1){
                            if(ganKIoDataBean!=null && ganKIoDataBean.getResults()!=null && ganKIoDataBean.getResults().size()>0){
                                adapter.clear();
                                adapter.addAll(ganKIoDataBean.getResults());
                                adapter.notifyDataSetChanged();
                                if(recyclerView!=null){
                                    recyclerView.showRecycler();
                                }
                            } else {
                                recyclerView.showEmpty();
                                recyclerView.setEmptyView(R.layout.view_custom_empty_data);
                            }
                        } else {
                            if(ganKIoDataBean!=null && ganKIoDataBean.getResults()!=null && ganKIoDataBean.getResults().size()>0){
                                adapter.addAll(ganKIoDataBean.getResults());
                                adapter.notifyDataSetChanged();
                                if(recyclerView!=null){
                                    recyclerView.showRecycler();
                                }
                            } else {
                                adapter.stopMore();
                            }
                        }
                    }
                });
    }

}
