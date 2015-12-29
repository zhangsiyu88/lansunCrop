package com.lansun.qmyo.fragment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.util.Handler_Inject;
import com.google.gson.Gson;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.QuestionListAdapter;
import com.lansun.qmyo.adapter.QuestionListAdapter.OnItemClickCallBack;
//import com.lansun.qmyo.adapter.question.QuestionAdapter;
//import com.lansun.qmyo.adapter.question.QuestionAdapter.OnItemClickCallBack;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.QuestionDetailItem;
import com.lansun.qmyo.domain.SecretaryQuestions;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.TestMyListView;
import com.lansun.qmyo.view.TestMyListView.OnRefreshListener;
import com.lansun.qmyo.view.ViewRight;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * @author Yeun.zhang
 */
public class MineSecretaryListFragment extends BaseFragment implements OnItemClickCallBack{//,OnRefreshListener
	/*private RecyclerView lv_question_recycle;
	 private QuestionAdapter question_adapter; 不使用原先的RecycleView*/
	
	private TestMyListView lv_question_recycle;
	private QuestionListAdapter question_adapter;
	private LinearLayoutManager manager;
	private List<QuestionDetailItem> lists;
	private SecretaryQuestions list;
	private int times;
	//private SwipeRefreshLayout swiperefresh;
	@InjectAll
	Views v;
	private String[] types = new String[] { "", "travel", "shopping", "party",
			"life", "student", "investment", "card" };
	class Views {
		private View fl_comments_right_iv;
		private TextView tv_activity_title;
		private ExpandTabView exp_mine_secretary;
	}
	private Handler handleOK=new Handler(){
		public void handleMessage(android.os.Message msg) {
			endProgress();
			switch (msg.what) {
			case 0:
				
//				setEmptyView(1);
//				lv_question_recycle.setVisibility(View.VISIBLE);
				question_adapter.notifyDataSetChanged();
				lv_question_recycle.onLoadMoreFished();//要让下次能够继续监听到话到末尾时，能够继续加载下一页
				
				break;
			case 1:
				break;
			case 5://页面一加载进来，lv_question_recycle要显示出来
				
				/*if(lists.size()!=0&&question_adapter!=null){
					lists.clear();
					question_adapter.notifyDataSetChanged();
				}*/
				
				lv_question_recycle.setVisibility(View.VISIBLE);
				lv_question_recycle.onRefreshFinshed(true);
				lv_question_recycle.onLoadMoreFished();
				
				break;
			case 6:
				
				lv_question_recycle.setVisibility(View.VISIBLE);
				lv_question_recycle.onLoadMoreOverFished();
				break;
			case 10:
				setEmptyView(0);//lv_question_recycle整个消失掉
				break;
			case 20:
				if(question_adapter!=null){
					question_adapter.notifyDataSetChanged();
				}
				break;
			}
			//swiperefresh.setRefreshing(false);
		};
	};
	private LinearLayout empty_liner;
	protected int lastItem;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_mine_secretary_list, container,false);
		initView(rootView);
		Handler_Inject.injectFragment(this, rootView);
		
		lv_question_recycle.setNoHeader(true);
		lv_question_recycle.setVisibility(View.INVISIBLE);
		
		//setListener();
		lv_question_recycle.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefreshing() {
				
				lists.clear();//清除之前展示的list
				first_enter = 0;//将第一次展示的标示重新设置为0
				
				startSearch(refreshUrl+"type="+currentType);
				LogUtils.toDebugLog("mysecretary", refreshUrl+"type="+currentType);
				
				//lv_question_recycle.onRefreshFinshed(true);
			
				
			}
			
			@Override
			public void onLoadingMore() {
				
				String refresh=String.valueOf(list.getNext_page_url());
				LogUtils.toDebugLog("", "refresh:   "+refresh);
				
				if ("".equals(refresh)||"null".equals(refresh)) {
					if(times == 0){
						CustomToast.show(getActivity(), R.string.none_data_tip,R.string.none_data_content);
						lv_question_recycle.onLoadMoreOverFished();
						times++;
					}else{
						CustomToast.show(getActivity(), R.string.none_data_tip,R.string.none_data_content);
						lv_question_recycle.onLoadMoreOverFished();
					}
				}else {
					startSearchForLoadMore(refresh+"&type="+currentType);
					LogUtils.toDebugLog("refreshUrl", refresh+"&type="+currentType);
				}
			}
		});
		return rootView;
	}
	
	/**
	 * RecycleView的对象暂时停用
	 * 自定义的ListView已经完成了需要的监听
	 */
//	private void setListener() {
//		lv_question_recycle.setOnScrollListener(new OnScrollListener() {
//			@Override
//			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//				super.onScrolled(recyclerView, dx, dy);
//				lastItem=manager.findLastVisibleItemPosition();
//			}
//			@Override
//			public void onScrollStateChanged(RecyclerView recyclerView,
//					int newState) {
//				super.onScrollStateChanged(recyclerView, newState);
//				if (newState==0) {
//					if (lastItem==question_adapter.getItemCount()-1) {
//						if (lastItem<9) {
//							swiperefresh.setRefreshing(false);
//						}else {
//							swiperefresh.setRefreshing(true);
//							String refresh=String.valueOf(list.getNext_page_url());
//							
//							
//							if ("".equals(refresh)||"null".equals(refresh)) {
//								CustomToast.show(getActivity(), R.string.none_data_tip,R.string.none_data_content);
//								swiperefresh.setRefreshing(false);
//							}else {
//								startSearch(refresh+"&type="+currentType);
//								
//								LogUtils.toDebugLog("refreshUrl", refresh+"&type="+currentType);
//							}
//						}
//					}else {
//						swiperefresh.setRefreshing(false);
//					}
//				}
//			}
//		});
//	}
	
	/**
	 * 实际进行加载更多的 网络访问操作
	 * @param string
	 */
	private void startSearchForLoadMore(String url) {
		
		while (GlobalValue.mySecretary!=null) {
			break;
		}
		try{
			OkHttp.asyncGet(url, "Authorization", "Bearer "+App.app.getData("access_token"), null, new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					if (response.isSuccessful()) {
						//handleOK.sendEmptyMessage(5);
						
						String json=response.body().string();
						Gson gson=new Gson();
						list = gson.fromJson(json, SecretaryQuestions.class);
						if (list.getData().size()==0) {
							handleOK.sendEmptyMessage(10);
						}else {
							if(list.getData().size()<10){
								times++;
								handleOK.sendEmptyMessage(6);
							}
							for (QuestionDetailItem item:list.getData()) {
								lists.add(item);
							}
							handleOK.sendEmptyMessage(0);
						}
					}
				}
				@Override
				public void onFailure(Request request, IOException exception) {
					
				}
			});
		}catch(Exception e){
			
		}
	}
	
	/**
	 * 设置 emptyview
	 * @param state 0 add 1 remove
	 */
	private void setEmptyView(int state) {
		if (state==0) {
			lv_question_recycle.setVisibility(View.GONE);
			empty_liner.setVisibility(View.VISIBLE);
		}else {
			lv_question_recycle.setVisibility(View.VISIBLE);
			empty_liner.setVisibility(View.INVISIBLE);
		}
	}
	private void initView(View rootView) {
		empty_liner=(LinearLayout)rootView.findViewById(R.id.empty_liner);
		lists=new ArrayList<>();
		/*lv_question_recycle=(RecyclerView)rootView.findViewById(R.id.lv_secretary_list);
		manager=new LinearLayoutManager(getActivity());
		lv_question_recycle.setLayoutManager(manager);*/
		
		lv_question_recycle=(TestMyListView)rootView.findViewById(R.id.lv_secretary_list);
		
		//注意下面的数据源是 lists（末尾多个s）
		question_adapter=new QuestionListAdapter(activity,lists,MineSecretaryListFragment.this);
		lv_question_recycle.setAdapter(question_adapter);
		
		rootView.findViewById(R.id.look_help).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentEntity entity=new FragmentEntity();
				
				/*Fragment fragment=new SecretaryFragment();*/
				MainFragment fragment=new MainFragment(1);
				entity.setFragment(fragment);
				EventBus.getDefault().post(entity);
			}
		});
//		swiperefresh=(SwipeRefreshLayout)rootView.findViewById(R.id.swiperefresh);
//		swiperefresh.setOnRefreshListener(this);
	}

	private HashMap<Integer, String> holder_button = new HashMap<>();
	private HashMap<Integer, View> mViewArray = new HashMap<>();
	private String currentType = ""; //

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.mine_secretary, null, 0);
		viewRight = new ViewRight(activity);
		v.exp_mine_secretary.removeAllViews();
		ArrayList<String> sxGroup = new ArrayList<String>();
		sxGroup.add(getString(R.string.all));
		sxGroup.add(getString(R.string.travel_holiday));
		sxGroup.add(getString(R.string.new_shopping));
		sxGroup.add(getString(R.string.shengyan_part));
		sxGroup.add(getString(R.string.gaozhi_life));
		sxGroup.add(getString(R.string.studybroad));
		sxGroup.add(getString(R.string.licai_touzi));
		sxGroup.add(getString(R.string.handlecard));
		viewRight.setItems(sxGroup);//将列表数据塞入到控件的接收者里去
		holder_button.put(0, getString(R.string.all));
		mViewArray.put(0, viewRight);
		v.exp_mine_secretary.setValue(holder_button, mViewArray);
		refreshUrl = GlobalValue.URL_SECRETARY_QUESTIONS;
		startSearch(refreshUrl+"type="+currentType);
		
		viewRight.setOnSelectListener(new ViewRight.OnSelectListener() {
			@Override
			public void getValue(String distance, String showText, int position) {
				
				lv_question_recycle.setVisibility(View.INVISIBLE);
				if (!currentType.equals(types[position])) {
					lists.clear();
				}
				
				//主动刷新页面数据
				currentType = types[position];
				startSearch(refreshUrl+"type="+currentType);
				times = 0;
				first_enter = 0;
				onRefresh(viewRight, showText);
			}
		});
	}

	/**
	 * 一进来加载页面 或 主动刷新的操作
	 * @param url
	 */
	private void startSearch(final String url) {
		
		setProgress(lv_question_recycle);
		startProgress();
		
		while (GlobalValue.mySecretary!=null) {
			break;
		}
		try{
			OkHttp.asyncGet(url, "Authorization", "Bearer "+App.app.getData("access_token"), null, new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					if (response.isSuccessful()) {
						
						handleOK.sendEmptyMessage(5);
						
						String json=response.body().string();
						Gson gson=new Gson();
						list = gson.fromJson(json, SecretaryQuestions.class);
						
						if (list.getData().size()==0) {//获取到的列表的值为 0 ，显示空图提示
							handleOK.sendEmptyMessage(10);
						}else {
							if(list.getData().size()<10){
								if(first_enter == 0){//首次进来，并且数据数目少于10条
									times++;
									handleOK.sendEmptyMessage(6);
								}
							}else{
								//NO-OP
							}
							lists.clear();
							handleOK.sendEmptyMessage(20);
							
							for (QuestionDetailItem item:list.getData()) {
								lists.add(item);
							}
							handleOK.sendEmptyMessage(0);
						}
						first_enter = Integer.MAX_VALUE;

					}
				}
				@Override
				public void onFailure(Request request, IOException exception) {
					
				}
			});
		}catch(Exception e){
			
		}
	}
	
	boolean isGreen = false;
	private ViewRight viewRight;
	private ArrayList<HashMap<String, String>> dataList;

	@Override
	public void onPause() {
		v.exp_mine_secretary.onPressBack();
		super.onPause();
	}
	private void onRefresh(View view, String showText) {
		v.exp_mine_secretary.onPressBack();
		int position = getPositon(view);
		if (position >= 0
				&& !v.exp_mine_secretary.getTitle(position).equals(showText)) {
			v.exp_mine_secretary.setTitle(showText, position);
		}
	}

	private int getPositon(View tView) {
		for (int i = 0; i < mViewArray.size(); i++) {
			if (mViewArray.get(i) == tView) {
				return i;
			}
		}
		return -1;
	}
	@Override
	public void callBack(int question_id, String type) {
		QuestionDetailFragment fragment = new QuestionDetailFragment();
		FragmentEntity event = new FragmentEntity();
		Bundle args = new Bundle();
		args.putString("question_id",question_id+"");
		fragment.setArguments(args);
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}
//	@Override
//	public void onRefresh() {
//		if (manager.findFirstVisibleItemPosition()==0) {
//			swiperefresh.setRefreshing(false);
//		}
//	}
}