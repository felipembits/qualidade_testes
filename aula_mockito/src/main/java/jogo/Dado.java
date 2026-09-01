package jogo;

import java.util.Random;

public class Dado {
	private static Random gerador = new Random();
	
	public int numero() {
		int numero = gerador.nextInt(6)+1;
		return numero;
	}
}
