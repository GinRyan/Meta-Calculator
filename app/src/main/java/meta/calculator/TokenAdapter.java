//package meta.calculator;
//import android.support.v7.widget.RecyclerView;
//import android.view.View;
//import java.util.ArrayList;
//import meta.calculator.TokenAdapter;
//import android.view.ViewGroup;
//
//
//import android.view.LayoutInflater;
//import android.widget.Button;
//
//public class TokenAdapter extends RecyclerView.Adapter<TokenAdapter.VH>
//{
//
//
//	@Override
//	public TokenAdapter.VH onCreateViewHolder(ViewGroup p1, int p2)
//	{
//		VH vh=new VH(LayoutInflater.from(p1.getContext()).inflate(R.layout.inner,p1,false));
//		return vh;
//	}
//
//	@Override
//	public void onBindViewHolder(TokenAdapter.VH p1, int p2)
//	{
//		p1.bt.setText(MainActivity.tokens[p2]);
//	    p1.bt.setTag(MainActivity.tags[p2]);
//	}
//
//	@Override
//	public int getItemCount()
//	{
//		return MainActivity.tokens.length;
//	}
//	
//
//	
//	public static class VH extends RecyclerView.ViewHolder{
//		private Button bt;
//		public VH(View v){
//			super(v);
//			bt=(Button) v.findViewById(R.id.innerButton);
//		}
//	}
//}
