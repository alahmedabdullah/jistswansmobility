package fagner.analyzer;

import java.io.*;
import java.util.Formatter;
import java.util.HashMap;
public class Analyzer {
	private static BufferedReader buffer;
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
			new Analyzer().delay();
			buffer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void delay() {

			System.out.println("Delay-UDP");
			
			Formatter fmt1 = new Formatter();
			fmt1.format("%.8f", delayUnit(500, 550));
			System.out.println(fmt1);
			
			Formatter fmt2 = new Formatter();
			fmt2.format("%.8f", delayUnit(550, 600));
			System.out.println(fmt2);
			
			Formatter fmt3 = new Formatter();
			fmt3.format("%.8f", delayUnit(600, 650));
			System.out.println(fmt3);
			
			Formatter fmt4 = new Formatter();	
			fmt4.format("%.8f", delayUnit(650, 700));
			System.out.println(fmt4);
			
			Formatter fmt5 = new Formatter();
			fmt5.format("%.8f", delayUnit(700, 750));
			System.out.println(fmt5);
		
		

	}
		
	private double delayUnit(int timeInicial, int timeFinal) {
		double sumDelay1=0,sumDelay2=0,sumDelay3=0,sumDelay4=0,sumDelay5=0;
		int contDelay1=0,contDelay2=0,contDelay3=0,contDelay4=0,contDelay5=0;
		System.gc();
		try {

			BufferedReader buffer = new BufferedReader(new FileReader("NET.log"));
			HashMap<String,String> idsPacotesSend  = new HashMap<String, String>();

			String line = buffer.readLine();

			
			int cont=0;

			while ( line!= null)
			{  
				//System.out.println(cont++);
				String []tokens = line.split(" |\t");
				String evento = tokens[0]; 
				if (evento.equals("NetIpBase:INFO:send")){
					String []timeSTRSend = tokens[1].split("=");
					Double  time = Double.parseDouble(timeSTRSend[1]);

					if (time>timeInicial & time<= timeFinal){

						String tipoPacote = tokens[10];
						if(tipoPacote.equals("data=udp)"))  {
							String fonte = tokens[3].substring(12);
							String destino = tokens[4].substring(4);
							String idPacote = tokens[8].substring(3);
							idsPacotesSend.put(idPacote+destino+fonte,timeSTRSend[1]);	
						}
					}
				}else
				{			
					if (evento.equals("NetIpBase:INFO:receive")){
						String tipoPacote = tokens[11];
						String []timeSTRReceive = tokens[1].split("=");
						Double  time = Double.parseDouble(timeSTRReceive[1]);
						if(tipoPacote.equals("data=udp)"))  {		
							String fonte = tokens[4].substring(12);
							String destino = tokens[5].substring(4);
							String idPacote = tokens[9].substring(3);
							String key = idPacote+destino+fonte;
							if(idsPacotesSend.containsKey(key)){	
								String pacoteIDTime  = idsPacotesSend.get(key);
								double intervalo = time - Double.parseDouble(pacoteIDTime);	
								sumDelay1+=intervalo;
								contDelay1++;
								idsPacotesSend.remove(key);
							}
						}

					}
				}				
				line = buffer.readLine();
			}

			
			buffer.close();
		}
		catch (IOException e) {
			System.err.println(e);
		}
		return sumDelay1/contDelay1;
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