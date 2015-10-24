package com.lansun.qmyo.view;

import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Color;

import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;

public class TextPage extends EditText implements OnLongClickListener{
	
	private int off;
	private Context content; //字符串的偏移值

		public TextPage(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
		}
	
		public TextPage(Context context, AttributeSet attrs, int defStyleAttr) {
			super(context, attrs, defStyleAttr);
			// TODO Auto-generated constructor stub
		}
	
		public TextPage(Context context) {
			super(context);
		}

		private void initialize() {
			setGravity(Gravity.TOP);
			setBackgroundColor(Color.WHITE);
		}
		
		@Override
	    protected void onCreateContextMenu(ContextMenu menu) {
			//不做任何处理，为了阻止长按的时候弹出上下文菜单
		}
		
		@Override
		public boolean getDefaultEditable() {
			return false;
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			int action = event.getAction();
			Layout layout = getLayout();
			int line = 0;
			switch(action) {
			case MotionEvent.ACTION_DOWN:
				line = layout.getLineForVertical(getScrollY()+ (int)event.getY());        
		        off = layout.getOffsetForHorizontal(line, (int)event.getX());
		        Selection.setSelection(getEditableText(), off);
				break;
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				line = layout.getLineForVertical(getScrollY()+(int)event.getY()); 
				int curOff = layout.getOffsetForHorizontal(line, (int)event.getX());			
		        Selection.setSelection(getEditableText(), off, curOff);
				break;
			}
			return true;
		}
		
	/*	OnLongClickListener*/
		
		
		

	@Override
	public boolean onLongClick(View arg0) {
		 Toast.makeText(content, "长点击了！", Toast.LENGTH_LONG).show();
		 Editable text = getText();
		 String testStr = text.toString();
		 ClipboardManager cmb = (ClipboardManager) content.getSystemService(Context.CLIPBOARD_SERVICE);
		 cmb.setText(testStr.trim()); //将内容放入粘贴管理器,在别的地方长按选择"粘贴"即可
		 Toast.makeText(content, "已复制文本到剪贴板", Toast.LENGTH_LONG).show();
	     cmb.getText();
		return false;
	}
       
	}

	

