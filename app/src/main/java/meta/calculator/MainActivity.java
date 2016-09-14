
package meta.calculator;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hanks.htextview.HTextView;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.rey.material.widget.FloatingActionButton;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import me.yugy.github.reveallayout.RevealLayout;
import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;
import com.rey.material.widget.Button;
public class MainActivity extends AppCompatActivity
{
public static String[] tokens=
{ "a/b",  "X", "Y","rnd", "sin", "cos","tan",
	"A" ,  "C", "D","log","asin","acos","atan",
	"rad","deg","ln","lg" , "abs","ave" ,"bin",
	"(" , "," , ")", "r" , "mod", "sum","hex",
	"1",  "2", "3", "+" , "min", "max","fac",
	"4",  "5", "6", "-" , "cl" , "fl" ,"int",
	"7",  "8", "9", "*" ,   "^",  "as","rd",
	"Ans",  "0", ".",  "/", "p"  ,"e",
	" not "," and "," or ", "==" , "≠"  , "true","false",
	"<" ,  ">","<-","->" ,"AC"  ,"Del"
  };
	public static String[] tags=
	{ "a/b",  "X", "Y","rnd(", "sin(", "cos(","tan(",
		"A" ,  "C", "D","log(","asin(","acos(","atan(",
		"rad(","deg(","ln(","lg(" , "abs(","ave(" ,"bin(",
		"(" , "," , ")", "√(" , "mod(", "sum(","hex(",
		"1",  "2", "3", "+" , "min(", "max(","fac(",
		"4",  "5", "6", "-" , "cl(" , "fl(" ,"int(",
		"7",  "8", "9", "*" ,   "^",  "as(","rd(",
		"Ans",  "0", ".",  "/", "π"  ,"e",
		" not "," and "," or ", "==" , "≠"  ,"true","false",
		"<" ,  ">","<-","->" ,"AC"  ,"Del"
	};
private StringBuilder output;
private FloatingActionButton fab;
private GridView gv;
private LuaState L;
private EditText et;
private HTextView ans;
//private RecyclerView rv;
private RevealLayout rl,diy;
private TextView er;
private SystemBarTintManager tintManager;
String Ans="0";
private boolean hasError;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		tintManager= new SystemBarTintManager(this);
		tintManager.setStatusBarTintColor(Color.parseColor("#25000000"));
        tintManager.setStatusBarTintEnabled(true);
		et=(EditText) findViewById(R.id.et_src);
		et.setShowSoftInputOnFocus(false);
		//et.requestFocus();
		fab=(FloatingActionButton) findViewById(R.id.fab);
		ans=(HTextView) findViewById(R.id.tv_ans);
		rl=(RevealLayout) findViewById(R.id.rl);
		diy=(RevealLayout) findViewById(R.id.diy);
		er=(TextView) findViewById(R.id.info);
		gv=(GridView) findViewById(R.id.gv);
		gv.setAdapter(new GvAdapter());
		et.setHintTextColor(0);
		output=new StringBuilder();
		
		L = LuaStateFactory.newLuaState();
		L.openLibs();

		try {
			L.pushJavaObject(this);
			L.setGlobal("activity");

			JavaFunction print = new JavaFunction(L) {
				@Override
				public int execute()  {
					for (int i = 2; i <= L.getTop(); i++) {
						int type = L.type(i);
						String stype = L.typeName(type);
						String val = null;
						 if (stype.equals("boolean")) {
							val = L.toBoolean(i) ? "true" : "false";
						} else {
							val = L.toString(i);
						}
						if (val == null)
							val = stype;						
						output.append(val);
						output.append("\t");
					}
					output.append("\n");					
					return 0;
				}
			};
			print.register("print");

			JavaFunction assetLoader = new JavaFunction(L) {
				@Override
				public int execute()  {
					String name = L.toString(-1);
					AssetManager am = getAssets();
					try {
						InputStream is = am.open(name + ".lua");
						byte[] bytes = readAll(is);
						L.LloadBuffer(bytes, name);
						return 1;
					} catch (Exception e) {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						e.printStackTrace(new PrintStream(os));
						L.pushString("Cannot load module "+name+":\n"+os.toString());
						return 1;
					}
				}
			};

		L.getGlobal("package");            // package
			L.getField(-1, "loaders");         // package loaders
			int nLoaders = L.objLen(-1);       // package loaders
			L.pushJavaFunction(assetLoader);   // package loaders loader
			L.rawSetI(-2, nLoaders + 1);       // package loaders
			L.pop(1);                          // package

	       L.getField(-1, "path");            // package path
			String customPath = getFilesDir() + "/?.lua";
			L.pushString(";" + customPath);    // package path custom
			L.concat(2);                       // package pathCustom
			L.setField(-2, "path");            // package
			L.pop(1);
		} catch (Exception e) {
			ans.setText("Cannot override print");
		}
	}
	private static byte[] readAll(InputStream input) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
		byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		return output.toByteArray();
	}
	
	
	String safeEvalLua(String src) {
		String res = null;	
		try {
			res = evalLua(src);
		} catch(LuaException e) {
			res = e.getMessage()+"\n";
		}
		return res;		
	}
	


	String evalLua(String src) throws LuaException {
		L.setTop(0);
		int ok = L.LloadString(src);
		if (ok == 0) {
	L.getGlobal("debug");
    L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
        	ok = L.pcall(0, 0, -2);
			if (ok == 0) {				
				String res = output.toString();
				output.setLength(0);
				return res;
			}
		}
		throw new LuaException(errorReason(ok)  + L.toString(-1));
		//return null;		

	}
	private String errorReason(int error) {
		switch (error) {
			case 4:
				return "错误:内存溢出";
			case 3:
				return "语法错误";
			case 2:
				return "运行时错误";
			case 1:
				return "域错误";
		}
		return "未知错误 " + error;
	}
//    private String describe(String src){
//		switch(src){
//			case "sin":return "正弦函数:sin(a),a默认为弧度";
//			case "cos":return "余弦函数:cos(a),a默认为弧度";
//			case "tan":return "正切函数:tan(a),a默认为弧度";
//			case "asin":return "反正弦函数:asin(a),-1≤a≤1,默认返回弧度值";
//			case "acos":return "反余弦函数:acos(a),-1≤a≤1,默认返回弧度值";
//			case "atan":return "反正切函数:atan(a),默认返回弧度值";
//			case "fl":return bj("fl(a):取得小于等于a的最小整数，如fl(1)=1,fl(3.1)=3,fl(-5.6)=-6";
//			case "cl":return "cl(a):取得大于等于a的最小整数，如cl(1)=1,cl(3.1)=4,cl(-5.6)=-5";
//			case "rd":return "四舍五入函数:rd(a),取得小于等于a的最小整数，如rd(1.5)=2,rd(3.1)=3,rd(-5.6)=-6";
//			case "rnd":return "随机数函数:rnd(a,b),随机产生一个随机整数r,a≤r≤b(a<=b)或b≤r≤a(b≤a)";
//			case "sum":return "求和函数:sum(a,b,...),取得传人参数之和";
//			case "ave":return "求平均数函数:ave(a,b,...),取得传人参数的平均数";
//			case "A":return "排列函数:A(a,b):a,b为自然数,取得从max(a，b)个元素\n中取min(a，b)个元素(考虑排序)的不同方法的个数";
//			case "C":return "组合函数:C(a,b):a,b为自然数,取得从max(a，b)个元素\n中取min(a，b)个元素(不考虑顺序)的不同方法的个数";
//			case "D":return "求方差函数:D(a,b,...):取得传人参数的方差";
//			case "fac":return "狭义阶乘:fac(a),a为自然数,默认fac(0)=1";
//			case "min": return "求最小值函数:min(a,b,...),取得传人参数中的最小值";
//			case "max": return "求最大值函数:max(a,b,...),取得传人参数中的最大值";
//			case "int":return "取整函数:int(a),取得a的整数部分";
//			case "log":return "取对数函数:log(a,b),a>0,a≠1,b>0,取得a为底数b为真数的对数值，如log(2,4)=2";
//			case "lg" :return "lg(a):取得十为底数a为真数的对数值,如lg(100)=2";
//			case "ln" :return "ln(a):取得自然底数e为底数a为真数的对数值,如ln(e)=1";
//			case "a/b":return "将计算结果转化为分数形式";
//			case "mod":return "取余函数:mod(a,b),取得a除以b的余数";
//			default :return "";
//		}
//	}
	
	public String f(){
		return 
		"require 'import'"
		+"\ne=Math.E p=Math.PI Ans="+Ans
		+"\nfunction sin(a) return Math:sin(a) end"
		+"\nfunction cos(a) return Math:cos(a) end"
		+"\nfunction tan(a) return Math:tan(a) end"
		+"\nfunction asin(a) return Math:asin(a) end"
		+"\nfunction acos(a) return Math:acos(a) end"
		+"\nfunction atan(a) return Math:atan(a) end"
		+"\nfunction log(a,b) return (Math:log(b))/Math:log(a) end"
		+"\nfunction lg(a) return Math:log10(a) end"
		+"\nfunction ln(a) return Math:log(a) end"
		+"\nfunction sumt(a) r=0 for i,v in ipairs(a) do r=r+v end return r end"
		+"\nfunction avet(a) return sumt(a)/#a end"
	    +"\nfunction sum(...) r=0 for i,v in ipairs(arg) do  r = r + v end return r end"
		+"\nfunction ave(...) return sumt(arg)/(#arg) end"
		+"\nfunction fac(a) if a==0 then return 1 elseif a<0 then return nil elseif a==1 then return 1 else return a*fac(a-1) end end"
		+"\nfunction min(a,...) local min=a for x,y in ipairs(arg) do min=math.min(min,y) end return min end"
		+"\nfunction max(a,...) local max=a for x,y in ipairs(arg) do max=math.max(max,y) end return max end"
		+"\nfunction  A(a,b) if a<b then return fac(b)/fac(b-a) else return fac(a)/fac(a-b) end end"
		+"\nfunction C(a,b) return A(a,b)/fac(min(a,b)) end"
		+"\nfunction r(a) return a^0.5 end"
		+"\nfunction D(...) local av=avet(arg) local r=0 for i,v in ipairs(arg) do r=r+(v-av)^2 end return r end"
		+"\nfunction mod(a,b) return a%b end"
		+"\nfunction abs(a) return Math:abs(a) end"
		+"\nfunction rad(a) return a*p/180 end"
		+"\nfunction fl(a) return Math:floor(a) end"
		+"\nfunction cl(a) return Math:ceil(a) end"
		+"\nfunction rd(a) return Math:round(a) end"
		+"\nfunction int(a) return as(a>=0,a-a%1,a-a%1+1) end"
		+"\nfunction rnd(a,b)  return math.random(math.min(a,b),math.max(a,b)) end"
	    +"\nfunction sinh(a) return 1/2*(E^a-E^(-a)) end"
	    +"\nfunction cosh(a) return 1/2*(E^a+E^(-a)) end"
		+"\nfunction tanh(a) return sinh(a)/cosh(a) end"
	    +"\nfunction as(a,b,c) if a then return b else return c end end";
		
		}
	private void calculate(){
		String src=f()+"\nprint("+et.getText().toString()+")";
		src=src.replace("π","p").replace("√","r");
		try {
			String res = evalLua(src);
			error(null);
			ans.animateText(res);	
		} catch(LuaException le) {	
			error("语法错误，请检查");}
	}
	////////////
	private void error(String error){
		if(error==null){
			hasError=false;
			rl.hide();
			er.setText("");
			return;
		}if(hasError){return;}
		hasError=true;
		rl.show();
	    er.setText(error);
	}
	
	public void onSwitch(View v)	
	{   FloatingActionButton bt=(FloatingActionButton) v;
		bt.setLineMorphingState((bt.getLineMorphingState() + 1) % 2, true);
		int[] p=getLocationInView(findViewById(R.id.diy),fab);
		if(diy.isContentShown()){diy.hide(p[0],p[1],1200);return;}
		
		diy.show(p[0],p[1],1000);
	}
	public void onShow(View v){
		String tag=((Button)v).getTag().toString();
		//String text=((Button)v).getText().toString();
		int s=et.getSelectionStart();
		int e=et.getSelectionEnd();
		switch(tag){
			case "清除":
				et.setText("");
				break;
			case "删除":
				if(et.getText().length()==0){return;}
				if(s==e){et.getText().delete(s-1, e);}
				else{et.getText().delete(s,e);}
                calculate();
				break;
			case "->":
				if(s==et.getText().length()){return;}
				et.setSelection(s+1);
				break;
			case "<-":
				if(s==0){return;}
				et.setSelection(s-1);
				break;
			
			default:
				et.getText().insert(s,tag);
               
				calculate();
				}
		}
	public static int[] getLocationInView(View src, View target)
    {
        final int[] l0 = new int[2];
        src.getLocationOnScreen(l0);

        final int[] l1 = new int[2];
        target.getLocationOnScreen(l1);

        l1[0] = l1[0] - l0[0] + target.getWidth() / 2;
        l1[1] = l1[1] - l0[1] + target.getHeight() / 2;

        return l1;
    }
	class GvAdapter extends BaseAdapter
	{  

		@Override
		public int getCount()
		{
			
			return tokens.length;
		}

		@Override
		public Object getItem(int p1)
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public long getItemId(int p1)
		{
			// TODO: Implement this method
			return 0;
		}

		@Override
		public View getView(int p1, View p2, ViewGroup p3)
		{   LinearLayout ll=(LinearLayout) LayoutInflater.from(p3.getContext()).inflate(R.layout.inner,p3,false);
			Button bt=(Button) ll.findViewById(R.id.innerButton);
			bt.setText(tokens[p1]);
			bt.setTag(tags[p1]);
			return ll;
		}
		
			
		}
	}
