package com.example.test_gate2all;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

public class ComprovanteActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comprovante);
		
		if(isScreenLarge()) {
	        // width > height, better to use Landscape
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    } else {
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
		
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    //String idTransacao = XML.BuscaValorTag(xml, "id_transacao");
		    String mensagem = extras.getString("mensagem");
		    String codAutorizacao = extras.getString("codAutorizacao");
		    String tid = extras.getString("tid");
		    String numeroDocumento = extras.getString("numeroDocumento");
		    
		    //Popular tela com os dados do comprovante
		    
		    TextView txtPedido = (TextView)findViewById(R.id.txtComprovantePedido); 
		    TextView txtMensagem = (TextView)findViewById(R.id.txtComprovanteMensagem);
		    TextView txtTID = (TextView)findViewById(R.id.txtComprovanteTID);
		    TextView txtAutorizacao = (TextView)findViewById(R.id.txtComprovanteAutorizacao);
		    
		    txtPedido.setText(numeroDocumento);
		    txtMensagem.setText(mensagem);
		    txtTID.setText(tid);
		    txtAutorizacao.setText(codAutorizacao);
		    
		    
		}

	}
	

	public boolean isScreenLarge() {
	    final int screenSize = getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK;
	    return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
	            || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}
	
}
