package com.example.test_gate2all;

public class Transacao {
	
	public String adquirente = "";
	public String bandeira = "";
	public String cancelada = "";
	public String capturada = "";
	public String dataTransacao = "";
	public String nome = "";
	public String numCartao = "";
    public String parcelas = "";
    public String pedido = "";
    public String statusTransacao = "";
    public String tid = "";
    public String transacaoId = "";
    public String valorPedido = "";
    
    //constructor
    public Transacao(String adquirente, String bandeira, String cancelada, String capturada, String dataTransacao,
    		String nome, String numCartao, String parcelas, String pedido, String statusTransacao, String tid,
    		String transacaoId, String valorPedido) {
    	
    	this.adquirente = adquirente;
    	this.bandeira = bandeira;
    	this.cancelada = cancelada;
    	this.capturada = capturada;
    	this.dataTransacao = dataTransacao;
    	this.nome = nome;
    	this.numCartao = numCartao;
    	this.parcelas = parcelas;
    	this.pedido = pedido;
    	this.statusTransacao = statusTransacao;
    	this.tid = tid;
    	this.transacaoId = transacaoId;
    	this.valorPedido = valorPedido;
    	
	        
    }

}
