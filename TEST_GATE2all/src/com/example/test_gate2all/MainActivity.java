package com.example.test_gate2all;

import java.sql.Date;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.bbpos.DUKPTServer;
import com.bbpos.TripleDES;
import com.bbpos.emvswipe.EmvSwipeController;
import com.bbpos.emvswipe.EmvSwipeController.BatteryStatus;
import com.bbpos.emvswipe.EmvSwipeController.CheckCardResult;
import com.bbpos.emvswipe.EmvSwipeController.DisplayText;
import com.bbpos.emvswipe.EmvSwipeController.EmvSwipeControllerListener;
import com.bbpos.emvswipe.EmvSwipeController.Error;
import com.bbpos.emvswipe.EmvSwipeController.StartEmvResult;
import com.bbpos.emvswipe.EmvSwipeController.TransactionResult;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

public class MainActivity extends Activity {

	//PageableListView listView = null;
	
	
	private Dialog dialog;
	private ExpandableListView eLView;
	private Dialog confDialog;
	private Dialog searchDialog;
	private TextView statusEditText;
	private EmvSwipeController emvSwipeController;
	private MyEmvSwipeControllerListener listener;
	private static SharedPreferences mAppPreferences;
	private static SharedPreferences.Editor mEditor;
	static ProgressDialog pDialog;
	private Boolean transacionando = false;

	String valorTransacao = "";
	String numeroDocumento = "";
	String qtdeParcelas = "";
	String tipoTransacao = "";
	String bandeira = "";
	String cvv = "";
	String tokenAutenticacao = "";
	Long tokenTime = 0L;
	Integer transacaoPendente = 0;

	String pan = ""; 
	String expiryMonth = ""; 
	String expiryYear = "";  
	String chName = "";

	String qtdRegistros = "10";
	private static String registroInicial = "0";
	private static String registroFinal = "15";
	
	String dataInicial = "2013-01-01";
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
	Date date = new Date(System.currentTimeMillis());
	ImageButton prev=null;
	ExpandableListAdapter adapter = new TransacaoAdapter(this, null);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(isScreenLarge()) {
			// width > height, better to use Landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		listener = new MyEmvSwipeControllerListener();
		emvSwipeController = new EmvSwipeController(this, listener);
		mAppPreferences = getSharedPreferences("CREDENTIALS", MODE_PRIVATE);

		statusEditText = (TextView)findViewById(R.id.txtStatus);


		//Lista de transaçoes
		//adapter = new TransacaoAdapter(this, null);
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		eLView = (ExpandableListView)findViewById(R.id.expLViewTransacoes);
		//eLView.setAdapter(adapter);


		//Botão atualizar
		final ImageButton btnAtualizar = (ImageButton) findViewById(R.id.btnAtualizar);
		btnAtualizar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				int ini=0;
				int fim=15;
				prev.setVisibility(View.INVISIBLE);
				ConsultaTransacoes(qtdRegistros, String.valueOf(ini),String.valueOf(fim), dataInicial, dateFormat.format(date));
			}
		});
		
		
		
		final ImageButton btnConfigurar = (ImageButton) findViewById(R.id.btnSearch);
		btnConfigurar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
			//	showSearch();
			}
		});

		//Botão "Vender"
		final ImageButton btnVender = (ImageButton) findViewById(R.id.btnVender);
		btnVender.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showTransacao();
			}
		});
		
		final ImageButton next =(ImageButton) findViewById(R.id.imgButtonNext);
		next.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				prev.setVisibility(View.VISIBLE);
				int regFinal=Integer.parseInt(registroFinal);				
				registroFinal=String.valueOf(regFinal);
				int regIni =Integer.parseInt(registroInicial)+15;
				registroInicial=String.valueOf(regIni);				
				ConsultaTransacoes(qtdRegistros, registroInicial,registroFinal, dataInicial, dateFormat.format(date));
				
				
			}
		});
		
		
		prev =(ImageButton) findViewById(R.id.imgButtonPrev);
		prev.setVisibility(View.INVISIBLE);
		prev.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int regFinal=Integer.parseInt(registroFinal);				
				registroFinal=String.valueOf(regFinal);
				int regIni =Integer.parseInt(registroInicial)-10;
				registroInicial=String.valueOf(regIni);				
				ConsultaTransacoes(qtdRegistros, registroInicial,registroFinal, dataInicial, dateFormat.format(date));
                 if(registroInicial.equals("0")){
                	 prev.setVisibility(View.INVISIBLE);
				}
				
			}
		});
		
		if(!verificaConexao()){
			showDialogOK(R.string.sem_conexao,R.string.atencao);
			return;
		}else{
			ConsultaTransacoes(qtdRegistros, registroInicial,registroFinal, dataInicial, dateFormat.format(date));
		}

	}

	public boolean isScreenLarge() {
		final int screenSize = getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
		return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
				|| screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	private void showTransacao(){
		if(mAppPreferences.getString("USER", "") == "" || mAppPreferences.getString("SENHA", "") == ""){
			statusEditText.setText(getString(R.string.invalid_credentials));
		}else{
			if (emvSwipeController.isDevicePresent()){

				if(dialog != null) dismissDialog();

				dialog = new Dialog(MainActivity.this);
				dialog.setCanceledOnTouchOutside(false);
				dialog.setContentView(R.layout.activity_dadosvenda);
				dialog.setTitle(R.string.request_data_to_server);

				final EditText edtValorTotal = (EditText) dialog.findViewById(R.id.txtValorTransacao);

				edtValorTotal.setInputType(InputType.TYPE_CLASS_NUMBER);
				//Máscara para valor
				edtValorTotal.addTextChangedListener(new TextWatcher() {

					private boolean isUpdating = false;
					// Pega a formatacao do sistema, se for brasil R$ se EUA US$
					private NumberFormat nf = NumberFormat.getCurrencyInstance();

					@Override
					public void onTextChanged(CharSequence s, int start, int before,
							int after) {
						// Evita que o método seja executado varias vezes.
						// Se tirar ele entre em loop
						if (isUpdating) {
							isUpdating = false;
							return;
						}

						isUpdating = true;
						String str = s.toString();
						// Verifica se já existe a máscara no texto.
						boolean hasMask = ((str.indexOf("R$") > -1 || str.indexOf("$") > -1) &&
								(str.indexOf(".") > -1 || str.indexOf(",") > -1));
						// Verificamos se existe máscara
						if (hasMask) {
							// Retiramos a máscara.
							str = str.replaceAll("[R$]", "").replaceAll("[,]", "")
									.replaceAll("[.]", "");
						}

						try {
							// Transformamos o número que está escrito no EditText em
							// monetário.
							str = nf.format(Double.parseDouble(str) / 100);
							edtValorTotal.setText(str);
							edtValorTotal.setSelection(edtValorTotal.getText().length());
						} catch (NumberFormatException e) {
							s = "";
						}
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						// Não utilizamos
					}

					@Override
					public void afterTextChanged(Editable s) {
						// Não utilizamos
					}
				});

				//Botão "Realizar transação"
	            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);	            
				dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
				dialog.setCancelable(false); 
	            dialog.findViewById(R.id.btnVenderConfirmar).setOnClickListener(new OnClickListener() {
         
					@Override
					public void onClick(View v) {

						if(!transacionando){
							transacionando = true;

							valorTransacao = ((TextView) dialog.findViewById(R.id.txtValorTransacao)).getText().toString();
							numeroDocumento = ((TextView) dialog.findViewById(R.id.txtPedido)).getText().toString();				
							
					          
							tipoTransacao = String.valueOf(((Spinner) dialog.findViewById(R.id.spnTipoTransacao)).getSelectedItemId());
							bandeira = String.valueOf(((Spinner) dialog.findViewById(R.id.spnBandeira)).getSelectedItem());
							cvv = ((TextView) dialog.findViewById(R.id.txtCVV)).getText().toString();
							
							if(!tipoTransacao.equalsIgnoreCase("Crédito à vista")){
								//dialog.findViewById(R.id.spnQtdeParcelas).VISIBLE;
							//	(Spinner) dialog.findViewById(R.id.spnQtdeParcelas).setVisibility(View.GONE);
								qtdeParcelas = String.valueOf(((Spinner) dialog.findViewById(R.id.spnQtdeParcelas)).getSelectedItem());
	
							}
							if(valorTransacao.length() < 3 || numeroDocumento.length() < 3 || cvv.length() < 3){
								showDialogOK(R.string.invalid_data, R.string.fill_form);
								transacionando = false;
							}else{

								valorTransacao = valorTransacao.substring(2);
								//System.out.println("VALORTRANSACAO: " + valorTransacao);
								valorTransacao = valorTransacao.replaceAll(",", "");
								//System.out.println("VALORTRANSACAO: " + valorTransacao);
								valorTransacao = valorTransacao.replaceAll("\\.", "");

								switch(Integer.parseInt(tipoTransacao)){
								case 0:
									tipoTransacao = "1";
									break;
								case 1:
									tipoTransacao = "3";
									break;
								case 2:
									tipoTransacao = "4";
									break;
								default:
									break;

								}

								//1 – Crédito à Vista
								//2 – Débito
								//3 – Parcelado pela Loja
								//4 – Parcelado pela Administradora

								statusEditText.setText(R.string.starting);
								emvSwipeController.checkCard();

							}

						}

					}
				});

				//Botão "Cancelar transação"
				dialog.findViewById(R.id.btnVenderCancelar).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//cancelar venda
						cancelaVenda();
					}
				});

				dialog.show();

			}else{

				showDialogOK(R.string.no_device_detected, R.string.transaction_device_error);
			}
		}
	}

	private void showDialogOK(Integer msg, Integer title){
		AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
		ad.setTitle(getResources().getString(title));
		ad.setMessage(getResources().getString(msg));
		ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (Message) null);
		ad.setCanceledOnTouchOutside(false);
		statusEditText.setText(getResources().getString(msg));
		ad.show();
	}

	private void showConfig(){

		if(confDialog != null) dismissConfDialog();

		confDialog = new Dialog(MainActivity.this);
		confDialog.setContentView(R.layout.activity_configuracao);
		confDialog.setTitle(R.string.insira_credenciais);
		confDialog.setCanceledOnTouchOutside(false);
		confDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		((EditText)confDialog.findViewById(R.id.txtUsuario)).setText(mAppPreferences.getString("USER", ""));

		//Botão "Salvar configuração"
		confDialog.findViewById(R.id.btnConfigurarConfirmar).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				RealizaAutenticacao(
						((EditText)confDialog.findViewById(R.id.txtUsuario)).getText().toString(),
						((EditText)confDialog.findViewById(R.id.txtSenha)).getText().toString()
						);

				confDialog.dismiss();
			}
		});
		
		

		//Botão "Cancelar configuração"
		confDialog.findViewById(R.id.btnConfigurarCancelar).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissConfDialog();
			}
		});

		confDialog.show();
	}
	
	
	private void showSearch(){

		//if(searchDialog != null) dismissConfDialog();

		searchDialog = new Dialog(MainActivity.this);
		searchDialog.setContentView(R.layout.activity_search);
		searchDialog.setTitle("Informe o perído ");
		searchDialog.setCanceledOnTouchOutside(false);
		searchDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		((EditText)searchDialog.findViewById(R.id.txtUsuario)).setText(mAppPreferences.getString("USER", ""));

		
		searchDialog.findViewById(R.id.btnSearch);
		
		//Botão "Salvar configuração"
		/*searchDialog.findViewById(R.id.btnSearch).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				RealizaAutenticacao(
						((EditText)searchDialog.findViewById(R.id.txtUsuario)).getText().toString(),
						((EditText)searchDialog.findViewById(R.id.txtSenha)).getText().toString()
						);

				searchDialog.dismiss();
			}
		});
		
		

		//Botão "Cancelar configuração"
		searchDialog.findViewById(R.id.btnConfigurarCancelar).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissConfDialog();
			}
		});*/

		searchDialog.show();
	}
	
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Captura o menu selecionado
		if(item.getItemId() == R.id.action_refresh){
			//Log.i("INFO", "DAta atual "+dateFormat.format(date));
			//System.out.println("data atual"+dateFormat.format(date));
			ConsultaTransacoes(qtdRegistros, registroInicial,registroFinal, dataInicial, dateFormat.format(date));
			//ConsultaTransacoes(qtdRegistros, registroInicial, dataInicial, dataFinal);
			
		}else if(item.getItemId() == R.id.action_logount){
			//finish();
			sairAplicacao();
		}else{
			showConfig();
		}
		return true;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		emvSwipeController.stopAudio();
		emvSwipeController.resetEmvSwipeController();
	}

	@Override
	public void onResume() {
		super.onResume();
		emvSwipeController.startAudio();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			emvSwipeController.stopAudio();
		} catch(Exception e) {
		}
	}

	public void RealizaTransacao(
			String numeroDocumento, 
			String tipoOperacao, 
			String valorTransacao,
			String qtdeParcelas,
			String numeroCartao,
			String mesValidade,
			String anoValidade,
			String codigoSeguranca,
			String bandeira,
			String nomePortador){

		if(isTokenValid()){

			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setCanceledOnTouchOutside(false);
			pDialog.setMessage("Realizando a transação..."); 
			pDialog.setIndeterminate(false); 
			pDialog.setCancelable(false); 
			pDialog.show(); 

			String xml = "";


			try {
				xml = XML.comporXML(
						mAppPreferences.getString("USER", ""), 
						tokenAutenticacao, 
						numeroDocumento, 
						tipoOperacao, 
						valorTransacao, 
						qtdeParcelas, 
						numeroCartao, 
						mesValidade, 
						anoValidade, 
						codigoSeguranca, 
						bandeira, 
						nomePortador);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



			AndroidHttpClient httpClient = new AndroidHttpClient("https://www.ntk2all.com.br");
			ParameterMap params = httpClient.newParams().add("xml", xml);
			httpClient.setMaxRetries(1);
			httpClient.setConnectionTimeout(5000);
			httpClient.setReadTimeout(30000);
			httpClient.post("/httpRoteador/aprovacao", params, new AsyncCallback() {
				@Override
				public void onComplete(HttpResponse httpResponse) {
					pDialog.dismiss();
					if(httpResponse != null){ 

						String xml = httpResponse.getBodyAsString();
						String codResposta = XML.buscaValorTag(xml, "codResposta");
						String mensagem = XML.buscaValorTag(xml, "message");

						if(!codResposta.equalsIgnoreCase("")){

							if(Integer.parseInt(codResposta) == 0){
								//Transacao autorizada

								statusEditText.setText("");

								String codAutorizacao = XML.buscaValorTag(xml, "codAutorizacao");
								String tid = XML.buscaValorTag(xml, "tid");
								String numeroDocumento = XML.buscaValorTag(xml, "numeroDocumento");

								Intent i = new Intent(MainActivity.this, ComprovanteActivity.class);
								i.putExtra("codAutorizacao", codAutorizacao);
								i.putExtra("tid", tid);
								i.putExtra("numeroDocumento", numeroDocumento);
								i.putExtra("mensagem", mensagem);
								startActivity(i);

							}else{
								//Transacao não autorizada

								AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
								ad.setMessage(mensagem);
								ad.setCanceledOnTouchOutside(false);
								ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (Message) null);
								ad.show();
							}
						}else{

							showDialogOK(R.string.invalid_response_sale, R.string.atencao);
						}
					}else{

						showDialogOK(R.string.check_internet, R.string.atencao);
					}
				}
				@Override
				public void onError(Exception e) {
					pDialog.dismiss();
					e.printStackTrace();
					showDialogOK(R.string.check_internet, R.string.atencao);

				}
			});
		}else{
			transacaoPendente = 1;
			RealizaAutenticacao(mAppPreferences.getString("USER", ""), mAppPreferences.getString("SENHA", ""));
		}
	}

	public void ConsultaTransacoes( 
			String qtdRegistros, 
			String registroInicial, 
			String registroFinal,
			String dataInicial,
			String dataFinal){

		if(isTokenValid()){

			pDialog = new ProgressDialog(MainActivity.this); 
			pDialog.setMessage("Realizando a consulta..."); 
			pDialog.setIndeterminate(false); 
			pDialog.setCancelable(false); 
			pDialog.show(); 

			String xml = "";

			try {
				
				
				/*<transacao>
				 <authentication>
				  <token>ffe3d060-20c0-4415-b44b-c3650ae7d10b</token>
				 </authentication>
				 <dataInicio>2014-01-01</dataInicio>
				 <dataFim>2014-01-06</dataFim>
				 <limitInicio>02</limitInicio>
				 <limitFim>04</limitFim>
				 <colunaOrdenacao>dataTransacao</colunaOrdenacao>
				 <tipoOrdenacao>desc</tipoOrdenacao>
				</transacao>
				*/		
				
				xml = XML.comporXMLConsulta(tokenAutenticacao, dataInicial, dataFinal, "", "", "", registroInicial,registroFinal, qtdRegistros);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			AndroidHttpClient httpClient = new AndroidHttpClient("https://www.ntk2all.com.br");
			httpClient.setMaxRetries(5);
			httpClient.setConnectionTimeout(5000);
			httpClient.setReadTimeout(30000);
			byte[] bArray = xml.getBytes();
			//System.out.println(xml);
			httpClient.post("/gatewaywsdl/transacao/load", "application/xml", bArray, new AsyncCallback() {
				@Override
				public void onComplete(HttpResponse httpResponse) {
					pDialog.dismiss();
					String xml = httpResponse.getBodyAsString();
					
					String status = XML.buscaValorTag(xml, "status");

					if(status.equalsIgnoreCase("0")){
						//Resposta OK
						statusEditText.setText("");

						populaListTransacao(xml);

					}else{
						AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
						ad.setMessage(getString(R.string.invalid_response));
						ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (Message) null);
						ad.setCanceledOnTouchOutside(false);
						ad.show();
						statusEditText.setText(R.string.invalid_response);

					}
				}

				@Override
				public void onError(Exception e) {
					pDialog.dismiss();
					e.printStackTrace();
					showDialogOK(R.string.check_internet, R.string.atencao);
				}
			});

		}else{
			transacaoPendente = 2;
			RealizaAutenticacao(mAppPreferences.getString("USER", ""), mAppPreferences.getString("SENHA", ""));
		}
	}

	public void RealizaAutenticacao(final String usuario, final String senha){

		pDialog = new ProgressDialog(MainActivity.this); 
		pDialog.setMessage("Realizando a autenticação..."); 
		pDialog.setIndeterminate(false); 
		pDialog.setCancelable(false); 
		pDialog.show(); 

		String xml = "";

		try {
			xml = XML.comporXMLAutenticacao(usuario, senha);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AndroidHttpClient httpClient = new AndroidHttpClient("https://www.ntk2all.com.br");
		httpClient.setMaxRetries(5);
		httpClient.setConnectionTimeout(5000);
		httpClient.setReadTimeout(30000);
		byte[] barray = xml.getBytes();
		
		httpClient.post("/gatewaywsdl/authentication/authenticate/", "application/xml", barray, new AsyncCallback() {
			@Override
			public void onComplete(HttpResponse httpResponse) {
				pDialog.dismiss();
				if(httpResponse != null){ 

					String xml = httpResponse.getBodyAsString();
					String status = XML.buscaValorTag(xml, "status");
					String message = XML.buscaValorTag(xml, "message");
					String token = XML.buscaValorTag(xml, "token");

					if(status.equalsIgnoreCase("0")){
						//Transacao autorizada

						statusEditText.setText("");

						tokenAutenticacao = token;
						tokenTime = System.currentTimeMillis();

						//Salva U/S 
						mEditor = mAppPreferences.edit();
						mEditor.putString("USER", usuario);
						mEditor.putString("SENHA", senha);
						mEditor.commit();

						//Reenviar transação pendente???

						switch(transacaoPendente){
						case 1:
							RealizaTransacao(numeroDocumento,
									tipoTransacao,
									valorTransacao,
									qtdeParcelas,
									pan,
									expiryMonth,
									expiryYear,
									cvv,
									bandeira,
									chName);
							break;
						case 2:
							ConsultaTransacoes(qtdRegistros,
									registroInicial,
									registroFinal,
									dataInicial,
									dateFormat.format(date));
							break;
						}						

						transacaoPendente = 0;

					}else{
						//Transacao não autorizada

						AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
						ad.setMessage(message);
						ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (Message) null);
						ad.setCanceledOnTouchOutside(false);
						ad.show();
					}
				}else{

					showDialogOK(R.string.invalid_response, R.string.atencao);

				}
			}
		
			@Override
			public void onError(Exception e) {
				pDialog.dismiss();
				System.out.println(e.getMessage());
				e.printStackTrace();
				showDialogOK(R.string.check_internet, R.string.atencao);

			}
			
		});
	}

	private void populaListTransacao(String xml){		
		adapter = new TransacaoAdapter(this, XML.criaObjetoTransacao(xml));
        
		eLView.setAdapter(adapter);
		((TransacaoAdapter) adapter).notifyDataSetChanged();

	}

	private boolean isTokenValid(){

		Long actualTime = System.currentTimeMillis();
		//System.out.println(actualTime);
		Long diffTime = actualTime - tokenTime;
		//System.out.println(tokenTime);

		//System.out.println(actualTime - tokenTime);
		if(diffTime < 290000){
			return true;
		}else{
			return false;
		}
	}

	public void dismissDialog() {
		if(dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	public void dismissConfDialog() {
		if(confDialog != null) {
			confDialog.dismiss();
			confDialog = null;
		}
	}
	
	public void dismissSearchDialog() {
		if(confDialog != null) {
			confDialog.dismiss();
			confDialog = null;
		}
	}

	class MyEmvSwipeControllerListener implements EmvSwipeControllerListener {

		@Override
		public void onWaitingForCard() {
			//dismissDialog();
			statusEditText.setText(getString(R.string.waiting_for_card));
			dismissDialog();
			dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.activity_cartao);
			dialog.setTitle(R.string.waiting_for_user);
			dialog.show();
			//dialog.findViewById(R.id.btnVenderConfirmar).setEnabled(true);
		}

		@Override
		public void onReturnCheckCardResult(CheckCardResult checkCardResult, Hashtable<String, String> decodeData) {
			dismissDialog();
			transacionando = false;
			if(checkCardResult == CheckCardResult.NONE) {
				statusEditText.setText(getString(R.string.no_card_detected));
			} else if(checkCardResult == CheckCardResult.ICC) {
				statusEditText.setText(getString(R.string.icc_card_inserted));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//emvSwipeController.startEmv(EmvOption.START);
			} else if(checkCardResult == CheckCardResult.NOT_ICC) {
				statusEditText.setText(getString(R.string.card_inserted));
			} else if(checkCardResult == CheckCardResult.BAD_SWIPE) {
				statusEditText.setText(getString(R.string.bad_swipe));
				//dialog.show();
				//System.out.println("BAD SWIPE");
				emvSwipeController.checkCard();
			} else if(checkCardResult == CheckCardResult.NO_RESPONSE) {
				statusEditText.setText(getString(R.string.card_no_response));
			} else if(checkCardResult == CheckCardResult.MCR ||
					checkCardResult == CheckCardResult.TRACK2_ONLY ||
					checkCardResult == CheckCardResult.NFC_TRACK2) {

				//String formatID = decodeData.get("formatID");
				//String maskedPAN = decodeData.get("maskedPAN");
				String expiryDate = decodeData.get("expiryDate");
				String cardHolderName = decodeData.get("cardholderName");
				String ksn = decodeData.get("ksn");
				//String serviceCode = decodeData.get("serviceCode");
				//String track1Length = decodeData.get("track1Length");
				//String track2Length = decodeData.get("track2Length");
				//String track3Length = decodeData.get("track3Length");
				String encTracks = decodeData.get("encTracks");
				String encTrack1 = decodeData.get("encTrack1");
				String encTrack2 = decodeData.get("encTrack2");
				//String encTrack3 = decodeData.get("encTrack3");
				//String partialTrack = decodeData.get("partialTrack");

				
                
				String bdk = "0123456789ABCDEFFEDCBA9876543210";
				String key = DUKPTServer.GetDataKeyVar(ksn, bdk);
				String decryptedTLV = TripleDES.decrypt(encTrack1, key);
				
				String decryptedTrack = unPadData(decryptedTLV);
				
				pan = getPan(decryptedTrack);
				
				//System.out.println("PAN: " + pan);
						

				if(expiryDate == null || cardHolderName == null || pan == ""){
					statusEditText.setText(getString(R.string.bad_swipe));
					//dialog.show();
					
					System.out.println("SOME DATA IS NULL");
					System.out.println("TRACK: " + decryptedTrack);
					System.out.println("CARDDATE: " + expiryDate);
					System.out.println("CHNAME:" + cardHolderName);
					emvSwipeController.checkCard();
				}else{

					if(numeroDocumento == "" 
							|| tipoTransacao == "" 
							|| valorTransacao == "" 
							||	qtdeParcelas == "" 
							|| bandeira == ""
							|| cardHolderName == ""
							|| expiryDate == ""){

						statusEditText.setText(getString(R.string.bad_swipe));
						System.out.println("SOME DATA IS EMPTY");
						System.out.println("TIPOTRANSACAO: " + tipoTransacao);
						System.out.println("VALORTRANSACAO: " + valorTransacao);
						System.out.println("QTDEPARCELAS: " + qtdeParcelas);
						System.out.println("BANDEIRA: " + bandeira);
						System.out.println("CHNAME: " + cardHolderName);
						System.out.println("EXPIRYDATE: " + expiryDate);
						//dialog.show();
						emvSwipeController.checkCard();
					}else{

						expiryMonth = expiryDate.substring(2, 4); 
						expiryYear = String.valueOf("20"+expiryDate.substring(0, 2));  
						chName = cardHolderName;

						RealizaTransacao(
								numeroDocumento, 
								tipoTransacao, 
								valorTransacao, 
								qtdeParcelas, 
								pan, 
								expiryMonth, 
								expiryYear, 
								cvv, 
								bandeira, 
								chName);



					/*	System.out.println("NomePortador: " + cardHolderName);
						System.out.println("AnoValidade: " + String.valueOf("20"+expiryDate.substring(0, 2)));
						System.out.println("MêsValidade: " + expiryDate.substring(2, 4));
						System.out.println("NumeroDocumento: " + numeroDocumento);
						System.out.println("TipoTransacao: " + tipoTransacao);
						System.out.println("ValorTransacao: " + valorTransacao);
						System.out.println("Parcelas: " + qtdeParcelas);
						System.out.println("Bandeira: " + bandeira);
						System.out.println("CVV: " + cvv);
*/
						/*numeroDocumento = "";
						tipoTransacao = "";
						valorTransacao = "";
						qtdeParcelas = "";
						bandeira = "";*/

					}
				}
			}
		}

		@Override
		public void onRequestTerminalTime() {
			dismissDialog();
			String terminalTime = new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime());
			emvSwipeController.sendTerminalTime(terminalTime);
			//statusEditText.setText(getString(R.string.request_terminal_time) + " " + terminalTime);
		}

		@Override
		public void onRequestDisplayText(DisplayText displayText) {
			dismissDialog();

			String msg = "";
			if(displayText == DisplayText.AMOUNT) {
				msg = getString(R.string.amount);
			} else if(displayText == DisplayText.AMOUNT_OK_OR_NOT) {
				msg = getString(R.string.amount_ok);
			} else if(displayText == DisplayText.APPROVED) {
				msg = getString(R.string.approved);
			} else if(displayText == DisplayText.CALL_YOUR_BANK) {
				msg = getString(R.string.call_your_bank);
			} else if(displayText == DisplayText.CANCEL_OR_ENTER) {
				msg = getString(R.string.cancel_or_enter);
			} else if(displayText == DisplayText.CARD_ERROR) {
				msg = getString(R.string.card_error);
			} else if(displayText == DisplayText.DECLINED) {
				msg = getString(R.string.decline);
			} else if(displayText == DisplayText.ENTER_AMOUNT) {
				msg = getString(R.string.enter_amount);
			} else if(displayText == DisplayText.ENTER_PIN) {
				msg = getString(R.string.enter_pin);
			} else if(displayText == DisplayText.INCORRECT_PIN) {
				msg = getString(R.string.incorrect_pin);
			} else if(displayText == DisplayText.INSERT_CARD) {
				msg = getString(R.string.insert_card);
			} else if(displayText == DisplayText.NOT_ACCEPTED) {
				msg = getString(R.string.not_accepted);
			} else if(displayText == DisplayText.PIN_OK) {
				msg = getString(R.string.pin_ok);
			} else if(displayText == DisplayText.PLEASE_WAIT) {
				msg = getString(R.string.wait); 
			} else if(displayText == DisplayText.PROCESSING_ERROR) {
				msg = getString(R.string.processing_error);
			} else if(displayText == DisplayText.REMOVE_CARD) {
				msg = getString(R.string.remove_card);
			} else if(displayText == DisplayText.USE_CHIP_READER) {
				msg = getString(R.string.use_chip_reader);
			} else if(displayText == DisplayText.USE_MAG_STRIPE) {
				msg = getString(R.string.use_mag_stripe);
			} else if(displayText == DisplayText.TRY_AGAIN) {
				msg = getString(R.string.try_again);
			} else if(displayText == DisplayText.REFER_TO_YOUR_PAYMENT_DEVICE) {
				msg = getString(R.string.refer_payment_device);
			} else if(displayText == DisplayText.TRANSACTION_TERMINATED) {
				msg = getString(R.string.transaction_terminated);
			} else if(displayText == DisplayText.TRY_ANOTHER_INTERFACE) {
				msg = getString(R.string.try_another_interface);
			} else if(displayText == DisplayText.ONLINE_REQUIRED) {
				msg = getString(R.string.online_required);
			} else if(displayText == DisplayText.PROCESSING) {
				msg = getString(R.string.processing);
			} else if(displayText == DisplayText.WELCOME) {
				msg = getString(R.string.welcome);
			} else if(displayText == DisplayText.PRESENT_ONLY_ONE_CARD) {
				msg = getString(R.string.present_one_card);
			} else if(displayText == DisplayText.CAPK_LOADING_FAILED) {
				msg = getString(R.string.capk_failed);
			} else if(displayText == DisplayText.LAST_PIN_TRY) {
				msg = getString(R.string.last_pin_try);
			}

			statusEditText.setText(msg);
		}

		@Override
		public void onBatteryLow(BatteryStatus batteryStatus) {
			if(batteryStatus == BatteryStatus.LOW) {
				statusEditText.setText(getString(R.string.battery_low));
			} else if(batteryStatus == BatteryStatus.CRITICALLY_LOW) {
				statusEditText.setText(getString(R.string.battery_critically_low));
			}
		}

		@Override
		public void onNoDeviceDetected() {
			dismissDialog();
			statusEditText.setText(getString(R.string.no_device_detected));
		}

		@Override
		public void onDevicePlugged() {
			dismissDialog();
			statusEditText.setText(getString(R.string.device_plugged));
		}

		@Override
		public void onDeviceUnplugged() {
			dismissDialog();
			statusEditText.setText(getString(R.string.device_unplugged));
		}

		@Override
		public void onError(Error errorState) {
			dismissDialog();
			//amountEditText.setText("");
			if(errorState == Error.CMD_NOT_AVAILABLE) {
				statusEditText.setText(getString(R.string.command_not_available));
			} else if(errorState == Error.TIMEOUT) {
				statusEditText.setText(getString(R.string.device_no_response));
			} else if(errorState == Error.DEVICE_RESET) {
				statusEditText.setText(getString(R.string.device_reset));
			} else if(errorState == Error.UNKNOWN) {
				statusEditText.setText(getString(R.string.unknown_error));
			} else if(errorState == Error.DEVICE_BUSY) {
				statusEditText.setText(getString(R.string.device_busy));
			} else if(errorState == Error.INPUT_OUT_OF_RANGE) {
				statusEditText.setText(getString(R.string.out_of_range));
			} else if(errorState == Error.INPUT_INVALID_FORMAT) {
				statusEditText.setText(getString(R.string.invalid_format));
			} else if(errorState == Error.INPUT_ZERO_VALUES) {
				statusEditText.setText(getString(R.string.zero_values));
			} else if(errorState == Error.INPUT_INVALID) {
				statusEditText.setText(getString(R.string.input_invalid));
			} else if(errorState == Error.CASHBACK_NOT_SUPPORTED) {
				statusEditText.setText(getString(R.string.cashback_not_supported));
			} else if(errorState == Error.CRC_ERROR) {
				statusEditText.setText(getString(R.string.crc_error));
			} else if(errorState == Error.COMM_ERROR) {
				statusEditText.setText(getString(R.string.comm_error));
			}
		}

		@Override
		public void onRequestClearDisplay() {
			dismissDialog();
			statusEditText.setText("");
		}

		@Override
		public void onPowerDown() {
			statusEditText.setText(getString(R.string.device_off));
		}

		private Integer h2c(char input)
		{
			if ((input >= '0') && (input <= '9'))
				return input-'0';
			else if ((input == 'a') || (input == 'A'))
				return 10;
			else if ((input == 'b') || (input == 'B'))
				return 11;
			else if ((input == 'c') || (input == 'C'))
				return 12;
			else if ((input == 'd') || (input == 'D'))
				return 13;
			else if ((input == 'e') || (input == 'E'))
				return 14;
			else if ((input == 'f') || (input == 'F'))
				return 15;
			else
				return -1;
		}
		
		private String unPadData(String input){
			
			char[] ucTemp = new char[512];
			char[] ucUnPad = new char[96];
			int length;



			Integer iTemp = 0;
			Integer iTemp2 = 0;
			Integer i = 0;
			Integer j = 0;

			
			try {

				while (i< input.length()) {
					iTemp=h2c(input.charAt(i));
					if (iTemp>-1){
						// first digit is number
						if  (i+1 < input.length()){
							i++;
							iTemp2 = h2c(input.charAt(i));
							if (iTemp2 >-1)
							{
								ucTemp[j] = Character.toChars((iTemp*16 + iTemp2)&0xff)[0];
								//System.out.println(ucTemp[j]);
								j++;
							}
						}	
					}
					i++;
				}

				for (i=0, j=0; i<64; i+=3, j+=4)
				{
					ucUnPad[j] = (char) ((ucTemp[i] >> 2) & 0x3F);
					ucUnPad[j+1] = (char) (((ucTemp[i] << 4) & 0x30) | ((ucTemp[i+1] >> 4) & 0x0F));
					ucUnPad[j+2] = (char) (((ucTemp[i+1] << 2) & 0x3C) | ((ucTemp[i+2] >> 6) & 0x03));
					ucUnPad[j+3] = (char) (ucTemp[i+2] & 0x3F);
				}

				for (i=0; i<84; i++)
				{
					ucUnPad[i] += 0x20;
					if (ucUnPad[i] == 0x3F)
					{
						length = i+1;
						ucUnPad[length] = 0;
						//System.out.println("DEC-TRACK: " + String.valueOf(ucUnPad));
					}
				}

			}catch(Exception e){
				//Do
				//System.out.println("ERRO:" + e.getMessage());
				return "";
			}
			return String.valueOf(ucUnPad);
		}
		
		private String getPan(String track){
			try {
				return track.substring(track.indexOf("B")+1,track.indexOf("^"));
			} catch (Exception e) {
				return "";
			}	
		}




		//Não implementado daqui para baixo!!!!!




		@Override
		public void onRequestReferProcess(String pan) {
			dismissDialog();
			/*dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.refer_process_dialog);
			dialog.setTitle(getString(R.string.call_your_bank));

			dialog.findViewById(R.id.approvedButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					emvSwipeController.sendReferProcessResult(ReferralResult.APPROVED);
				}
			});

			dialog.findViewById(R.id.declinedButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					emvSwipeController.sendReferProcessResult(ReferralResult.DECLINED);
				}
			});

			dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					emvSwipeController.cancelReferProcess();
				}
			});*/
		}

		@Override
		public void onRequestAdviceProcess(String tlv) {
			dismissDialog();
			//statusEditText.setText(getString(R.string.advice_process));
		}

		@Override
		public void onRequestFinalConfirm() {
			dismissDialog();
			/*if(!isPinCanceled) {
				dialog = new Dialog(MainActivity.this);
				dialog.setContentView(R.layout.confirm_dialog);
				dialog.setTitle(getString(R.string.confirm_amount));

				String message = getString(R.string.amount) + ": $" + amount;
				if(!cashbackAmount.equals("")) {
					message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmount;
				}

				((TextView)dialog.findViewById(R.id.messageTextView)).setText(message);

				dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						emvSwipeController.sendFinalConfirmResult(true);
						dialog.dismiss();
					}
				});

				dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						emvSwipeController.sendFinalConfirmResult(false);
						dialog.dismiss();
					}
				});

				dialog.show();
			} else {
				emvSwipeController.sendFinalConfirmResult(false);
			}*/
		}

		@Override
		public void onRequestPinEntry() {
			dismissDialog();

			/*dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.pin_dialog);
			dialog.setTitle(getString(R.string.enter_pin));

			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String pin = ((EditText)dialog.findViewById(R.id.pinEditText)).getText().toString();
					emvSwipeController.sendPinEntryResult(pin);
					dismissDialog();
				}
			});

			dialog.findViewById(R.id.bypassButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					emvSwipeController.bypassPinEntry();
					dismissDialog();
				}
			});

			dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					isPinCanceled = true;
					emvSwipeController.cancelPinEntry();
					dismissDialog();
				}
			});

			dialog.show();*/
		}

		@Override
		public void onReturnStartEmvResult(StartEmvResult startEmvResult, String ksn) {
			if(startEmvResult == StartEmvResult.SUCCESS) {
				//statusEditText.setText(getString(R.string.start_emv_success));
			} else {
				//statusEditText.setText(getString(R.string.start_emv_fail));
			}
		}

		@Override
		public void onReturnDeviceInfo(Hashtable<String, String> deviceInfoData) {
			String isSupportedTrack1 = deviceInfoData.get("isSupportedTrack1") == null? "" : deviceInfoData.get("isSupportedTrack1");
			String isSupportedTrack2 = deviceInfoData.get("isSupportedTrack2") == null? "" : deviceInfoData.get("isSupportedTrack2");
			String isSupportedTrack3 = deviceInfoData.get("isSupportedTrack3") == null? "" : deviceInfoData.get("isSupportedTrack3");
			String bootloaderVersion = deviceInfoData.get("bootloaderVersion") == null? "" : deviceInfoData.get("bootloaderVersion");
			String firmwareVersion = deviceInfoData.get("firmwareVersion") == null? "" : deviceInfoData.get("firmwareVersion");
			String isUsbConnected = deviceInfoData.get("isUsbConnected") == null? "" : deviceInfoData.get("isUsbConnected");
			String isCharging = deviceInfoData.get("isCharging") == null? "" : deviceInfoData.get("isCharging");
			String batteryLevel = deviceInfoData.get("batteryLevel") == null? "" : deviceInfoData.get("batteryLevel");
			String hardwareVersion = deviceInfoData.get("hardwareVersion") == null? "" : deviceInfoData.get("hardwareVersion");

			/*String content = "";
			content += getString(R.string.bootloader_version) + bootloaderVersion + "\n";
			content += getString(R.string.firmware_version) + firmwareVersion + "\n";
			content += getString(R.string.usb) + isUsbConnected + "\n";
			content += getString(R.string.charge) + isCharging + "\n";
			content += getString(R.string.battery_level) + batteryLevel + "\n";
			content += getString(R.string.hardware_version) + hardwareVersion + "\n";
			content += getString(R.string.track_1_supported) + isSupportedTrack1 + "\n";
			content += getString(R.string.track_2_supported) + isSupportedTrack2 + "\n";
			content += getString(R.string.track_3_supported) + isSupportedTrack3 + "\n";

			statusEditText.setText(content);*/
		}

		@Override
		public void onReturnTransactionResult(TransactionResult transactionResult) {
			dismissDialog();
			//statusEditText.setText("");
			dialog = new Dialog(MainActivity.this);
			/*dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.transaction_result);
			TextView messageTextView = (TextView)dialog.findViewById(R.id.messageTextView);

			if(transactionResult == TransactionResult.APPROVED) {
				String message = getString(R.string.transaction_approved) + "\n"
						+ getString(R.string.amount) + ": $" + amount + "\n";
				if(!cashbackAmount.equals("")) {
					message += getString(R.string.cashback_amount) + ": $" + cashbackAmount;
				}
				messageTextView.setText(message);
			} else if(transactionResult == TransactionResult.TERMINATED) {
				messageTextView.setText(getString(R.string.transaction_terminated));
			} else if(transactionResult == TransactionResult.DECLINED) {
				messageTextView.setText(getString(R.string.transaction_declined));
			} else if(transactionResult == TransactionResult.CANCEL) {
				messageTextView.setText(getString(R.string.transaction_cancel));
			} else if(transactionResult == TransactionResult.CAPK_FAIL) {
				messageTextView.setText(getString(R.string.transaction_capk_fail));
			} else if(transactionResult == TransactionResult.NOT_ICC) {
				messageTextView.setText(getString(R.string.transaction_not_icc));
			} else if(transactionResult == TransactionResult.SELECT_APP_FAIL) {
				messageTextView.setText(getString(R.string.transaction_app_fail));
			} else if(transactionResult == TransactionResult.DEVICE_ERROR) {
				messageTextView.setText(getString(R.string.transaction_device_error));
			} else if(transactionResult == TransactionResult.CARD_NOT_SUPPORTED) {
				messageTextView.setText(getString(R.string.card_not_supported));
			} else if(transactionResult == TransactionResult.MISSING_MANDATORY_DATA) {
				messageTextView.setText(getString(R.string.missing_mandatory_data));
			} else if(transactionResult == TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS) {
				messageTextView.setText(getString(R.string.card_blocked_or_no_evm_apps));
			} else if(transactionResult == TransactionResult.INVALID_ICC_DATA) {
				messageTextView.setText(getString(R.string.invalid_icc_data));
			}

			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dismissDialog();
				}
			});
			 */
			//dialog.show();

			//amount = "";
			//cashbackAmount = "";
			//amountEditText.setText("");
		}

		@Override
		public void onReturnBatchData(String tlv) {
			dismissDialog();
			//String content = getString(R.string.batch_data);
			//content += tlv;
			//statusEditText.setText(content);
		}

		@Override
		public void onReturnTransactionLog(String tlv) {
			dismissDialog();
			//String content = getString(R.string.transaction_log);
			//content += tlv;
			//statusEditText.setText(content);
		}

		@Override
		public void onReturnReversalData(String tlv) {
			dismissDialog();
			//String content = getString(R.string.reversal_data);
			//content += tlv;
			//statusEditText.setText(content);			
		}

		@Override
		public void onReturnPowerOnIccResult(boolean isSuccess, String ksn, String atr, int atrLength) {
		}

		@Override
		public void onReturnPowerOffIccResult(boolean isSuccess) {
		}

		@Override
		public void onReturnApduResult(boolean isSuccess, String apdu, int apduLength) {
		}

		@Override
		public void onReturnNfcDataResult(boolean isSuccess, String response, int responseLength) {
		}

		@Override
		public void onReturnPowerOffNfcResult(boolean isSuccess) {
		}

		@Override
		public void onReturnPowerOnNfcResult(boolean isSuccess, String data, int dataLength) {
		}

		@Override
		public void onReturnKsn(Hashtable<String, String> ksnTable) {
			String pinKsn = ksnTable.get("pinKsn") == null? "" : ksnTable.get("pinKsn");
			String trackKsn = ksnTable.get("trackKsn") == null? "" : ksnTable.get("trackKsn");
			String emvKsn = ksnTable.get("emvKsn") == null? "" : ksnTable.get("emvKsn");
			String uid = ksnTable.get("uid") == null? "" : ksnTable.get("uid");

			/*String content = "";
			content += getString(R.string.pin_ksn) + pinKsn + "\n";
			content += getString(R.string.track_ksn) + trackKsn + "\n";
			content += getString(R.string.emv_ksn) + emvKsn + "\n";
			content += getString(R.string.uid) + uid + "\n";

			statusEditText.setText(content);*/
		}

		@Override
		public void onRequestSelectApplication(ArrayList<String> appList) {
			dismissDialog();

			/*dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.application_dialog);
			dialog.setTitle(R.string.please_select_app);

			String[] appNameList = new String[appList.size()];
			for(int i = 0; i < appNameList.length; ++i) {
				appNameList[i] = appList.get(i);
			}

			appListView = (ListView)dialog.findViewById(R.id.appList);
			appListView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, appNameList));
			appListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					emvSwipeController.selectApplication(position);
					dismissDialog();
				}

			});

			dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					emvSwipeController.cancelSelectApplication();
					dismissDialog();
				}
			});
			dialog.show();*/
		}

		@Override
		public void onRequestSetAmount() {
			dismissDialog();
			/*dialog = new Dialog(MainActivity.this);
    		dialog.setContentView(R.layout.amount_dialog);
    		dialog.setTitle(getString(R.string.set_amount));

    		String[] transactionTypes = new String[] {
    				"GOODS",
    				"SERVICES",
    				"CASHBACK",
    				"INQUIRY",
    				"TRANSFER",
    				"PAYMENT"
    		};
    		((Spinner)dialog.findViewById(R.id.transactionTypeSpinner)).setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, transactionTypes));

    		dialog.findViewById(R.id.setButton).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String amount = ((EditText)(dialog.findViewById(R.id.amountEditText))).getText().toString();
					String cashbackAmount = ((EditText)(dialog.findViewById(R.id.cashbackAmountEditText))).getText().toString();
					String transactionTypeString = (String)((Spinner)dialog.findViewById(R.id.transactionTypeSpinner)).getSelectedItem();

					TransactionType transactionType = TransactionType.GOODS;
					if(transactionTypeString.equals("GOODS")) {
						transactionType = TransactionType.GOODS;
					} else if(transactionTypeString.equals("SERVICES")) {
						transactionType = TransactionType.SERVICES;
					} else if(transactionTypeString.equals("CASHBACK")) {
						transactionType = TransactionType.CASHBACK;
					} else if(transactionTypeString.equals("INQUIRY")) {
						transactionType = TransactionType.INQUIRY;
					} else if(transactionTypeString.equals("TRANSFER")) {
						transactionType = TransactionType.TRANSFER;
					} else if(transactionTypeString.equals("PAYMENT")) {
						transactionType = TransactionType.PAYMENT;
					}

					amountEditText.setText("$" + amount);
					emvSwipeController.setAmount(amount, cashbackAmount, "384", transactionType);
					MainActivity.this.amount = amount;
					MainActivity.this.cashbackAmount = cashbackAmount;
					dismissDialog();
				}

    		});

    		dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					emvSwipeController.cancelSetAmount();
					dialog.dismiss();
				}

    		});

    		dialog.show();*/
		}

		@Override
		public void onRequestCheckServerConnectivity() {
			dismissDialog();
			/*dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.online_process_requested);

			((TextView)dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_connected);

			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					emvSwipeController.sendServerConnectivity(true);
					dismissDialog();
				}
			});

			dialog.show();*/
		}

		@Override
		public void onRequestOnlineProcess(String tlv) {
			dismissDialog();
			/*dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.request_data_to_server);

			if(isPinCanceled) {
				((TextView)dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_failed);
			} else {
				((TextView)dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_success);
			}

			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(isPinCanceled) {
						emvSwipeController.sendOnlineProcessResult(null);
					} else {
						emvSwipeController.sendOnlineProcessResult("8A023030");
					}
					dismissDialog();
				}
			});

			dialog.show();*/
		}
	}
	
	public  boolean verificaConexao() {  
	    boolean conectado;  
	    ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
	    if (conectivtyManager.getActiveNetworkInfo() != null  
	            && conectivtyManager.getActiveNetworkInfo().isAvailable()  
	            && conectivtyManager.getActiveNetworkInfo().isConnected()) {  
	        conectado = true;  
	    } else {  
	        conectado = false;  
	    }  
	    return conectado;  
	}  	
	

	private void sairAplicacao() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 builder.setTitle("  Finalizar"); 
		  builder.setMessage("Deseja sair da aplicação ?"); 
		 builder.setPositiveButton("Sair", new DialogInterface.OnClickListener() {
			 
			 public void onClick(DialogInterface arg0, int arg1) { 
				 finish();
				 } });
		 builder.setNegativeButton("Não", new DialogInterface.OnClickListener() { 
			 public void onClick(DialogInterface arg0, int arg1) {
				 return;
			 } }); 
		 
		 AlertDialog alerta = builder.create(); //Exibe 
		 alerta.show(); 
		 }
	
	private void cancelaVenda() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 builder.setTitle("  Cancelar"); 
		  builder.setMessage("Cancelar venda ?"); 
		  builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
			 
			 public void onClick(DialogInterface arg0, int arg1) { 
				    valorTransacao = "";
					numeroDocumento = "";
					qtdeParcelas = "";
					tipoTransacao = "";
					bandeira = "";				  
					dismissDialog();
				 } });
		 
		 builder.setNegativeButton("Não", new DialogInterface.OnClickListener() { 
			 public void onClick(DialogInterface arg0, int arg1) {
				 return;
			 } }); 
		 
		 AlertDialog alerta = builder.create(); //Exibe 
		 alerta.show(); 
		 }
	
	
	public void onBackPressed()  { 
		
		}
	
	/* public void onAttachedToWindow() {
	      
	      this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	      super.onAttachedToWindow();
	      
	      }*/
}
