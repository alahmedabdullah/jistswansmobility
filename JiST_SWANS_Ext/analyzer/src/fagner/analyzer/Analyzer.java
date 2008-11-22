package fagner.analyzer;
import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
public class Analyzer {
	private static BufferedReader buffer;
	private static class PacoteSend{
		String id;
		String timeStr;
		public PacoteSend(String id, String timeStr) {
			super();
			this.id = id;
			this.timeStr = timeStr;
		}
		@Override
		public int hashCode() {
			return Integer.parseInt(id);
		}
	}
	public static void main(String args[]) {
		try {
			buffer = new BufferedReader(new FileReader("NET.log"));
			//overhead(buffer);
			buffer.close();
			buffer = new BufferedReader(new FileReader("NET.log"));
			//delivery(buffer);
			buffer.close();
			buffer = new BufferedReader(new FileReader("NET.log"));
			//dropped(buffer);
			buffer.close();
			buffer = new BufferedReader(new FileReader("NET.log"));
			delay(buffer);
			buffer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void delay(BufferedReader buffer) {
		try {

			
			HashMap<String,String> idsPacotesSend  = new HashMap<String, String>();

			String line = buffer.readLine();

			double sumDelay1=0,sumDelay2=0,sumDelay3=0,sumDelay4=0,sumDelay5=0;
			int contDelay1=0,contDelay2=0,contDelay3=0,contDelay4=0,contDelay5=0;
			
			while ( line!= null)
			{  
				String []tokens = line.split(" |\t");
				String evento = tokens[0]; 
				String []timeSTRSend = tokens[1].split("=");
				Double  time = Double.parseDouble(timeSTRSend[1]);
				if (time>500 && time<= 550){
					if (evento.equals("NetIpBase:INFO:send")){
						String tipoPacote = tokens[10];
						if(tipoPacote.equals("data=udp)"))  {
							String fonte = tokens[3].substring(12);
							String destino = tokens[4].substring(4);
							String idPacote = tokens[8].substring(3);
							idsPacotesSend.put(idPacote+destino+fonte,timeSTRSend[1]);	
						}
					}
					else
					{				
						if (evento.equals("NetIpBase:INFO:receive")){
							String tipoPacote = tokens[11];
							if(tipoPacote.equals("data=udp)"))  {		
								String fonte = tokens[4].substring(12);
								String destino = tokens[5].substring(4);
								String idPacote = tokens[9].substring(3);
								if(idsPacotesSend.containsKey(idPacote+destino+fonte)){	
									String pacoteIDTime  = idsPacotesSend.get(idPacote);
									double intervalo = time - Double.parseDouble(pacoteIDTime );	
									sumDelay1+=intervalo;
									contDelay1++;
								}
							}
						}

					}
				}
				if (time>550 && time<= 600){
					if (evento.equals("NetIpBase:INFO:send")){
						String tipoPacote = tokens[10];
						if(tipoPacote.equals("data=udp)"))  {
							String fonte = tokens[3].substring(12);
							String destino = tokens[4].substring(4);
							String idPacote = tokens[8].substring(3);
							idsPacotesSend.put(idPacote+destino+fonte,timeSTRSend[1]);	
						}
					}
					else
					{				
						if (evento.equals("NetIpBase:INFO:receive")){
							String tipoPacote = tokens[11];
							if(tipoPacote.equals("data=udp)"))  {		
								String fonte = tokens[4].substring(12);
								String destino = tokens[5].substring(4);
								String idPacote = tokens[9].substring(3);
								if(idsPacotesSend.containsKey(idPacote+destino+fonte)){	
									String pacoteIDTime  = idsPacotesSend.get(idPacote);
									double intervalo = time - Double.parseDouble(pacoteIDTime );	
									sumDelay2+=intervalo;
									contDelay2++;
								}
							}
						}

					}
				}
				
				if (time>600 && time<= 650){
					if (evento.equals("NetIpBase:INFO:send")){
						String tipoPacote = tokens[10];
						if(tipoPacote.equals("data=udp)"))  {
							String fonte = tokens[3].substring(12);
							String destino = tokens[4].substring(4);
							String idPacote = tokens[8].substring(3);
							idsPacotesSend.put(idPacote+destino+fonte,timeSTRSend[1]);	
						}
					}
					else
					{				
						if (evento.equals("NetIpBase:INFO:receive")){
							String tipoPacote = tokens[11];
							if(tipoPacote.equals("data=udp)"))  {		
								String fonte = tokens[4].substring(12);
								String destino = tokens[5].substring(4);
								String idPacote = tokens[9].substring(3);
								if(idsPacotesSend.containsKey(idPacote+destino+fonte)){	
									String pacoteIDTime  = idsPacotesSend.get(idPacote);
									double intervalo = time - Double.parseDouble(pacoteIDTime );	
									sumDelay3+=intervalo;
									contDelay3++;
								}
							}
						}

					}
				}
				
				if (time>650 && time<= 700){
					if (evento.equals("NetIpBase:INFO:send")){
						String tipoPacote = tokens[10];
						if(tipoPacote.equals("data=udp)"))  {
							String fonte = tokens[3].substring(12);
							String destino = tokens[4].substring(4);
							String idPacote = tokens[8].substring(3);
							idsPacotesSend.put(idPacote+destino+fonte,timeSTRSend[1]);	
						}
					}
					else
					{				
						if (evento.equals("NetIpBase:INFO:receive")){
							String tipoPacote = tokens[11];
							if(tipoPacote.equals("data=udp)"))  {		
								String fonte = tokens[4].substring(12);
								String destino = tokens[5].substring(4);
								String idPacote = tokens[9].substring(3);
								if(idsPacotesSend.containsKey(idPacote+destino+fonte)){	
									String pacoteIDTime  = idsPacotesSend.get(idPacote);
									double intervalo = time - Double.parseDouble(pacoteIDTime );	
									sumDelay4+=intervalo;
									contDelay4++;
								}
							}
						}

					}
				}
				
				if (time>700 && time<= 750){
					if (evento.equals("NetIpBase:INFO:send")){
						String tipoPacote = tokens[10];
						if(tipoPacote.equals("data=udp)"))  {
							String fonte = tokens[3].substring(12);
							String destino = tokens[4].substring(4);
							String idPacote = tokens[8].substring(3);
							idsPacotesSend.put(idPacote+destino+fonte,timeSTRSend[1]);	
						}
					}
					else
					{				
						if (evento.equals("NetIpBase:INFO:receive")){
							String tipoPacote = tokens[11];
							if(tipoPacote.equals("data=udp)"))  {		
								String fonte = tokens[4].substring(12);
								String destino = tokens[5].substring(4);
								String idPacote = tokens[9].substring(3);
								if(idsPacotesSend.containsKey(idPacote+destino+fonte)){	
									String pacoteIDTime  = idsPacotesSend.get(idPacote);
									double intervalo = time - Double.parseDouble(pacoteIDTime );	
									sumDelay5+=intervalo;
									contDelay5++;
								}
							}
						}

					}
				}
				
				line = buffer.readLine();
			}

			
			System.out.println("Delay-UDP");
			Formatter fmt1 = new Formatter();
		    fmt1.format("%.6f", sumDelay1/contDelay1*1000);
			System.out.println(fmt1);
			Formatter fmt2 = new Formatter();
		    fmt2.format("%.6f", sumDelay2/contDelay2*1000);
			System.out.println(fmt2);
			Formatter fmt3 = new Formatter();
		    fmt3.format("%.6f", sumDelay3/contDelay3*1000);
			System.out.println(fmt3);
			Formatter fmt4 = new Formatter();
		    fmt4.format("%.6f", sumDelay4/contDelay4*1000);
			System.out.println(fmt4);
			Formatter fmt5 = new Formatter();
		    fmt5.format("%.6f", sumDelay5/contDelay5*1000);
			System.out.println(fmt5);
		}
		catch (IOException e) {
			System.err.println(e);
		}
	}

	
	private static void dropped(BufferedReader buffer) {
		try {

			String line = buffer.readLine();

			int contS1=0,contS2=0,contS3=0,contS4=0,contS5=0;
			int contR1=0,contR2=0,contR3=0,contR4=0,contR5=0;
			int cont1=0,cont2=0;

			while ( line!= null)
			{  
				String []tokens = line.split(" |\t");
				String evento = tokens[0]; 
				String []timeSTR = tokens[1].split("=");



				if (evento.equals("NetIpBase:INFO:send")){
					String tipoPacote = tokens[10];
					if(tipoPacote.equals("data=udp)"))  {

						cont1++;

						Double  time = Double.parseDouble(timeSTR[1]);

						if (time>500 && time<= 550) 
							contS1++;
						if(time>550 && time<= 600)
							contS2++;
						if(time>600 && time<= 650)
							contS3++;
						if(time>650 && time<=700)
							contS4++;
						if(time>700 && time<=750)
							contS5++;

					}
				}
				if (evento.equals("NetIpBase:INFO:receive")){
					String tipoPacote = tokens[11];
					if(tipoPacote.equals("data=udp)"))  {
						Double  time = Double.parseDouble(timeSTR[1]);
						cont2++;
						if (time>500 && time<= 550) 
							contR1++;
						if(time>550 && time<= 600)
							contR2++;
						if(time>600 && time<= 650)
							contR3++;
						if(time>650 && time<=700)
							contR4++;
						if(time>700 && time<=750)
							contR5++;
					}
				}

				line = buffer.readLine();
			}


			System.out.println("Dropped-UDP");

			if (contS1!=0)
				System.out.println(contS1-contR1);
			if (contS2!=0)
				System.out.println(contS2-contR2);
			if (contS3!=0)
				System.out.println(contS3-contR3);
			if (contS4!=0)
				System.out.println(contS4-contR4);
			if (contS5!=0)
				System.out.println(contS5-contR5);


		}
		catch (IOException e) {
			System.err.println(e);
		}
	}

	
	private static void delivery(BufferedReader buffer) {
		try {

			String line = buffer.readLine();

			int contS1=0,contS2=0,contS3=0,contS4=0,contS5=0;
			int contR1=0,contR2=0,contR3=0,contR4=0,contR5=0;
			int cont1=0,cont2=0;

			while ( line!= null)
			{  
				String []tokens = line.split(" |\t");
				String evento = tokens[0]; 
				String []timeSTR = tokens[1].split("=");



				if (evento.equals("NetIpBase:INFO:send")){
					String tipoPacote = tokens[10];
					if(tipoPacote.equals("data=udp)"))  {

						cont1++;

						Double  time = Double.parseDouble(timeSTR[1]);

						if (time>500 && time<= 550) 
							contS1++;
						if(time>550 && time<= 600)
							contS2++;
						if(time>600 && time<= 650)
							contS3++;
						if(time>650 && time<=700)
							contS4++;
						if(time>700 && time<=750)
							contS5++;

					}
				}
				if (evento.equals("NetIpBase:INFO:receive")){
					String tipoPacote = tokens[11];
					if(tipoPacote.equals("data=udp)"))  {
						Double  time = Double.parseDouble(timeSTR[1]);
						cont2++;
						if (time>500 && time<= 550) 
							contR1++;
						if(time>550 && time<= 600)
							contR2++;
						if(time>600 && time<= 650)
							contR3++;
						if(time>650 && time<=700)
							contR4++;
						if(time>700 && time<=750)
							contR5++;
					}
				}

				line = buffer.readLine();
			}


			System.out.println("Delivery-UDP");

			if (contS1!=0)
				System.out.println((double)contR1/contS1);
			if (contS2!=0)
				System.out.println((double)contR2/contS2);
			if (contS3!=0)
				System.out.println((double)contR3/contS3);
			if (contS4!=0)
				System.out.println((double)contR4/contS4);
			if (contS5!=0)
				System.out.println((double)contR5/contS5);


		}
		catch (IOException e) {
			System.err.println(e);
		}
	}


	private static void overhead(BufferedReader buffer) {
		try {

			String line = buffer.readLine();

			int cont1=0,cont2=0,cont3=0,cont4=0,cont5=0;
			while ( line!= null)
			{  
				String []tokens = line.split(" |\t");
				String evento = tokens[0]; 
				String []timeSTR = tokens[1].split("=");

				String tipoPacote = tokens[10];

				if(evento.equals("NetIpBase:INFO:send") && (tipoPacote.equals("data=RREQ)")||tipoPacote.equals("data=RREP)")||tipoPacote.equals("data=RERR)")||tipoPacote.equals("data=HELLO)")))
				{
					Double  time = Double.parseDouble(timeSTR[1]);

					if (time>500 && time<= 550) 
						cont1++;
					if(time>550 && time<= 600)
						cont2++;
					if(time>600 && time<= 650)
						cont3++;
					if(time>650 && time<=700)
						cont4++;
					if(time>700 && time<=750)
						cont5++;
				}

				line = buffer.readLine();
			}
			System.out.println("Overhead-AODV");

			System.out.println(cont1);
			System.out.println(cont2);
			System.out.println(cont3);
			System.out.println(cont4);
			System.out.println(cont5);

		}
		catch (IOException e) {
			System.err.println(e);
		}
	}
}