package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PickerView;
import com.android.pc.ioc.view.PickerView.onSelectListener;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

/**
 * 编辑用户信息的Fragment
 * 
 * @author bhxx
 * 
 */
public class EditUserFragment extends BaseFragment {

	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View  rl_edit_user_gender, rl_edit_user_email,
				rl_edit_user_truname, rl_edit_user_address, rl_edit_user_job,
				rl_edit_user_position, rl_edit_user_degress, rl_edit_user_like,
				rl_edit_user_income;
		private EditText et_edit_user_email;
		private EditText et_edit_user_truname;

		private TextView tv_edit_user_income, tv_edit_user_like,
				tv_edit_user_degress, tv_edit_user_position, tv_edit_user_job,
				tv_edit_user_address, tv_edit_user_name, tv_edit_user_email,
				tv_edit_user_gender;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_edit_user, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		initData();
	}

	/**
	 * 数据的回显
	 */
	private void initData() {
		v.tv_edit_user_income.setText(GlobalValue.user.getIncome()=="null"?"":GlobalValue.user.getIncome());
		v.tv_edit_user_like.setText(GlobalValue.user.getHobby()=="null"?"":GlobalValue.user.getHobby());
		v.tv_edit_user_degress.setText(GlobalValue.user.getDegrees()=="null"?"":GlobalValue.user.getDegrees());
		v.tv_edit_user_position.setText(GlobalValue.user.getPosition()=="null"?"":GlobalValue.user.getPosition());
		v.tv_edit_user_job.setText(GlobalValue.user.getJob()=="null"?"":GlobalValue.user.getJob());
		v.tv_edit_user_address.setText(GlobalValue.user.getAddress()=="null"?"":GlobalValue.user.getAddress());
		v.tv_edit_user_name.setText(GlobalValue.user.getName()=="null"?"":GlobalValue.user.getName());
		v.tv_edit_user_email.setText(GlobalValue.user.getEmail()=="null"?"":GlobalValue.user.getEmail());
		v.tv_edit_user_gender.setText(GlobalValue.user.getGender()=="null"?"":GlobalValue.user.getGender());
		v.tv_edit_user_name.setText(GlobalValue.user.getTruename()=="null"?"":GlobalValue.user.getTruename());
	}

	private void click(View view) {
		ArrayList<String> data = new ArrayList<String>();
		Fragment fragment;
		switch (view.getId()) {
		case R.id.rl_edit_user_email://邮编
			fragment = new EditUserInfoFragment();
			Bundle args = new Bundle();
			args.putString("name", getString(R.string.change_email));
			args.putString("paramName", "email");
			fragment.setArguments(args);
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
			break;
		case R.id.rl_edit_user_truname://真名
			fragment = new EditUserInfoFragment();
			args = new Bundle();
			args.putString("name", getString(R.string.change_truename));
			args.putString("paramName", "truename");
			fragment.setArguments(args);
			event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
			break;
		case R.id.rl_edit_user_address://地址
			/*fragment = new EditUserAddressFragment();*/
			fragment = new EditUserAddressFragment1();
			event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
			break;
		case R.id.rl_edit_user_gender://性别
			data.clear();
			data.add("男");
			data.add("女");
			selectDialog(data, "gender");
			break;
		case R.id.rl_edit_user_job://工作
			data.clear();
			data.add("学生");
			data.add("IT/互联网/电子");
			data.add("金融业");
			data.add("贸易/消费/制造");
			data.add("医疗");
			data.add("广告/媒体");
			data.add("房地产/建筑");
			data.add("专业服务/教育/培训");
			data.add("服务业、物流/运输");
			data.add("能源/原材料");
			data.add("政府/非赢利机构");
			data.add("其他");
			selectDialog(data, "job");
			break;
		case R.id.rl_edit_user_position://职位
			data.clear();
			data.add("学生");
			data.add("普通职工");
			data.add("中层");
			data.add("高管");
			data.add("企业主");
			selectDialog(data, "position");
			break;
		case R.id.rl_edit_user_degress://学历
			data.clear();
			data.add("大专以下");
			data.add("大专");
			data.add("本科");
			data.add("研究生");
			data.add("博士或以上");
			selectDialog(data, "degrees");
			break;
		case R.id.rl_edit_user_like://-->爱好
			EditUserLickFragment fragment1 = new EditUserLickFragment();
			FragmentEntity event1 = new FragmentEntity();
			event1.setFragment(fragment1);
			EventBus.getDefault().post(event1);
			break;
		case R.id.rl_edit_user_income://收入
			data.clear();
			data.add("3万以下");
			data.add("3万-5万");
			data.add("5万-10万");
			data.add("10万-20万");
			data.add("20万-50万");
			data.add("100万以上");
			selectDialog(data, "income");
			break;
		}
		initData();
	}

	public void selectDialog(ArrayList<String> data, final String paramsName) {
		View view = inflater.inflate(R.layout.select_sex_dialog, null);
		PickerView ll_setmovment_pv = (PickerView) view
				.findViewById(R.id.ll_setmovment_pv);
		final TextView tv_set_up = (TextView) view.findViewById(R.id.tv_set_up);
		
		final Dialog dialog = new Dialog(activity,R.style.transparentFrameWindowStyle);
		
		dialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		
		Window window = dialog.getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.PopupWindowAnimation);
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = activity.getWindowManager().getDefaultDisplay().getHeight();
		// 以下这两句是为了保证按钮可以水平满屏
		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = 500;
		// 设置显示位置
		dialog.onWindowAttributesChanged(wl);
		// 设置点击外围解散
		dialog.setCanceledOnTouchOutside(true);
		PickerView pv = new PickerView(activity);
		
		//pv.init(0x33000000);
		
		//给PickerView设置上原始的数据内容
		ll_setmovment_pv.setData(data);//给PickerView设置上需要展示的数据
		
		
		
		dialog.show();
		ll_setmovment_pv.setOnSelectListener(new onSelectListener() {

			@Override
			public void onSelect(final String text) {

				//滚动中间放入了点击监听
				tv_set_up.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						InternetConfig config = new InternetConfig();
						config.setKey(0);
						HashMap<String, Object> head = new HashMap<>();
						head.put("Authorization",
								"Bearer " + App.app.getData("access_token"));
						config.setHead(head);
						LinkedHashMap<String, String> params = new LinkedHashMap<>();
						params.put(paramsName, text);
						
						//POST方式提交
						FastHttpHander.ajax(GlobalValue.URL_USER_SAVE,
								params, config, EditUserFragment.this);
						
						dialog.dismiss();
					}
				});

			}
		});

	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				GlobalValue.user = Handler_Json.JsonToBean(User.class,
						r.getContentAsString());
				CustomToast.show(activity, getString(R.string.tip), "修改成功");
				initData();
				break;
			}
		} else {
			CustomToast.show(activity, "网络故障", "网络错误");
		}

	}

}
