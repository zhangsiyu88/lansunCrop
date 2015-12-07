package com.lansun.qmyo.fragment;

import java.math.BigDecimal;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
/*import android.view.View.OnClickListener;*/
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.TelDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.lansun.qmyo.R;

/**
 * 关于迈界
 * 
 * @author bhxx
 * 
 */
public class AboutFragment extends BaseFragment {
	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_about_qmyo_net, tv_setting_cache_size,tv_about_qmyo_wx;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View rl_mine_common_problem, rl_mine_feedback,
				rl_mine_clear_cache, rl_mine_user_agreement;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_about, null);
		Handler_Inject.injectOrther(this, rootView);
		
		LogUtils.toDebugLog("", "MainBankcard的值为：  "+ App.app.getData("MainBankcard"));
//		CustomToast.show(activity, "MainBankcard的值为：", App.app.getData("MainBankcard"));
		
		return rootView;
	}

	@InjectInit
	private void init() {
		v.tv_about_qmyo_net
				.setText(Html.fromHtml(getString(R.string.qmyo_net)));
		
		
		v.tv_about_qmyo_wx
			.setText(Html.fromHtml(getString(R.string.qmyo_wx)));
		
		initCacheSize();
		
		init325435435();
		
	}
	
	private void init325435435() {
		initCacheSize();
		initCacheSize();
		initCacheSize();
		initCacheSize();
		initCacheSize();
	}
	
	

	private void click(View view) {
		BaseFragment fragment = null;
		switch (view.getId()) {
		case R.id.rl_mine_common_problem:// 常见问题 --〉帮助中心
			fragment = new HelpFragment();
			break;
		case R.id.rl_mine_feedback:// 意见反馈
			fragment = new FeedBackListFragment();
			break;
		case R.id.rl_mine_clear_cache:// 清空缓存
			Builder dialog = new AlertDialog.Builder(activity);
			dialog.setIcon(R.drawable.icon);
			dialog.setTitle(R.string.tip);
			dialog.setMessage(R.string.remove_cache_tip);
			dialog.setPositiveButton(getString(android.R.string.ok),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							
							//实质上清除图片的缓存内容来达到节省空间的作用
							ImageLoader.getInstance().getDiskCache()
									.getDirectory().delete();
							ImageLoader.getInstance().getDiskCache().clear();
							initCacheSize();
						}
					});
			dialog.setNegativeButton(getString(android.R.string.cancel), null);
			
			//dialog千万不要忘了show()
			dialog.create().show();
			break;
		case R.id.rl_mine_user_agreement:// TODO 用户协议
			fragment = new UserProtocolFragment();
			break;
			
		case R.id.tv_about_qmyo_net:
			
			  Intent intent= new Intent();        
			  intent.setAction("android.intent.action.VIEW");    
			  Uri content_url = Uri.parse("http://m.qmyo.com");   
			  intent.setData(content_url);  
			  startActivity(intent);
			break;
			
		case R.id.tv_about_qmyo_wx:
				/*Intent intentToWX= new Intent("android.intent.action.VIEW"); //声明要打开另一个VIEW.
				String qmyoclubRL = "http://weixin.qq.com/r/f3VFXS3Es3DMrWk_9yBt"; //这是你公共帐号的二维码的实际内容
				intentToWX.setData(Uri.parse(qmyoclubRL)); //设置要传递的内容。
				//intentToWX.setPackage("com.tencent.mm"); //直接制定要发送到的程序的包名。也可以不制定。就会弹出程序选择器让你手动选木程序。
				intentToWX.putExtra(Intent.EXTRA_SUBJECT,"Share"); 
				intentToWX.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentToWX); //当然要在Activity界面 调用了。
*/				
				Intent intentToWX = activity.getPackageManager().getLaunchIntentForPackage("com.tencent.mm");  
				String qmyoclubRL = "http://weixin.qq.com/r/f3VFXS3Es3DMrWk_9yBt"; //这是你公共帐号的二维码的实际内容
				intentToWX.setData(Uri.parse(qmyoclubRL)); //设置要传递的内容。
				intentToWX.putExtra(Intent.EXTRA_SUBJECT,"Share"); 
				intentToWX.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentToWX);   
				
			break;
		}
		if (fragment != null) {
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
		}
	}

	private void initCacheSize() {
		long length = ImageLoader.getInstance().getDiskCache().getDirectory()
				.length();
		float mbSize = (float) length / 1024 / 1024;
		BigDecimal b = new BigDecimal(mbSize);
		float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		v.tv_setting_cache_size.setText(String.format(
				getString(R.string.cache_size), f1));
	}
	
	/*Uri uri = Uri.parse(urlText.getText().toString());
	Intent intent = new Intent(Intent.ACTION_VIEW,uri);
	startActivity(intent);*/
	
/*	　原理同 分享到其他程序一样。 都要用 Intent 来传递数据到其他程序。 
 * 
 *    Intent i = new Intent(Intent.ACTION_VIEW); //声明要打开另一个VIEW.
	　　String guanzhu_URL = "http://weixin.qq.com/r/o3W_sRvEMSVOhwrSnyCH"; //这是你公共帐号的二维码的实际内容。可以用扫描软件扫一下就得到了。这是我的公共帐号地址。
	　　i.setData(Uri.parse(guanzhu_URL)); //设置要传递的内容。
	　　i.setPackage("com.tencent.mm"); //直接制定要发送到的程序的包名。也可以不制定。就会弹出程序选择器让你手动选木程序。
	　　i.putExtra(Intent.EXTRASUBJECT,"Share"); 
	   i.setFlags(Intent.FLAGACTIVITYNEWTASK);
	　　startActivity(intent); //当然要在Activity界面 调用了。
*/
}
