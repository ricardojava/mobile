package com.example.test_gate2all;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public class XML {

	public static String buscaValorTag(String xml, String tag){
		try{
			Document doc = getDomElement(xml);
			return doc.getElementsByTagName(tag).item(0).getFirstChild().getNodeValue();
		}catch(Exception e){
			return "";
		}
	}
	
	public static String buscaValorTagOfElement(Document doc, String xml, String tag, Integer element){
		try{
			//Document doc = getDomElement(xml);
			return doc.getElementsByTagName(tag).item(element).getFirstChild().getNodeValue();
		}catch(Exception e){
			return "";
		}
	}

	public static ArrayList<Transacao> criaObjetoTransacao(String xml){

		try{
			ArrayList<Transacao> transacao = new ArrayList<Transacao>();
			Document doc = getDomElement(xml);
			NodeList nl = doc.getElementsByTagName("transacoes");

			for (int i = 0; i < nl.getLength(); i++) {
				Transacao t;

				t = new Transacao(
						buscaValorTagOfElement(doc, xml, "adquirente", i),
						buscaValorTagOfElement(doc, xml, "bandeira", i),
						buscaValorTagOfElement(doc, xml, "cancelada", i),
						buscaValorTagOfElement(doc, xml, "capturada", i),
						buscaValorTagOfElement(doc, xml, "dataTransacao", i),
						buscaValorTagOfElement(doc, xml, "nome", i),
						buscaValorTagOfElement(doc, xml, "numCartao", i),
						buscaValorTagOfElement(doc, xml, "parcelas", i),
						buscaValorTagOfElement(doc, xml, "pedido", i),
						buscaValorTagOfElement(doc, xml, "statusTransacao", i),
						buscaValorTagOfElement(doc, xml, "tId", i),
						buscaValorTagOfElement(doc, xml, "transacaoId", i),
						buscaValorTagOfElement(doc, xml, "valorPedido", i)/*,
						String.valueOf(doc.getElementsByTagName("bandeira").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("cancelada").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("capturada").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("dataTransacao").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("nome").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("numCartao").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("parcelas").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("pedido").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("statusTransacao").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("tId").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("transacaoId").item(i).getFirstChild().getNodeValue()),
						String.valueOf(doc.getElementsByTagName("valorPedido").item(i).getFirstChild().getNodeValue())*/
						);
				transacao.add(t);
			}

			return transacao;

		}catch(Exception e){
			//System.out.println(xml);
			return new ArrayList<Transacao>();
			
		}
	}

	public static String comporXML(
			String usuario, 
			String token, 
			String numeroDocumento, 
			String tipoOperacao, 
			String valorTransacao, 
			String qtdeParcelas, 
			String numeroCartao, 
			String mesValidade, 
			String anoValidade, 
			String codigoSeguranca, 
			String bandeira, 
			String nomePortador) throws Exception {

		XmlSerializer xmlSerializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();

		xmlSerializer.setOutput(writer);
		// start DOCUMENT
		xmlSerializer.startDocument("ISO-8859-1", true);
		xmlSerializer.startTag("", "transacao");
		xmlSerializer.attribute("", "versao", "1.1.2");

		xmlSerializer.startTag("", "usuario");
		xmlSerializer.text(usuario);
		xmlSerializer.endTag("", "usuario");

		xmlSerializer.startTag("", "token");
		xmlSerializer.text(token);
		xmlSerializer.endTag("", "token");

		xmlSerializer.startTag("", "numeroDocumento");
		xmlSerializer.text(numeroDocumento);
		xmlSerializer.endTag("", "numeroDocumento");

		xmlSerializer.startTag("", "tipoOperacao");
		xmlSerializer.text(tipoOperacao);
		xmlSerializer.endTag("", "tipoOperacao");

		xmlSerializer.startTag("", "valorTransacao");
		xmlSerializer.text(valorTransacao);
		xmlSerializer.endTag("", "valorTransacao");

		xmlSerializer.startTag("", "qtdeParcelas");
		xmlSerializer.text(qtdeParcelas);
		xmlSerializer.endTag("", "qtdeParcelas");

		xmlSerializer.startTag("", "moeda");
		xmlSerializer.text("986");
		xmlSerializer.endTag("", "moeda");

		xmlSerializer.startTag("", "captura");
		xmlSerializer.text("S");
		xmlSerializer.endTag("", "captura");

		xmlSerializer.startTag("", "cartao");

		xmlSerializer.startTag("", "numeroCartao");
		xmlSerializer.text(numeroCartao);
		xmlSerializer.endTag("", "numeroCartao");

		xmlSerializer.startTag("", "mesValidade");
		xmlSerializer.text(mesValidade);
		xmlSerializer.endTag("", "mesValidade");

		xmlSerializer.startTag("", "anoValidade");
		xmlSerializer.text(anoValidade);
		xmlSerializer.endTag("", "anoValidade");

		xmlSerializer.startTag("", "codigoSeguranca");
		xmlSerializer.text(codigoSeguranca);
		xmlSerializer.endTag("", "codigoSeguranca");

		xmlSerializer.startTag("", "bandeira");
		xmlSerializer.text(bandeira);
		xmlSerializer.endTag("", "bandeira");

		xmlSerializer.endTag("", "cartao");

		xmlSerializer.startTag("", "comprador");

		xmlSerializer.startTag("", "nomePortadorCartao");
		xmlSerializer.text(nomePortador);
		xmlSerializer.endTag("", "nomePortadorCartao");

		xmlSerializer.endTag("", "comprador");

		// end DOCUMENT
		xmlSerializer.endDocument();

		return writer.toString();

	}

	public static String comporXMLAutenticacao(
			String usuario, 
			String senha
			) throws Exception {

		XmlSerializer xmlSerializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();

		xmlSerializer.setOutput(writer);
		// start DOCUMENT
		xmlSerializer.startDocument("ISO-8859-1", true);
		xmlSerializer.startTag("", "usuario");
		//xmlSerializer.attribute("", "versao", "1.1.2");

		xmlSerializer.startTag("", "usuario");
		xmlSerializer.text(usuario);
		xmlSerializer.endTag("", "usuario");


		xmlSerializer.startTag("", "senha");
		xmlSerializer.text(senha);
		xmlSerializer.endTag("", "senha");

		xmlSerializer.endTag("", "usuario");

		// end DOCUMENT
		xmlSerializer.endDocument();

		return writer.toString();


	}

	public static String comporXMLConsulta(
			String tokenAutenticacao, 
			String dataInicio, 
			String dataFim, 
			String codAutorizacao, 
			String cartao, 
			String pedido, 
			String limitInicio, 
			String limitFim, 
			String qtdeRegistro 
			
			) throws Exception {

		XmlSerializer xmlSerializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();

		xmlSerializer.setOutput(writer);
		// start DOCUMENT
		xmlSerializer.startDocument("ISO-8859-1", true);
		xmlSerializer.startTag("", "transacao");
		//xmlSerializer.attribute("", "versao", "1.1.2");

		xmlSerializer.startTag("", "authentication");

		xmlSerializer.startTag("", "token");
		xmlSerializer.text(tokenAutenticacao);
		xmlSerializer.endTag("", "token");

		xmlSerializer.endTag("", "authentication");

		if(!dataInicio.equals("")){
			xmlSerializer.startTag("", "dataInicio");
			xmlSerializer.text(dataInicio);
			xmlSerializer.endTag("", "dataInicio");
		}

		if(!dataFim.equals("")){
			xmlSerializer.startTag("", "dataFim");
			xmlSerializer.text(dataFim);
			xmlSerializer.endTag("", "dataFim");
		}

		if(!codAutorizacao.equals("")){
			xmlSerializer.startTag("", "codAutorizacao");
			xmlSerializer.text(codAutorizacao);
			xmlSerializer.endTag("", "codAutorizacao");
		}

		if(!cartao.equals("")){
			xmlSerializer.startTag("", "cartao");
			xmlSerializer.text(cartao);
			xmlSerializer.endTag("", "cartao");
		}

		if(!pedido.equals("")){
			xmlSerializer.startTag("", "pedido");
			xmlSerializer.text(pedido);
			xmlSerializer.endTag("", "pedido");
		}

		if(!limitInicio.equals("")){
			xmlSerializer.startTag("", "limitInicio");
			xmlSerializer.text(limitInicio);
			xmlSerializer.endTag("", "limitInicio");
		}
		
		if(!qtdeRegistro.equals("")){
			xmlSerializer.startTag("", "qtdeRegistro");
			xmlSerializer.text(limitInicio);
			xmlSerializer.endTag("", "qtdeRegistro");
		}

		if(!limitFim.equals("")){
			xmlSerializer.startTag("", "limitFim");
			xmlSerializer.text(limitFim);
			xmlSerializer.endTag("", "limitFim");
		}

		xmlSerializer.endTag("", "transacao");

		// end DOCUMENT
		xmlSerializer.endDocument();

		return writer.toString();

	}

	private static Document getDomElement(String xml){
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is); 

		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}
		// return DOM
		return doc;
	}

}
