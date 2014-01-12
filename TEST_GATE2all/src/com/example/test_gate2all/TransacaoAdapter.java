package com.example.test_gate2all;

import java.util.ArrayList;

import android.R.color;
import android.R.drawable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TransacaoAdapter extends BaseExpandableListAdapter {

	private Activity activity;
	private ArrayList<Transacao> transacoes;
	private String espacos = "                   ";
	
	public TransacaoAdapter(Activity activity, ArrayList<Transacao> transacoes){
		this.activity = activity;
		this.transacoes = transacoes;
		
	}
	
	public void atualizarPara(ArrayList<Transacao> transacoes){
		
		this.transacoes = transacoes;
		this.notifyDataSetChanged();
		
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
	
		return transacoes.get(groupPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		TextView txt = new TextView(this.activity);
		txt.setPadding(15, 5, 0, 0);
		txt.setTextSize(12);
		txt.setTypeface(Typeface.MONOSPACE);
		
		switch(childPosition){
		case 0:
			txt.setText(new String("Adquirente: " +espacos).substring(0, 15) + transacoes.get(groupPosition).adquirente);
			break;
		case 1:
			txt.setText(new String("Status: " +espacos).substring(0, 15) + transacoes.get(groupPosition).statusTransacao);
			//txt.setText(transacoes.get(groupPosition).statusTransacao);
			break;
		case 2:
			txt.setText(new String("Cancelada: " +espacos).substring(0, 15) + transacoes.get(groupPosition).cancelada);
			//txt.setText(transacoes.get(groupPosition).cancelada);
			break;
		case 3:
			txt.setText(new String("Capturada: " +espacos).substring(0, 15) + transacoes.get(groupPosition).capturada);
			//txt.setText(transacoes.get(groupPosition).capturada);
			break;
		case 4:
			txt.setText(new String("Data: " +espacos).substring(0, 15) + transacoes.get(groupPosition).dataTransacao);
			//txt.setText(transacoes.get(groupPosition).dataTransacao);
			break;
		case 5:
			txt.setText(new String("Nome: " +espacos).substring(0, 15) + transacoes.get(groupPosition).nome);
			//txt.setText(transacoes.get(groupPosition).nome);
			break;
		case 6:
			txt.setText(new String("Cartão: " +espacos).substring(0, 15) + transacoes.get(groupPosition).numCartao);
			//txt.setText(transacoes.get(groupPosition).numCartao);
			break;
		case 7:
			txt.setText(new String("Plano: " +espacos).substring(0, 15) + transacoes.get(groupPosition).parcelas);
			//txt.setText(transacoes.get(groupPosition).parcelas);
			break;
		case 8:
			txt.setText(new String("TID: " +espacos).substring(0, 15) + transacoes.get(groupPosition).tid);
			//txt.setText(transacoes.get(groupPosition).tid);
			break;
		case 9:
			txt.setText(new String("Transação Id: " +espacos).substring(0, 15) + transacoes.get(groupPosition).transacaoId);
			//txt.setText(transacoes.get(groupPosition).transacaoId);
			break;
		default:
			txt = null;
			break;
		}
		
		return txt;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		
		return 10;
	}

	@Override
	public Object getGroup(int groupPosition) {
		
		return transacoes.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		
		return transacoes.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		
		return groupPosition;
	}

	//@SuppressLint("ResourceAsColor")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		ImageView imageView = new ImageView(this.activity);
		

	    if(transacoes.get(groupPosition).bandeira.equalsIgnoreCase("Visa")){
		 imageView = new ImageView(this.activity);
		 imageView.setImageResource(R.drawable.visa);		 
		}else if (transacoes.get(groupPosition).bandeira.equalsIgnoreCase("Mastercard")){
			 imageView = new ImageView(this.activity);			
			 imageView.setImageResource(R.drawable.master);
		}else if (transacoes.get(groupPosition).bandeira.equalsIgnoreCase("amex")){
			 imageView = new ImageView(this.activity);
			 imageView.setImageResource(R.drawable.amex);
		}else if (transacoes.get(groupPosition).bandeira.equalsIgnoreCase("elo")){
			 imageView = new ImageView(this.activity);
			 imageView.setImageResource(R.drawable.elo);
		}else if (transacoes.get(groupPosition).bandeira.equalsIgnoreCase("dinners")){
			 imageView = new ImageView(this.activity);
			 imageView.setImageResource(R.drawable.diners1);
		}else if (transacoes.get(groupPosition).bandeira.equalsIgnoreCase("discover")){
			 imageView = new ImageView(this.activity);
			 imageView.setImageResource(R.drawable.discover);
		}else if (transacoes.get(groupPosition).bandeira.equalsIgnoreCase("aura")){
			 imageView = new ImageView(this.activity);
			 imageView.setImageResource(R.drawable.aura);
			 
		}else if (transacoes.get(groupPosition).adquirente.equalsIgnoreCase("boleto")){
			 imageView = new ImageView(this.activity);
			 imageView.setImageResource(R.drawable.boleto);
			 
		}
		
		TextView txt1 = new TextView(this.activity);
		txt1.setPadding(75, 15, 0, 0);
		txt1.setTextSize(17);
		txt1.setTypeface(Typeface.DEFAULT_BOLD);
		txt1.setTypeface(Typeface.MONOSPACE);		
		txt1.setText(new String(transacoes.get(groupPosition).pedido+espacos).substring(0, 7));		
		//txt1.setTextColor(transacoes.get(groupPosition).color.holo_green_dark));
		
		TextView txt2 = new TextView(this.activity);
		txt2.setPadding(75, 15, 0, 0);
		txt2.setTextSize(17);
		txt2.setTypeface(Typeface.DEFAULT_BOLD);
		txt2.setTypeface(Typeface.MONOSPACE);		
		txt2.setText(new String("R$"+transacoes.get(groupPosition).valorPedido.replaceAll("\\.", ",")+espacos).substring(0, 8));
		
		LinearLayout layout1 = new LinearLayout(this.activity);
		layout1.setOrientation(LinearLayout.HORIZONTAL);
		layout1.setPadding(100, 0, 0, 0);
		
		
		LinearLayout layout2 = new LinearLayout(this.activity);
		layout2.setOrientation(LinearLayout.HORIZONTAL);
		
		layout1.addView(imageView);
		layout1.setBackgroundResource(android.R.color.transparent);
		layout1.addView(layout2);
		
		
		layout2.addView(txt1);
		layout2.addView(txt2);
		//layout2.addView(txt3);
			
		return layout1;
	}
	

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

}
